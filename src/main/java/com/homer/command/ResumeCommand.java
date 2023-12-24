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
public class ResumeCommand extends Command {

    private final AudioPlayer player;

    public ResumeCommand(CommandsHolder commandsHolder, AudioPlayer player) {
        super(commandsHolder, "resume", "Resumes track playback.");
        this.player = player;
    }

    public void execute(@NotNull SlashCommandInteractionEvent event) {
        log.info("[{}] resumed playing", event.getUser().getName());

        if (player.getPlayingTrack() != null) {
            player.setPaused(false);
            event.getHook().editOriginal("```[" + event.getUser().getName() + "] resumed playing```")
                    .queue();
        } else {
            event.getHook().editOriginal("```There's nothing to resume.```")
                    .queue();
        }
    }

    @NotNull
    public SlashCommandData createSlashCommand() {
        return Commands.slash(this.getName(), this.getDescription());
    }
}
