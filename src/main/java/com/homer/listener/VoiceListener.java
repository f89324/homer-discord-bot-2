package com.homer.listener;

import com.homer.config.HomerProperties;
import com.homer.service.AudioPlayerSendHandler;
import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class VoiceListener extends ListenerAdapter {

    private final AudioPlayer player;
    private final HomerProperties homerProperties;
    private final Map<String, String> intros;

    @Autowired
    public VoiceListener(AudioPlayer player, HomerProperties homerProperties) {
        this.player = player;
        this.homerProperties = homerProperties;
        this.intros = getIntroAsMap();
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        logEvent(event);

        if (event.getMember().getUser().isBot()) {
            return;
        }


        GuildVoiceState botVoiceState = event.getGuild().getSelfMember().getVoiceState();
        boolean botAlreadyInVoiceChannel = botVoiceState != null && botVoiceState.getChannel() != null;
        if (botAlreadyInVoiceChannel) {
            if (event.getChannelJoined() != null && event.getChannelLeft() != null) {
                // User switch from one channel to another when the bot is already in voice channel
                if (event.getChannelLeft().getMembers().size() == 1) {
                    log.info("bot moved to [{}] cause it was alone", event.getChannelLeft().getName());
                    AudioManager manager = event.getGuild().getAudioManager();
                    manager.openAudioConnection(event.getChannelJoined());
                }
                playIntroForMember(event);

                return;
            }
            if (event.getChannelJoined() != null) {
                // User joined the voice channel when the bot is already in voice channel
                log.info("bot is already in [{}]", event.getChannelJoined().getName());
                AudioManager manager = event.getGuild().getAudioManager();
                manager.setSendingHandler(new AudioPlayerSendHandler(player));
                manager.openAudioConnection(event.getChannelJoined());

                playIntroForMember(event);

                return;
            }
            if (event.getChannelLeft() != null) {
                // user left voice chat
                if (event.getChannelLeft().getMembers().size() == 1) {
                    log.info("bot left [{}] cause it was alone", event.getChannelLeft().getName());
                    AudioManager manager = event.getGuild().getAudioManager();
                    manager.closeAudioConnection();
                }
            }
        } else {
            // User joined the voice channel when there is no one else in the voice channel
            if (event.getChannelJoined() != null) {
                log.info("bot join [{}]", event.getChannelJoined().getName());
                AudioManager manager = event.getGuild().getAudioManager();
                manager.setSendingHandler(new AudioPlayerSendHandler(player));
                manager.openAudioConnection(event.getChannelJoined());

                playIntroForMember(event);
            }
        }
    }

    private void playIntroForMember(@NotNull GuildVoiceUpdateEvent event) {
        String userName = event.getMember().getUser().getName();
        if (intros.containsKey(userName)) {
            String introFilename = intros.get(userName);
            log.info("playing intro [{}] for member [{}]", introFilename, userName);

            InputStream audioStream = AudioPlayerSendHandler.class.getClassLoader().getResourceAsStream("intro/" + introFilename);
            AudioTrackInfo trackInfo = new AudioTrackInfo(introFilename, "", 0, "", false, "");
            AudioTrack track = new Mp3AudioTrack(trackInfo, new NonSeekableInputStream(audioStream));

            player.stopTrack();
            player.playTrack(track);
        }
    }

    private void logEvent(@NotNull GuildVoiceUpdateEvent event) {
        log.info("[{}] {}({}) JOINED:{} LEFT:{}",
                event.getGuild().getName(),
                event.getMember().getUser().getName(),
                event.getMember().getNickname(),
                event.getChannelJoined() != null ? event.getChannelJoined().getName() : "-",
                event.getChannelLeft() != null ? event.getChannelLeft().getName() : "-");
    }

    private Map<String, String> getIntroAsMap() {
        return homerProperties.getIntro().stream()
                .collect(Collectors.toMap(
                        HomerProperties.MemberIntro::getUsername,
                        HomerProperties.MemberIntro::getFile));
    }
}