package com.homer.command;

import com.homer.service.CommandsHolder;
import com.homer.service.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SkipCommand extends Command {

    private final AudioPlayer player;
    private final TrackScheduler trackScheduler;

    public SkipCommand(CommandsHolder commandsHolder, AudioPlayer player, TrackScheduler trackScheduler) {
        super(commandsHolder, "skip", "Skips current track and plays the next one.");
        this.player = player;
        this.trackScheduler = trackScheduler;
    }

    public void execute(@NotNull SlashCommandInteractionEvent event) {
        if (player.getPlayingTrack() != null) {
            log.info("[{}] skip track [{}]", event.getUser().getName(), player.getPlayingTrack().getInfo().title);
            trackScheduler.nextTrack();

            event.getHook().editOriginal("```Skipped to next track.```")
                    .queue();
        } else {
            log.info("[{}] skip track, but there's nothing to skip.", event.getUser().getName());
            event.getHook().editOriginal("```Skipped to next track, but there's nothing to skip.```")
                    .queue();
        }
    }

    @NotNull
    public SlashCommandData createSlashCommand() {
        return Commands.slash(this.getName(), this.getDescription());
    }
}
