package com.homer.util;

import com.homer.exception.HomerException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public enum BotCommand {

    JOIN("join", "Join the voice channel you're in."),
    LEAVE("leave", "Leave a voice channel.");

    /**
     * Slash command name.
     */
    private final String name;
    /**
     * Description may not be longer than 100 characters.
     */
    private final String description;

    @NotNull
    public static BotCommand findByName(String name) {
        for (BotCommand command : values()) {
            if (command.name.equals(name)) {
                return command;
            }
        }
        throw new HomerException("Command [" + name + "] not found");
    }
}
