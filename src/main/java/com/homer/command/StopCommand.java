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
public class StopCommand extends Command {

    private final AudioPlayer player;

    public StopCommand(CommandsHolder commandsHolder, AudioPlayer player) {
        super(commandsHolder, "stop", "Stops broadcasting a track to the voice channel.");
        this.player = player;
    }

    public void execute(@NotNull SlashCommandInteractionEvent event) {
        log.info("[{}] stopped playing", event.getUser().getName());

        if (player.getPlayingTrack() != null) {
            player.stopTrack();
            event.getHook().editOriginal("```[" + event.getUser().getName() + "] stopped playing```")
                    .queue();
        } else {
            event.getHook().editOriginal("```There's nothing to stop.```")
                    .queue();
        }
    }

    @NotNull
    public SlashCommandData createSlashCommand() {
        return Commands.slash(this.getName(), this.getDescription());
    }
}
