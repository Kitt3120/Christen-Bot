package de.vanillekeks.christenbot.modules.modules.audio;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;

import de.vanillekeks.christenbot.Core;
import de.vanillekeks.christenbot.misc.Authentication;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.requests.RestAction;

/**
 * Created by kitt3120 on 04.08.2017 at 00:49.
 */
public class AudioRequest implements EventListener {

    private SearchListResponse response = null;
    private Message message = null;
    private User requester;

    public AudioRequest(String searchTerm, MessageChannel channel, User requester, YouTube youtube) throws IOException {
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
        if (response.isEmpty()) {
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
            message.editMessage("```" + list + "\n```").queue();
			Core.getBot().addEventListener(this);
        }
    }

    @Override
    public void onEvent(Event event) {
    	if(event instanceof MessageReceivedEvent) {
    		MessageReceivedEvent e = (MessageReceivedEvent) event;
    		if(e.getAuthor().equals(requester)) {
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
    			if(response.size() < index) {
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
    			Core.getAudioSystem().getAudioPlayerManager().loadItem(result.getId().getVideoId(), new TrackReceiveHandler(Core.getAudioSystem().getTrackScheduler()) {
					
					@Override
					public void onNoMatches() {
						message.editMessage("```Entschuldige. Es ist ein Fehler aufgetreten, der jetzt zu kompliziert zum erklären wäre.```");
					}
					
					@Override
					public void onLoadFailed(FriendlyException exception) {
						message.editMessage("```Der Folgende Fehler ist aufgetreten: " + exception.getMessage() + "```");
					}
				});
    		}
    	}
    }
}
