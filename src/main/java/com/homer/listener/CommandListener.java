package com.homer.listener;

import com.homer.util.BotCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class CommandListener extends ListenerAdapter {

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
}
