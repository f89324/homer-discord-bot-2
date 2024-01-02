package com.homer.command;

import com.homer.service.CommandsHolder;
import com.homer.service.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NowPlayingCommand extends Command {

    private final AudioPlayer player;
    private final TrackScheduler trackScheduler;

    public NowPlayingCommand(CommandsHolder commandsHolder, AudioPlayer player, TrackScheduler trackScheduler) {
        super(commandsHolder, "now-playing", "Shows what is currently playing.");
        this.player = player;
        this.trackScheduler = trackScheduler;
    }

    public void execute(@NotNull SlashCommandInteractionEvent event) {
        AudioTrack playingTrack = player.getPlayingTrack();

        if (playingTrack != null) {
            LinkedList<AudioTrack> playlist = trackScheduler.getPlaylist();

            // Discord message character limit is 2000
            List<AudioTrack> croppedPlaylist = playlist.size() > 10
                    ? playlist.subList(0, 9)
                    : playlist;

            String playlistStr = "Current playlist: \n"
                    + "   \uD83D\uDD0A " + playingTrack.getInfo().title + " (" + formatTrackDuration(playingTrack) + ")\n" // 🔊
                    + croppedPlaylist.stream()
                    .map(track -> "   ⏩ " + track.getInfo().title + " (" + formatTrackDuration(track) + ")")
                    .collect(Collectors.joining("\n"))
                    + (playlist.size() > 10 ? "\n   ⏩ ..." : "");

            event.getHook().editOriginal("```" + playlistStr + "```")
                    .queue();
        } else {
            event.getHook().editOriginal("```Nothing is playing now.```")
                    .queue();
        }
    }

    @NotNull
    private String formatTrackDuration(@NotNull AudioTrack track) {
        if (track.getInfo().isStream) {
            return "STREAM";
        } else {
            return DurationFormatUtils.formatDuration(track.getDuration(), "HH:mm:ss");
        }
    }

    @NotNull
    public SlashCommandData createSlashCommand() {
        return Commands.slash(this.getName(), this.getDescription());
    }
}
