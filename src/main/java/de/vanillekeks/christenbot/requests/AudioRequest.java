package de.vanillekeks.christenbot.requests;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import de.vanillekeks.christenbot.Core;
import de.vanillekeks.christenbot.audio.TrackType;
import de.vanillekeks.christenbot.misc.Authentication;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.requests.RestAction;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by kitt3120 on 04.08.2017 at 00:49.
 */
public class AudioRequest implements EventListener {

    private SearchListResponse response = null;
    private MessageChannel channel;
    private Message message = null;
    private User requester;

    public AudioRequest(String searchTerm, MessageChannel channel, User requester, YouTube youtube) throws IOException {
        this.channel = channel;
        this.requester = requester;

        RestAction<Message> action = channel.sendMessage("```Suche...```");
        try {
            message = action.complete(true);
        } catch (RateLimitedException e) {
            e.printStackTrace();
            if (message != null) message.editMessage("```Etwas lief schief```").queue();
            return;
        }
        YouTube.Search.List search = youtube.search().list("id,snippet");
        search.setKey(Authentication.YOUTUBE_DATA_API_KEY);
        search.setQ(searchTerm);
        search.setType("video");
        search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
        search.setMaxResults(10L);

        response = search.execute();
        if (response.getItems().isEmpty()) {
            message.editMessage("```Keine Songs zu \"" + searchTerm + "\" gefunden```").queue();
            return;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            int count = 1;
            for (SearchResult result : response.getItems()) {
                stringBuilder.append("\n[" + count + "] - \"" + result.getSnippet().getTitle() + "\"");
                count++;
            }
            String list = stringBuilder.toString(); //Because the for loop will add a \n at the front of the list it will fit perfectly -> ```\nText\n```
            message.editMessage("```[" + requester.getName() + "]\n" + list + "\n```").queue();
            Core.getBot().addEventListener(this);
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent e = (MessageReceivedEvent) event;
            if (e.getAuthor().equals(requester) && e.getChannel().equals(channel)) {
                int index = 0;
                try {
                    index = Integer.parseInt(e.getMessage().getContent());
                } catch (Exception e2) {
                    final String msgText = message.getChannel().getMessageById(message.getId()).complete().getContent();
                    message.editMessage("```Bitte gebe eine Nummer ein```").queue();
                    Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {

                        @Override
                        public void run() {
                            message.editMessage(msgText).queue();
                        }
                    }, 2, TimeUnit.SECONDS);
                    return;
                }
                index--;
                if (response.getItems().size() < index) {
                    final String msgText = message.getChannel().getMessageById(message.getId()).complete().getContent();
                    message.editMessage("```Diese Nummer ist nicht in der Liste```").queue();
                    Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {

                        @Override
                        public void run() {
                            message.editMessage(msgText).queue();
                        }
                    }, 2, TimeUnit.SECONDS);
                    return;
                }

                SearchResult result = response.getItems().get(index);
                message.editMessage("```" + result.getSnippet().getTitle() + " zur Warteschlange hinzugefügt```").queue();

                Core.getAudioSystem().addToQueue(result.getId().getVideoId(), message, TrackType.YOUTUBE);

                if (e.getChannelType().equals(ChannelType.TEXT) && e.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                    e.getMessage().delete().queue();
                }

                Core.getBot().removeEventListener(this);
            }
        }
    }
}
