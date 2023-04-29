package com.homer.config;

import com.homer.exception.HomerException;
import com.homer.listener.CommandListener;
import com.homer.listener.MessageListener;
import com.homer.listener.ReadyListener;
import com.homer.util.BotCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class DiscordConfig {

    @Value("${homer.discord.token}")
    private String discordToken;

    @Bean(name = "bot")
    public JDA bot(@NotNull ReadyListener readyListener,
                   @NotNull MessageListener messageListener,
                   @NotNull CommandListener commandListener) {
        JDA bot = JDABuilder.createDefault(discordToken)
                .disableCache(CacheFlag.MEMBER_OVERRIDES)
                // Enable the VOICE_STATE cache to find a user's connected voice channel
                .enableCache(CacheFlag.VOICE_STATE)
                .setBulkDeleteSplittingEnabled(false)
                .enableIntents(
                        EnumSet.of(
                                // We need messages in guilds to accept commands from users
                                GatewayIntent.GUILD_MESSAGES,
                                // We need voice states to connect to the voice channel
                                GatewayIntent.GUILD_VOICE_STATES))
                .setStatus(OnlineStatus.ONLINE)
                .addEventListeners(readyListener, messageListener, commandListener)
                .build();

        // Sets the global command list to the provided commands (removing all others)
        bot.updateCommands()
                .addCommands(createCommands())
                .queue();

        try {
            // optionally block until JDA is ready
            return bot.awaitReady();
        } catch (InterruptedException e) {
            throw new HomerException("Error while connecting bot to discord", e);
        }
    }

    @NotNull
    private static List<SlashCommandData> createCommands() {
        return Arrays.stream(BotCommand.values())
                .map(c -> {
                    SlashCommandData command = Commands.slash(c.getName(), c.getDescription());
                    if (c.hasOptions()) {
                        command.addOptions(c.getOptions());
                    }
                    return command;
                })
                .collect(Collectors.toList());
    }
}
