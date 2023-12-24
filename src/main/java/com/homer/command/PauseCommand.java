package com.homer.command;

import com.homer.service.CommandsHolder;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PauseCommand extends Command {

    private final AudioPlayer player;

    public PauseCommand(CommandsHolder commandsHolder, AudioPlayer player) {
        super(commandsHolder, "pause", "Pauses track playback.");
        this.player = player;
    }

    public void execute(@NotNull SlashCommandInteractionEvent event) {
        log.info("[{}] paused playing", event.getUser().getName());

        if (player.getPlayingTrack() != null) {
            player.setPaused(true);
            event.getHook().editOriginal("```[" + event.getUser().getName() + "] paused playing```")
                    .queue();
        } else {
            event.getHook().editOriginal("```There's nothing to pause.```")
                    .queue();
        }
    }

    @NotNull
    public SlashCommandData createSlashCommand() {
        return Commands.slash(this.getName(), this.getDescription());
    }
}
