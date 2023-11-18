package com.homer.service;

import com.homer.command.Command;
import com.homer.exception.HomerException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Getter
public class CommandsHolder {

    private final Map<String, Command> commands = new HashMap<>();

    public void addCommand(@NotNull Command command) {
        this.commands.put(command.getName(), command);
    }

    @NotNull
    public Command getCommandByName(String name) {
        if (!this.commands.containsKey(name)) {
            throw new HomerException("Command [" + name + "] not found");
        }
        return this.commands.get(name);
    }
}
