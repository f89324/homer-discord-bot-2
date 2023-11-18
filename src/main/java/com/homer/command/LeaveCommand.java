package com.homer.command;

import com.homer.service.CommandsHolder;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LeaveCommand extends Command {

    public LeaveCommand(CommandsHolder commandsHolder) {
        super(commandsHolder, "leave", "Leave a voice channel.");
    }

    public void execute(@NotNull SlashCommandInteractionEvent event) {
        @SuppressWarnings("ConstantConditions") // event.getInteraction().getGuild() is null if the interaction is not from a guild. This cannot be because there is "isDM" check.
        AudioManager manager = event.getInteraction().getGuild().getAudioManager();
        manager.closeAudioConnection();

        // Edit the thinking message with our response on success
        event.getHook().editOriginal("```I left.```").queue();
    }

    @NotNull
    public SlashCommandData createSlashCommand() {
        return Commands.slash(this.getName(), this.getDescription());
    }
}
