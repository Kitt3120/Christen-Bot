package de.vanillekeks.christenbot.modules.modules.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public abstract class TrackReceiveHandler implements AudioLoadResultHandler {
	
	private TrackScheduler trackScheduler;
	
	public TrackReceiveHandler(TrackScheduler trackScheduler) {
		this.trackScheduler = trackScheduler;
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		trackScheduler.queue(track);
		
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		for(AudioTrack track : playlist.getTracks()) {
			trackScheduler.queue(track);
		}
		
	}

	@Override
	public void noMatches() {
		onNoMatches();
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		onLoadFailed(exception);
	}
	
	public abstract void onNoMatches();
	public abstract void onLoadFailed(FriendlyException exception);

}
