package com.homer.listener;

import com.homer.config.HomerProperties;
import com.homer.exception.HomerException;
import com.homer.service.AudioPlayerSendHandler;
import com.homer.util.BotCommand;
import com.homer.util.HomerUtil;
import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class CommandListener extends ListenerAdapter {

    private final AudioPlayer player;
    private final HomerProperties homerProperties;
    private final AudioPlayerManager audioPlayerManager;

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
            BotCommand command = BotCommand.findByName(event.getName());
            switch (command) {
                case JOIN:
                    join(event);
                    break;
                case LEAVE:
                    leave(event);
                    break;
                case REACT:
                    react(event);
                    break;
                case ABOUT:
                    about(event);
                    break;
                case PLAY:
                    play(event);
                    break;
                case STOP:
                    stop(event);
                    break;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            // Edit the thinking message with our response on success
            event.getHook().editOriginal("```Command error: [" + e.getMessage() + "] ```").queue();
        }
    }

    @SuppressWarnings("ConstantConditions")
    // event.getInteraction().getGuild() & event.getInteraction().getMember() is null if the interaction is not from a guild. This cannot be because there is "isDM" check.
    private void join(@NotNull SlashCommandInteractionEvent event) {
        AudioChannel channel;
        GuildChannelUnion channelFromOption = event.getOption("channel", OptionMapping::getAsChannel);
        if (channelFromOption != null) {
            channel = channelFromOption.asAudioChannel();
        } else {
            if (event.getInteraction().getMember().getVoiceState() == null
                    || event.getInteraction().getMember().getVoiceState().getChannel() == null) {
                log.warn("No channel to join");
                event.getHook().editOriginal("```No channel to join. Please either specify a valid channel or join one.```").queue();
                return;
            }
            channel = event.getInteraction().getMember().getVoiceState().getChannel();
        }

        log.info("Bot joins the voice channel [{}]", channel.getName());

        AudioManager manager = event.getInteraction().getGuild().getAudioManager();
        manager.setSendingHandler(new AudioPlayerSendHandler(player));
        manager.openAudioConnection(channel);

        // Edit the thinking message with our response on success
        event.getHook().editOriginal("```I joined [" + channel.getName() + "].```").queue();
    }

    private void leave(@NotNull SlashCommandInteractionEvent event) {
        @SuppressWarnings("ConstantConditions") // event.getInteraction().getGuild() is null if the interaction is not from a guild. This cannot be because there is "isDM" check.
        AudioManager manager = event.getInteraction().getGuild().getAudioManager();
        manager.closeAudioConnection();

        // Edit the thinking message with our response on success
        event.getHook().editOriginal("```I left.```").queue();
    }

    private void react(@NotNull SlashCommandInteractionEvent event) {
        @SuppressWarnings("ConstantConditions") // "reaction" option cannot be absent
        HomerProperties.Reaction reaction = findReactionByName(
                event.getOption("reaction").getAsString());

        @SuppressWarnings("ConstantConditions") // event.getInteraction().getGuild() is null if the interaction is not from a guild. This cannot be because there is "isDM" check.
        GuildVoiceState botVoiceState = event.getInteraction().getGuild().getSelfMember().getVoiceState();
        boolean botAlreadyInVoiceChannel = botVoiceState != null && botVoiceState.getChannel() != null;

        if (botAlreadyInVoiceChannel) {
            // Edit the thinking message with our response on success
            event.getHook().editOriginal("```I reacted with [" + reaction.getName() + "]```").queue();

            InputStream audioStream = HomerUtil.getFile(reaction.getFile());
            AudioTrackInfo trackInfo = new AudioTrackInfo("REACTION " + reaction.getName(), "", 0, "", false, "");
            AudioTrack track = new Mp3AudioTrack(trackInfo, new NonSeekableInputStream(audioStream));

            player.stopTrack();
            player.playTrack(track);
            log.info("the bot reacted with [{}] reaction to the audio channel [{}].", reaction.getName(), botVoiceState.getChannel().getName());
        } else {
            log.info("bot did not react cause it is not in the audio channel");
            event.getHook().editOriginal("```Nope. The bot reacts only when it is in the audio channel.```").queue();
        }
    }

    private void about(@NotNull SlashCommandInteractionEvent event) {
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

    private void play(@NotNull SlashCommandInteractionEvent event) {
        @SuppressWarnings("ConstantConditions") // "url" option cannot be absent
        String url = event.getOption("url").getAsString();
        audioPlayerManager.loadItem(url, createHandler(event, url));
    }

    private void stop(@NotNull SlashCommandInteractionEvent event) {
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
    private AudioLoadResultHandler createHandler(@NotNull SlashCommandInteractionEvent event, String url) {
        return new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                player.stopTrack();
                player.playTrack(track);
                log.info("Play track [{}]", track.getInfo().title);

                event.getHook().editOriginal("```Play track [" + track.getInfo().title + " — " + track.getInfo().author + "]```")
                        .queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                event.getHook().editOriginal("```Play playlist [" + playlist.getName() + "]```")
                        .queue();

                player.stopTrack();

                for (AudioTrack track : playlist.getTracks()) {
                    log.info("Play track [{}]", track);
                    player.playTrack(track);
                }
            }

            @Override
            public void noMatches() {
                log.warn("Track [{}] not found!", url);
                event.getHook().editOriginal("```Track [" + url + "] not found!```")
                        .queue();
            }

            @Override
            public void loadFailed(FriendlyException throwable) {
                log.warn("Something went wrong when trying to load track: [{}]", throwable.getMessage());
                event.getHook().editOriginal("```Something went wrong when trying to load track: [" + throwable.getMessage() + "]```")
                        .queue();
            }
        };
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

    @NotNull
    private HomerProperties.Reaction findReactionByName(@Nullable String name) {
        for (HomerProperties.Reaction reaction : homerProperties.getReactions()) {
            if (reaction.getName().equals(name)) {
                return reaction;
            }
        }
        throw new HomerException("React [" + name + "] not found");
    }
}
