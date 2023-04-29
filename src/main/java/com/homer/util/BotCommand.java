package com.homer.util;

import com.homer.exception.HomerException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@AllArgsConstructor
public enum BotCommand {

    JOIN("join", "Join the voice channel you're in.",
            List.of(
                    new OptionData(
                            OptionType.CHANNEL, "channel", "Channel to join")
                            .setChannelTypes(ChannelType.VOICE))),
    LEAVE("leave", "Leave a voice channel.", null);

    /**
     * Slash command name.
     */
    private final String name;
    /**
     * Description may not be longer than 100 characters.
     */
    private final String description;
    private final List<OptionData> options;

    @NotNull
    public static BotCommand findByName(String name) {
        for (BotCommand command : values()) {
            if (command.name.equals(name)) {
                return command;
            }
        }
        throw new HomerException("Command [" + name + "] not found");
    }

    public boolean hasOptions() {
        return options != null && !options.isEmpty();
    }
}
