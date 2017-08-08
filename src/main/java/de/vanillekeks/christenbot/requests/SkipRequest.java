package de.vanillekeks.christenbot.requests;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.vanillekeks.christenbot.Core;
import de.vanillekeks.christenbot.audio.QueueEmptyException;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by kitt3120 on 06.08.2017 at 19:56.
 */
public abstract class SkipRequest {

    private MessageChannel channel;
    private Message message = null;
    private User requester;
    private AudioTrack currentTrack;

    private int votes = 0;
    private int votesToSuccess = 5; //Default 5 if something goes wrong
    private int timeLeft = 25;
    private boolean isRunning = true;

    private ScheduledExecutorService refresher;

    public SkipRequest(User requester, MessageChannel channel) {
        this.requester = requester;
        this.channel = channel;
        this.currentTrack = Core.getAudioSystem().getCurrentTrack();

        if (currentTrack == null) {
            channel.sendMessage("```Gerade spielt kein Song```").queue();
            onFinish();
            return;
        }

        if (!Core.getAudioSystem().getAudioManager().isConnected()) {
            channel.sendMessage("```Ich befinde mich gerade in keinem VoiceChannel. Bitte benutze !lock```").queue();
            onFinish();
            return;
        }

        int userCount = Core.getAudioSystem().getAudioManager().getConnectedChannel().getMembers().size() - 1;
        if (userCount <= 2) {
            votesToSuccess = userCount;
        } else {
            votesToSuccess = userCount / 2 + 1;
        }

        refresher = Executors.newSingleThreadScheduledExecutor();
        refresher.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                refreshMessage();
                timeLeft--;
                if (timeLeft == 0) cancel();
            }
        }, 0L, 1L, TimeUnit.SECONDS);
        isRunning = true;
    }

    private void refreshMessage() {
        String msg = "Song überspringen: " + currentTrack.getInfo().title + "\n[";
        int spaces = votesToSuccess - votes;
        for (int i = 0; i < votes; i++) {
            msg = msg + "=";
        }
        for (int i = 0; i < spaces; i++) {
            msg = msg + " ";
        }
        msg = msg + "]\n" + votes + "/" + votesToSuccess + "\nGestartet von:" + requester.getName() + "\nZeit verbleibend: " + timeLeft;
        if (message == null) {
            try {
                message = channel.sendMessage("```" + msg + "```").complete(true);
                message.addReaction("✅").queue();
            } catch (RateLimitedException e) {
                e.printStackTrace();
                message.delete();
                message = null;
                return;
            }
        } else {
            message.editMessage("```" + msg + "```").queue();
        }

        refreshVotes();

        if (votes >= votesToSuccess) success();
    }

    private void refreshVotes() {
        for (MessageReaction reaction : message.getChannel().getMessageById(message.getId()).complete().getReactions()) {
            if (reaction.getEmote().getName().equalsIgnoreCase("✅")) {
                votes = reaction.getCount() - 1;
            }
        }
    }

    public void success() {
        refresher.shutdown();
        isRunning = false;
        message.editMessage("```Skip-Anfrage für Song " + currentTrack.getInfo().title + " mit " + votesToSuccess + " von " + votesToSuccess + " votes erfolgreich```").queue();
        try {
            Core.getAudioSystem().getTrackScheduler().skip();
        } catch (QueueEmptyException e) {
            e.printStackTrace();
            message.editMessage("```Ein Fehler ist aufgetreten```").queue();
        }
        onFinish();
    }

    public void cancel() {
        refresher.shutdown();
        isRunning = false;
        message.editMessage("```Skip-Anfrage für Song " + currentTrack.getInfo().title + " mit " + votes + " von " + votesToSuccess + " votes fehlgeschlagen```").queue();
        onFinish();
    }

    public abstract void onFinish();

    public void setVotesToSuccess(int votesToSuccess) {
        this.votesToSuccess = votesToSuccess;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public int getVotes() {
        return votes;
    }

    public int getVotesToSuccess() {
        return votesToSuccess;
    }
}
