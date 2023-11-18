package com.homer.listener;

import com.homer.service.CommandsHolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class CommandListener extends ListenerAdapter {

    private final CommandsHolder commandsHolder;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        logEvent(event);

        // This sends a "Bot is thinking..." message which is later edited
        event.deferReply().queue();

        if (event.getUser().isBot()) {
            return;
        }

        if (isDM(event)) {
            //noinspection ConstantConditions
            event.getChannel().asPrivateChannel().getUser().openPrivateChannel().queue(
                    channel -> channel.sendMessage("```Nope. The bot works only in the guild.```").queue());
            return;
        }

        try {
            commandsHolder.getCommandByName(event.getName())
                    .execute(event);
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            // Edit the thinking message with our response on success
            event.getHook().editOriginal("```Command error: [" + e.getMessage() + "] ```").queue();
        }
    }

    private void logEvent(@NotNull SlashCommandInteractionEvent event) {
        if (isDM(event)) {
            log.info("[PM] {} command [{}]",
                    event.getUser().getName(),
                    event.getCommandString());
        } else {
            log.info("[{}][{}] {} command [{}]",
                    event.getChannel().asGuildMessageChannel().getGuild().getName(),
                    event.getChannel().asGuildMessageChannel().getName(),
                    event.getUser().getName(),
                    event.getFullCommandName());
        }
    }

    private boolean isDM(@NotNull SlashCommandInteractionEvent event) {
        return ChannelType.PRIVATE.equals(event.getChannel().getType());
    }
}
