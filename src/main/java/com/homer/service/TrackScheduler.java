package com.homer.service;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
@Slf4j
@Component("trackScheduler")
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private final Object priorityTrackSwitchLock;

    /**
     * @param player The audio player this scheduler uses
     */
    @Autowired
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        priorityTrackSwitchLock = new Object();

        player.addListener(this);
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(track);
            return;
        }
        log.info("Play track [{}]", track.getInfo().title);
    }

    @NotNull
    public LinkedList<AudioTrack> getPlaylist() {
        return new LinkedList<>(queue);
    }

    /**
     * Play the track in priority. Then continue playing the current tracks.
     */
    public void playPriorityTrack(@NotNull AudioTrack track) {
        log.info("Play priority track [{}]", track.getInfo().title);

        synchronized (priorityTrackSwitchLock) {
            List<AudioTrack> dump = new ArrayList<>();
            AudioTrack currentTrack = player.getPlayingTrack();

            // if a track is already playing, then save the playlist to continue after playing the priority one
            if (currentTrack != null) {
                // It is possible to save the current position for later playback from the same position,
                // but after this the player may throw an error.
                // currentTrackClone.setPosition(currentTrack.getPosition());
                dump.add(currentTrack.makeClone());
                dump.addAll(queue);

                dump.add(currentTrack);
                dump.addAll(queue);

                player.stopTrack();
                player.setPaused(false);
                player.startTrack(track, true);

                dump.forEach(queue::offer);
            } else {
                player.startTrack(track, true);
            }
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        AudioTrack nextTrack = queue.poll();
        if (nextTrack != null) {
            log.info("Play track [{}]", nextTrack.getInfo().title);
        }
        player.startTrack(nextTrack, false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }
}