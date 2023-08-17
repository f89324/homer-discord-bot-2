package com.homer.listener;

import com.homer.config.HomerProperties;
import com.homer.util.HomerUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ReadyListener implements EventListener {

    private final HomerProperties homerProperties;

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            validateConfig();
            log.info("Homer is ready!");
        }
    }

    private void validateConfig() {
        log.info("start of config validation");

        String errors = "";

        errors += checkIntroAudioFilesExisting();
        errors += checkReactionAudioFilesExisting();

        if (!errors.isBlank()) {
            log.error("Errors in config:" + errors);
        }

        log.info("end of config validation");

        if (!errors.isBlank()) {
            log.error("There are errors in the application configuration!");
            System.exit(0);
        }
    }

    @NotNull
    private String checkIntroAudioFilesExisting() {
        StringBuilder errorsBuilder = new StringBuilder();

        homerProperties.getIntro().forEach(intro -> {
            try {
                HomerUtil.getFile(intro.getFile());
            } catch (Exception e) {
                errorsBuilder.append("\n * File [").append(intro.getFile()).append("] not found!");
            }
        });

        return errorsBuilder.toString();
    }

    @NotNull
    private String checkReactionAudioFilesExisting() {
        StringBuilder errorsBuilder = new StringBuilder();

        homerProperties.getReactions().forEach(reaction -> {
            try {
                HomerUtil.getFile(reaction.getFile());
            } catch (Exception e) {
                errorsBuilder.append("\n * File [").append(reaction.getFile()).append("] not found!");
            }
        });

        return errorsBuilder.toString();
    }
}
