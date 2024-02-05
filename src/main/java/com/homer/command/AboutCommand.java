package com.homer.command;

import com.homer.config.HomerProperties;
import com.homer.service.CommandsHolder;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AboutCommand extends Command {

    private final HomerProperties homerProperties;

    public AboutCommand(CommandsHolder commandsHolder, HomerProperties homerProperties) {
        super(commandsHolder, "about", "Shows information about the bot.");
        this.homerProperties = homerProperties;
    }

    public void execute(@NotNull SlashCommandInteractionEvent event) {
        String reactionsHint = "Available reactions: \n"
                + homerProperties.getReactions().stream()
                .map(reaction -> "[" + reaction.getEmoji() + "] "
                        + reaction.getName() + " - " + reaction.getDescription())
                .collect(Collectors.joining("\n"));

        event.getHook().editOriginal("```" + reactionsHint + "```")
                .setActionRow(
                        List.of(
                                Button.link("https://github.com/f89324/homer-discord-bot-2", "GitHub"),
                                Button.link("https://youtu.be/dQw4w9WgXcQ", "Full Guide")))
                .queue();
    }

    @NotNull
    public SlashCommandData createSlashCommand() {
        return Commands.slash(this.getName(), this.getDescription());
    }
}
