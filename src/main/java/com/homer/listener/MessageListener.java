package com.homer.listener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        if (event.isFromType(ChannelType.PRIVATE)) {
            log.info("[{}] tried to write a message to the bot.", event.getAuthor().getName());
            event.getAuthor().openPrivateChannel().queue(
                    channel -> channel.sendMessage("```Nope. The bot works only in the guild and only with slash commands.```").queue());
        }
    }
}