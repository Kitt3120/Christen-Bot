package de.vanillekeks.christenbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import de.vanillekeks.christenbot.Core;

import java.util.ArrayList;
import java.util.List;

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
		if (queue.size() > index) {
			AudioTrack track = queue.get(index);
			queue.remove(index);
			System.out.println(track.getInfo().title + " removed from queue");
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
		if (!queue.isEmpty()) play();
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

	public int getTrackIdByTrack(AudioTrack track) throws TrackNotInQueueException {
		if (!queue.contains(track)) throw new TrackNotInQueueException();
		int count = 0;
		for (AudioTrack aTrack : queue) {
			if (aTrack.equals(track)) return count;
			count++;
		}
		return -1;
	}
	
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) {
			System.out.println("Track end: " + endReason.name() + " - Playing next track");
			try {
				remove(0);
			} catch (QueueSizeTooSmallException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Track end: " + endReason.name() + "- Stopping");
		}

		if (queue.isEmpty()) {
			stop();
		}
	}

	public List<AudioTrack> getQueue() {
		return queue;
	}

	public boolean isPlaying() {
		return isPlaying;
	}
}
