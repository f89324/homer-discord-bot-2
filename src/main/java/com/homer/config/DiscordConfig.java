package com.homer.config;

import com.homer.exception.HomerException;
import com.homer.listener.CommandListener;
import com.homer.listener.MessageListener;
import com.homer.listener.ReadyListener;
import com.homer.listener.VoiceListener;
import com.homer.util.BotCommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class DiscordConfig {

    @Value("${homer.discord.token}")
    private String discordToken;

    @Bean(name = "bot")
    public JDA bot(@NotNull HomerProperties homerProperties,
                   @NotNull ReadyListener readyListener,
                   @NotNull MessageListener messageListener,
                   @NotNull VoiceListener voiceListener,
                   @NotNull CommandListener commandListener) {
        JDA bot = JDABuilder.createDefault(discordToken)
                .disableCache(CacheFlag.MEMBER_OVERRIDES)
                // Enable the VOICE_STATE cache to find a user's connected voice channel
                .enableCache(CacheFlag.VOICE_STATE)
                .setBulkDeleteSplittingEnabled(false)
                .enableIntents(
                        EnumSet.of(
                                // Needed for accept commands from users
                                GatewayIntent.GUILD_MESSAGES,
                                // Needed for connect to the voice channel
                                GatewayIntent.GUILD_VOICE_STATES))
                .setStatus(OnlineStatus.ONLINE)
                .addEventListeners(readyListener, messageListener, voiceListener, commandListener)
                .build();

        // Sets the global command list to the provided commands (removing all others)
        bot.updateCommands()
                .addCommands(createCommands(homerProperties.getReactions()))
                .queue();

        try {
            // optionally block until JDA is ready
            return bot.awaitReady();
        } catch (InterruptedException e) {
            throw new HomerException("Error while connecting bot to discord", e);
        }
    }

    @Bean(name = "audioPlayerManager")
    public AudioPlayerManager audioPlayerManager() {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager();
        youtubeAudioSourceManager.configureRequests(config -> RequestConfig.copy(config)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build());
        playerManager.registerSourceManager(youtubeAudioSourceManager);
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new BeamAudioSourceManager());

        AudioSourceManagers.registerRemoteSources(playerManager);

        return playerManager;
    }

    @Bean(name = "audioPlayer")
    public AudioPlayer audioPlayer(AudioPlayerManager audioPlayerManager) {
        return audioPlayerManager.createPlayer();
    }

    @NotNull
    private List<SlashCommandData> createCommands(List<HomerProperties.Reaction> reactions) {
        return List.of(
                createSlashCommand(BotCommand.JOIN)
                        .addOptions(
                                List.of(
                                        new OptionData(
                                                OptionType.CHANNEL, "channel", "Channel to join")
                                                .setChannelTypes(ChannelType.VOICE))),
                createSlashCommand(BotCommand.LEAVE),
                createSlashCommand(BotCommand.REACT)
                        .addOptions(
                                List.of(
                                        new OptionData(
                                                OptionType.STRING, "reaction", "Reaction to play", true)
                                                .addChoices(createChoicesForReaction(reactions)))),
                createSlashCommand(BotCommand.PLAY)
                        .addOptions(
                                List.of(
                                        new OptionData(
                                                OptionType.STRING, "url", "URL to track location", true))),
                createSlashCommand(BotCommand.STOP),
                createSlashCommand(BotCommand.ABOUT)
        );
    }

    @NotNull
    private SlashCommandData createSlashCommand(@NotNull BotCommand command) {
        return Commands.slash(command.getName(), command.getDescription());
    }

    @NotNull
    private List<Command.Choice> createChoicesForReaction(@NotNull List<HomerProperties.Reaction> reactions) {
        return reactions.stream()
                .map(r -> new Command.Choice(
                        "[" + r.getEmoji() + "] " + r.getName(),
                        r.getName()))
                .collect(Collectors.toList());
    }
}
