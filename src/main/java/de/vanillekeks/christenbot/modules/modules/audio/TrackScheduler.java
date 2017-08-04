package de.vanillekeks.christenbot.modules.modules.audio;

import java.util.ArrayList;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import de.vanillekeks.christenbot.Core;

public class TrackScheduler extends AudioEventAdapter {
	
	private boolean isPlaying = false;
	
	private List<AudioTrack> queue = new ArrayList<>();

	public TrackScheduler(AudioPlayer audioPlayer) {
		audioPlayer.addListener(this);
	}

	public void queue(AudioTrack track) {
		queue.add(track);
		System.out.println(track.getInfo().title + " added to queue");
		try {
			play();
		} catch (QueueEmptyException e) {
			e.printStackTrace();
		}
	}
	
	public void remove(int index) throws QueueSizeTooSmallException {
		if(queue.size() < index) {
			queue.remove(index);
		} else {
			throw new QueueSizeTooSmallException("The queue only has " + queue.size() + " tracks but tried to remove track with id " + index);
		}
	}
	
	public void play() throws QueueEmptyException {
		if(!isPlaying()) {
			if(!queue.isEmpty()) {
				AudioTrack next = queue.get(0);
				Core.getAudioSystem().getAudioPlayer().playTrack(next);
				isPlaying = true;
				System.out.println("Now playing " + next.getInfo().title);
			} else {
				stop();
				throw new QueueEmptyException();
			}
		}
	}
	
	public void skip() throws QueueEmptyException {
		System.out.println("Skipping current track");
		stop();
		try {
			remove(0);
		} catch (QueueSizeTooSmallException e) {
			e.printStackTrace();
		}
		play();
	}
	
	public void stop() {
		Core.getAudioSystem().getAudioPlayer().stopTrack();
		isPlaying = false;
		System.out.println("Current track stopped");
	}
	
	public void pause() {
		Core.getAudioSystem().getAudioPlayer().setPaused(true);
		isPlaying = false;
		System.out.println("Current track paused");
	}
	
	public void resume() {
		Core.getAudioSystem().getAudioPlayer().setPaused(false);
		isPlaying = true;
		System.out.println("Current track resumed");
	}
	
	public boolean isPlaying() {
		return !Core.getAudioSystem().getAudioPlayer().isPaused() && isPlaying;
	}
	
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if(!endReason.equals(AudioTrackEndReason.STOPPED)) {
			System.out.println("Current track end");
			try {
				remove(0);
			} catch (QueueSizeTooSmallException e) {
				e.printStackTrace();
			}
		}
	}

}
