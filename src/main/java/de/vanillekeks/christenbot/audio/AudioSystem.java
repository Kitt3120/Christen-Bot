package de.vanillekeks.christenbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import de.vanillekeks.christenbot.Core;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.managers.AudioManager;

public class AudioSystem implements AudioSendHandler {

    private AudioPlayerManager audioPlayerManager;
    private AudioManager audioManager;
    private AudioPlayer audioPlayer;
    private TrackScheduler trackScheduler;
    private AudioFrame lastFrame;

    public AudioSystem() {
        audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);

        audioPlayer = audioPlayerManager.createPlayer();
        trackScheduler = new TrackScheduler(audioPlayer);

        //Setup the audioManager 2 seconds delayed because of a nullpointer
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000L);
                    audioManager = Core.getMainGuild().getAudioManager();
                    audioManager.setSendingHandler(Core.getAudioSystem());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public byte[] provide20MsAudio() {
        return lastFrame.data;
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public void addToQueue(String source, final Message message, TrackType trackType) {
        if (trackType.equals(TrackType.YOUTUBE) && !(source.toLowerCase().startsWith("https://youtube.com") || source.toLowerCase().startsWith("http://youtube.com")))
            source = "https://youtube.com/watch?v=" + source;
        Core.getAudioSystem().getAudioPlayerManager().loadItem(source, new TrackReceiveHandler(Core.getAudioSystem().getTrackScheduler()) {

            @Override
            public void onNoMatches() {
                message.editMessage("```Entschuldige. Es ist ein Fehler aufgetreten, der jetzt zu kompliziert zum erklären wäre.```");
            }

            @Override
            public void onLoadFailed(FriendlyException exception) {
                exception.printStackTrace();
                message.editMessage("```Der Link konnte nicht geladen werden```");
            }
        });
    }

    public void addToQueue(String source, TrackType trackType, AudioLoadResultHandler audioLoadResultHandler) {
        if (trackType.equals(TrackType.YOUTUBE) && !(source.toLowerCase().startsWith("https://youtube.com") || source.toLowerCase().startsWith("http://youtube.com")))
            source = "https://youtube.com/watch?v=" + source;
        Core.getAudioSystem().getAudioPlayerManager().loadItemOrdered(Core.getAudioSystem().getTrackScheduler(), source, audioLoadResultHandler);
    }

    public AudioTrack getCurrentTrack() {
        if (trackScheduler.getQueue().size() == 0) return null;
        return trackScheduler.getQueue().get(0);
    }

    public void stop() {
        trackScheduler.stop();
    }

    public void start() throws QueueEmptyException {
        trackScheduler.play();
    }
}
