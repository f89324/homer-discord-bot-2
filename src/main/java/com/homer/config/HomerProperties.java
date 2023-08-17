package com.homer.config;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Validated
@Component
@AllArgsConstructor
@ConfigurationProperties(prefix = "homer")
public class HomerProperties {
    private List<MemberIntro> intro;
    private List<Reaction> reactions;

    @Getter
    @AllArgsConstructor
    public static class MemberIntro {
        @NotBlank
        private String username;
        @NotBlank
        private String file;
    }

    @Getter
    @AllArgsConstructor
    public static class Reaction {
        @NotBlank
        private String name;
        @NotBlank
        private String file;
        @NotBlank
        private String emoji;
        @NotBlank
        private String description;
    }
}
