package com.homer.listener;

import com.homer.service.AudioPlayerSendHandler;
import com.homer.util.BotReactButton;
import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
@AllArgsConstructor
public class ButtonListener extends ListenerAdapter {

    private AudioPlayer player;

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        logEvent(event);

        // This sends a "Bot is thinking..." message which is later edited
//        event.deferReply().queue(); // TODO

        if (event.getButton().getId() != null &&
                BotReactButton.exist(event.getButton().getId())) {
            BotReactButton byName = BotReactButton.findByName(event.getButton().getId()); // TODO

            GuildVoiceState botVoiceState = event.getGuild().getSelfMember().getVoiceState();
            boolean botAlreadyInVoiceChannel = botVoiceState != null && botVoiceState.getChannel() != null;
            if (botAlreadyInVoiceChannel) {
                log.warn("react with [{}]", byName.getName()); // TODO
                event.reply("react with [" + byName.getName() + "]").queue(); // TODO

                InputStream audioStream = AudioPlayerSendHandler.class.getClassLoader().getResourceAsStream("reactions/" + byName.getFile());
                AudioTrackInfo trackInfo = new AudioTrackInfo("", "", 0, "", false, ""); // TODO
                AudioTrack track = new Mp3AudioTrack(trackInfo, new NonSeekableInputStream(audioStream));

                player.stopTrack();
                player.playTrack(track);
                log.warn("---------------------2"); // TODO
            } else {
                log.warn("not react cause not in voice"); // TODO
                event.reply("NOPE :)").queue(); // TODO
            }
        }
    }

    private void logEvent(@NotNull ButtonInteractionEvent event) {
        if (isDM(event)) {
            log.info("[PM] {} hit button [{}]",
                    event.getUser().getName(),
                    event.getButton().getId());
        } else {
            log.info("[{}][{}] {} hit button [{}]",
                    event.getChannel().asGuildMessageChannel().getGuild().getName(),
                    event.getChannel().asGuildMessageChannel().getName(),
                    event.getUser().getName(),
                    event.getButton().getId());
        }
    }

    private boolean isDM(@NotNull ButtonInteractionEvent event) {
        return ChannelType.PRIVATE.equals(event.getChannel().getType());
    }
}
