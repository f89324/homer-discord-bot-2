package com.homer.config;

import com.homer.exception.HomerException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;

@Configuration
public class DiscordConfig {

    @Value("${homer.discord.token}")
    private String discordToken;

    @Bean(name = "bot")
    public JDA bot() {
        JDA bot = JDABuilder.createDefault(discordToken)
                .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .setBulkDeleteSplittingEnabled(false)
                .enableIntents(
                        EnumSet.of(
                                // We need messages in guilds to accept commands from users
                                GatewayIntent.GUILD_MESSAGES,
                                // We need voice states to connect to the voice channel
                                GatewayIntent.GUILD_VOICE_STATES))
                .setStatus(OnlineStatus.ONLINE)
                .build();

        try {
            // optionally block until JDA is ready
            return bot.awaitReady();
        } catch (InterruptedException e) {
            throw new HomerException("Error while connecting bot to discord", e);
        }
    }
}
