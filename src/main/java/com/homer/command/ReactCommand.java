package com.homer.command;

import com.homer.config.HomerProperties;
import com.homer.exception.HomerException;
import com.homer.service.CommandsHolder;
import com.homer.service.TrackScheduler;
import com.homer.util.HomerUtil;
import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ReactCommand extends Command {

    private final HomerProperties homerProperties;
    private final TrackScheduler trackScheduler;

    public ReactCommand(CommandsHolder commandsHolder, HomerProperties homerProperties, TrackScheduler trackScheduler) {
        super(commandsHolder, "react", "Broadcasts a reaction to the voice channel.");
        this.homerProperties = homerProperties;
        this.trackScheduler = trackScheduler;
    }

    public void execute(@NotNull SlashCommandInteractionEvent event) {
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

            trackScheduler.playPriorityTrack(track);

            log.info("the bot reacted with [{}] reaction to the audio channel [{}].", reaction.getName(), botVoiceState.getChannel().getName());
        } else {
            log.info("bot did not react cause it is not in the audio channel");
            event.getHook().editOriginal("```Nope. The bot reacts only when it is in the audio channel.```").queue();
        }
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

    @NotNull
    public SlashCommandData createSlashCommand() {
        return Commands.slash(this.getName(), this.getDescription())
                .addOptions(
                        List.of(
                                new OptionData(
                                        OptionType.STRING, "reaction", "Reaction to play", true)
                                        .addChoices(createChoicesForReaction(homerProperties.getReactions()))));
    }

    @NotNull
    private List<net.dv8tion.jda.api.interactions.commands.Command.Choice> createChoicesForReaction(@NotNull List<HomerProperties.Reaction> reactions) {
        return reactions.stream()
                .map(r -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(
                        "[" + r.getEmoji() + "] " + r.getName(),
                        r.getName()))
                .collect(Collectors.toList());
    }
}
