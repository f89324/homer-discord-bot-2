package com.homer.command;

import com.homer.service.CommandsHolder;
import com.homer.service.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
public class PlayCommand extends Command {

    private final AudioPlayer player;
    private final AudioPlayerManager audioPlayerManager;
    private final TrackScheduler trackScheduler;

    public PlayCommand(CommandsHolder commandsHolder, AudioPlayer player, AudioPlayerManager audioPlayerManager, TrackScheduler trackScheduler) {
        super(commandsHolder, "play", "Broadcasts a track to the voice channel.");
        this.player = player;
        this.audioPlayerManager = audioPlayerManager;
        this.trackScheduler = trackScheduler;
    }

    public void execute(@NotNull SlashCommandInteractionEvent event) {
        @SuppressWarnings("ConstantConditions") // "url" option cannot be absent
        String url = event.getOption("url").getAsString();
        audioPlayerManager.loadItem(url, createHandler(event, url));
    }

    @NotNull
    private AudioLoadResultHandler createHandler(@NotNull SlashCommandInteractionEvent event, String url) {
        return new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                Consumer<String> errorHandler = (errorMessage) -> event.getChannel().sendMessage("```" + errorMessage + "```").queue();
                trackScheduler.queue(track, errorHandler);
                log.info("Play track [{}]", track.getInfo().title);

                event.getHook().editOriginal("```Play track [" + track.getInfo().title + " — " + track.getInfo().author + "]```")
                        .queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                event.getHook().editOriginal("```Play playlist [" + playlist.getName() + "]```")
                        .queue();

                for (AudioTrack track : playlist.getTracks()) {
                    Consumer<String> errorHandler = (errorMessage) -> event.getChannel().sendMessage("```" + errorMessage + "```").queue();
                    trackScheduler.queue(track, errorHandler);
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

    @NotNull
    public SlashCommandData createSlashCommand() {
        return Commands.slash(this.getName(), this.getDescription())
                .addOptions(
                        List.of(
                                new OptionData(
                                        OptionType.STRING, "url", "URL to track location", true)));
    }
}
