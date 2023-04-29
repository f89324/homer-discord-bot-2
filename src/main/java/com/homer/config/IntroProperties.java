package com.homer.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Component
@AllArgsConstructor
@ConfigurationProperties(prefix = "homer")
public class IntroProperties {
    private List<MemberIntroInfo> intro;

    @Getter
    @AllArgsConstructor
    public static class MemberIntroInfo {
        private String username;
        private String file;
    }

    public Map<String, String> getAsMap() {
        return intro.stream()
                .collect(Collectors.toMap(
                        IntroProperties.MemberIntroInfo::getUsername,
                        IntroProperties.MemberIntroInfo::getFile));
    }
}
