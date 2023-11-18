package com.homer.command;

import com.homer.service.CommandsHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

@Slf4j
@Getter
public abstract class Command {

    /**
     * Slash command name.
     */
    private final String name;
    /**
     * Description may not be longer than 100 characters.
     */
    private final String description;


    public Command(CommandsHolder commandsHolder, String name, String description) {
        this.name = name;
        this.description = description;
        commandsHolder.addCommand(this);
    }

    public abstract void execute(@NotNull SlashCommandInteractionEvent event);

    @NotNull
    public abstract SlashCommandData createSlashCommand();
}
