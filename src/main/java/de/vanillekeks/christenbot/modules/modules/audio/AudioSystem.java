package de.vanillekeks.christenbot.modules.modules.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.core.audio.AudioSendHandler;

public class AudioSystem implements AudioSendHandler {
	
	private AudioPlayerManager audioPlayerManager;
	private AudioPlayer audioPlayer;
	private TrackScheduler trackScheduler;
	private AudioFrame lastFrame;
	
	public AudioSystem() {
		audioPlayerManager = new DefaultAudioPlayerManager();
		audioPlayerManager.getConfiguration().setOpusEncodingQuality(100);
		audioPlayerManager.getConfiguration().setResamplingQuality(ResamplingQuality.HIGH);
		audioPlayer = audioPlayerManager.createPlayer();
		trackScheduler = new TrackScheduler(audioPlayer);
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

}
