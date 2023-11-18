package com.homer.command;

import com.homer.service.AudioPlayerSendHandler;
import com.homer.service.CommandsHolder;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class JoinCommand extends Command {

    private final AudioPlayer player;

    public JoinCommand(CommandsHolder commandsHolder, AudioPlayer player) {
        super(commandsHolder, "join", "Join the voice channel you're in.");
        this.player = player;
    }

    @SuppressWarnings("ConstantConditions")
    // event.getInteraction().getGuild() & event.getInteraction().getMember() is null if the interaction is not from a guild. This cannot be because there is "isDM" check.
    public void execute(@NotNull SlashCommandInteractionEvent event) {
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

    @NotNull
    public SlashCommandData createSlashCommand() {
        return Commands.slash(this.getName(), this.getDescription())
                .addOptions(
                        List.of(
                                new OptionData(
                                        OptionType.CHANNEL, "channel", "Channel to join")
                                        .setChannelTypes(ChannelType.VOICE)));
    }
}
