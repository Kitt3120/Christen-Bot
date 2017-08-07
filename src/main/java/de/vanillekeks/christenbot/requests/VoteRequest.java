package de.vanillekeks.christenbot.requests;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by kitt3120 on 06.08.2017 at 19:56.
 */
public abstract class VoteRequest {

    private MessageChannel channel;
    private Message message = null;
    private String voteMessage;
    private User requester;
    private AudioTrack currentTrack;

    private int positiveVotes = 0;
    private int negativeVotes = 0;
    private int timeLeft = 25;
    private boolean isRunning = true;
    private ArrayList<String> hasVoted = new ArrayList<>();

    private ScheduledExecutorService refresher;

    public VoteRequest(User requester, MessageChannel channel, int time, String voteMessage) {
        this.requester = requester;
        this.channel = channel;
        this.voteMessage = voteMessage;

        refresher = Executors.newSingleThreadScheduledExecutor();
        refresher.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                refreshMessage();
                timeLeft--;
                if (timeLeft == 0) finish();
            }
        }, 0L, 1L, TimeUnit.SECONDS);
    }

    private void refreshMessage() {
        String msg = "Vote von " + requester.getName() + "\n" + voteMessage + "\n\n[";
        for (int i = 0; i < positiveVotes; i++) {
            msg = msg + "✅";
        }
        for (int i = 0; i < negativeVotes; i++) {
            msg = msg + "❌";
        }
        msg = msg + "]\n\nZustimmungen: " + positiveVotes + "\nWidersprüche: " + negativeVotes + "\n\nZeit verbleibend: " + timeLeft;
        if (message == null) {
            try {
                message = channel.sendMessage("```" + msg + "```").complete(true);
                message.addReaction("✅").queue();
                message.addReaction("❌").queue();
            } catch (RateLimitedException e) {
                e.printStackTrace();
                message.delete();
                message = null;
                return;
            }
        } else {
            message.editMessage("```" + msg + "```").queue();
        }

        updateVotes();
    }

    private void updateVotes() {
        hasVoted.clear();
        positiveVotes = 0;
        negativeVotes = 0;
        for (MessageReaction reaction : message.getChannel().getMessageById(message.getId()).complete().getReactions()) {
            if (reaction.getEmote().getName().equalsIgnoreCase("✅")) {
                positiveVotes = reaction.getCount() - 1;
                for (User voter : reaction.getUsers()) {
                    hasVoted.add(voter.getId());
                }
            }
            if (reaction.getEmote().getName().equalsIgnoreCase("❌")) {
                for (User voter : reaction.getUsers()) {
                    if (!hasVoted.contains(voter.getId())) negativeVotes++;
                }
            }
        }
    }

    private void finish() {
        refresher.shutdown();
        refreshMessage();

        int totalVotes = positiveVotes + negativeVotes;

        double positivePercentage = positiveVotes / totalVotes * 100;
        double negativePercentage = negativeVotes / totalVotes * 100;

        String msg = "Vote von " + requester.getName() + "\n\"" + voteMessage + "\n\n[";
        for (int i = 0; i < positiveVotes; i++) {
            msg = msg + "✅";
        }
        for (int i = 0; i < negativeVotes; i++) {
            msg = msg + "❌";
        }
        msg = msg + "]\n\nZustimmungen: " + positiveVotes + " (" + positivePercentage + "%)\nWidersprüche: " + negativeVotes + " (" + negativePercentage + "%)\n\n";

        if (positivePercentage > negativePercentage) {
            msg = msg + "Die Mehrheit hat mit " + positivePercentage + "% zugestimmt";
        } else if (positivePercentage < negativePercentage) {
            msg = msg + "Die Mehrheit hat mit " + negativePercentage + "% wiedersprochen";
        } else {
            msg = msg + "Es gibt keine Mehrheit";
        }

        message.editMessage(msg).queue();

        onTimeUp(positiveVotes, negativeVotes, totalVotes, positivePercentage, negativePercentage, positivePercentage > negativePercentage);
    }

    public abstract void onTimeUp(int positiveVotes, int negativeVotes, int totalVotes, double positivePercentage, double negativePercentage, boolean isPositive);
}
