package de.vanillekeks.christenbot.modules.modules.audio;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import de.vanillekeks.christenbot.Core;
import de.vanillekeks.christenbot.misc.Authentication;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.requests.RestAction;

import java.io.IOException;

/**
 * Created by kitt3120 on 04.08.2017 at 00:49.
 */
public class AudioRequest implements EventListener {

    private SearchListResponse response = null;
    private User requester;

    public AudioRequest(String searchTerm, MessageChannel channel, User requester, YouTube youtube) throws IOException {
        this.requester = requester;

        RestAction<Message> action = channel.sendMessage("```Searching...```");
        Message message = null;
        try {
            message = action.complete(true);
        } catch (RateLimitedException e) {
            e.printStackTrace();
            if (message != null) message.editMessage("```Something went wrong```").queue();
            return;
        }
        YouTube.Search.List search = youtube.search().list("id,snippet");
        search.setKey(Authentication.YOUTUBE_DATA_API_KEY);
        search.setQ(searchTerm);
        search.setType("video");
        search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");

        response = search.execute();
        if (response.isEmpty()) {
            message.editMessage("```No videos found for \"" + searchTerm + "\"```").queue();
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
            Core.getInstance().getBot().addEventListener(this);
        }
    }

    @Override
    public void onEvent(Event event) {

    }
}
