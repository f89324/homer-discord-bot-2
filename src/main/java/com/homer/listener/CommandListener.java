package com.homer.listener;

import com.homer.service.AudioPlayerSendHandler;
import com.homer.util.BotCommand;
import com.homer.util.BotReactButton;
import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class CommandListener extends ListenerAdapter {

    private AudioPlayer player;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        logEvent(event);

        // This sends a "Bot is thinking..." message which is later edited
        event.deferReply().queue();

        if (event.getUser().isBot()) {
            return;
        }

        if (isDM(event)) {
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
                case PLAY:
                    play(event);
                    break;
                case STOP:
                    stop(event);
                    break;
                case VOLUME:
                    volume(event);
                    break;
                case PAUSE:
                    pause(event);
                    break;
                case RESUME:
                    resume(event);
                    break;
                case NOW_PLAYING:
                    // TODO
                    break;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            // Edit the thinking message with our response on success
            event.getHook().editOriginal("```Command error: [" + e.getMessage() + "] ```").queue();
        }
    }

    private void join(@NotNull SlashCommandInteractionEvent event) {
        if (event.getInteraction().getMember() == null || event.getInteraction().getGuild() == null) {
            // This is null if the interaction is not from a guild. Unreachable code because there is "isDM" check.
            return;
        }

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
        if (event.getInteraction().getGuild() == null) {
            // This is null if the interaction is not from a guild. Unreachable code because there is "isDM" check.
            return;
        }

        AudioManager manager = event.getInteraction().getGuild().getAudioManager();
        manager.closeAudioConnection();

        // Edit the thinking message with our response on success
        event.getHook().editOriginal("```I left.```").queue();
    }

    private void react(@NotNull SlashCommandInteractionEvent event) {
        List<Button> buttons = Arrays.stream(BotReactButton.values())
                .map(c -> Button.primary(c.getName(), c.getEmoji()))
                .collect(Collectors.toList());
        buttons.add(
                Button.link("https://github.com/f89324/homer-discord-bot-2", "GitHub"));

        event.getHook().editOriginal("")
                .setActionRow(buttons)
                .queue();
    }

    private void volume(@NotNull SlashCommandInteractionEvent event) {
// TODO
//        EnumSet<Permission> perms = Permission.getPermissions(PermissionUtil.getEffectivePermission(channel, self));
//        if (!perms.contains(Permission.VOICE_CONNECT)) {
//            log.error(String.valueOf(event.getMember())); // TODO
////            throw new InsufficientPermissionException(Permission.VOICE_CONNECT);
//        }

//        String reason = event.getOption("reason", OptionMapping::getAsString);

        player.setVolume(100); // TODO

        // Edit the thinking message with our response on success
        event.getHook().editOriginal("```WWWWWWWWWWWWW```").queue(); // TODO track name in response
    }

    private void resume(@NotNull SlashCommandInteractionEvent event) {
        //TODO
//        if (event.getMember().getUser().equals(event.getJDA().getSelfUser())
//                && flareBot.getMusicManager().hasPlayer(event.getGuild().getId())) {
//            flareBot.getMusicManager().getPlayer(event.getGuild().getId()).setPaused(false);
//        }

        player.setPaused(false);

        // Edit the thinking message with our response on success
        event.getHook().editOriginal("```I resume playing```").queue(); // TODO track name in response
    }

    private void pause(@NotNull SlashCommandInteractionEvent event) {
        // This sends a "Bot is thinking..." message which is later edited
        event.deferReply().queue();

        player.setPaused(true);

        // Edit the thinking message with our response on success
        event.getHook().editOriginal("```I pause playing```").queue(); // TODO track name in response
    }

    private void stop(@NotNull SlashCommandInteractionEvent event) {
        player.stopTrack();

        // Edit the thinking message with our response on success
        event.getHook().editOriginal("```I'm stop playing```").queue(); // TODO track name in response
    }

    private void play(@NotNull SlashCommandInteractionEvent event) {
        AudioTrack track = new Mp3AudioTrack(
                new AudioTrackInfo("", "", 0, "", false, ""),
                new NonSeekableInputStream(AudioPlayerSendHandler.class.getClassLoader().getResourceAsStream("src/main/resources/intro/ShawnMichaels.mp3")));

        player.stopTrack();
        player.playTrack(track);

        // Edit the thinking message with our response on success
        event.getHook().editOriginal("```I'm start playing```").queue(); // TODO track name in response
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

//            EmbedBuilder eb = new EmbedBuilder();
//        eb.setTitle("Title", null);
//        eb.setColor(new Color(201, 255, 54));
//        eb.setDescription("Text");
//        eb.addField("Title of field", "test of field", false);
//        eb.addBlankField(false);
//        eb.addField("Title of field 2", "test of field", false);
//        eb.setAuthor("name", null, "https://github.com/zekroTJA/DiscordBot/blob/master/.websrc/zekroBot_Logo_-_round_small.png");
//        eb.setFooter("Text", "https://github.com/zekroTJA/DiscordBot/blob/master/.websrc/zekroBot_Logo_-_round_small.png");
//        eb.setImage("https://github.com/zekroTJA/DiscordBot/blob/master/.websrc/logo%20-%20title.png");
//        eb.setThumbnail("https://github.com/zekroTJA/DiscordBot/blob/master/.websrc/logo%20-%20title.png");
//
//
//        event.getHook().editOriginal("")
//                .setEmbeds(eb.build())
//                .queue();
}
