package com.homer.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
@AllArgsConstructor
@ConfigurationProperties(prefix = "homer")
public class HomerProperties {
    private List<MemberIntro> intro;
    private List<Reaction> reactions;

    @Getter
    @AllArgsConstructor
    public static class MemberIntro {
        private String username;
        private String file;
    }

    @Getter
    @AllArgsConstructor
    public static class Reaction {
        private String name;
        private String file;
        private String emoji;
        private String description;
    }
}
