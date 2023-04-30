package com.homer.util;

import com.homer.exception.HomerException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum BotReactButton {

    MILLION("million", "million_question.mp3", "💰", "Кто хочет стать миллионером?"),
    SHIT("shit", "gtasa_ah_shit_here_we_go_again.mp3", "💩", "GTASA: Ah shit, here we go again"),
    WUBBA("wubba", "wubba_lubba_dub_dub.mp3", "🛸", "Wubba Lubba Dub Dub"),
    TWIST("twist", "what_a_twist.mp3", "🔀", "Вот это поворот!"),
//    PIRATES("pirates", "pirates_of_the_caribbean.mp3", "☠", "pirates of the caribbean"),
//    PISTOLETOV("pistoletov", "pistoletov.mp3", "🦜", "Я стал новым пиратом (Пистолетов)"),
//    PERKELE("perkele", "ahti_perkele.mp3", "🇫🇮", "'Перкеле' Ахти из Control"),
//    BALLS("balls", "chef_chocolate_salty_balls.mp3", "👨🏿‍🍳", "Соленые шоколадные яица Шефа (South Park)"),
//    MAGIC("magic", "witcher_black_magic.mp3", "🧙", "Колдун ебучий! (Witcher)"),
//    DOH("doh", "doh.mp3", "🤬", "Homer Simpsons Doh!"),
    ;

    private final String name;
    private final String file;
    private final String emoji;
    private final String description;

    @NotNull
    public static BotReactButton findByName(String name) {
        for (BotReactButton command : values()) {
            if (command.name.equals(name)) {
                return command;
            }
        }
        throw new HomerException("React [" + name + "] not found");
    }

    public static boolean exist(String name) { // TODO name + переделать вообще этот мусор
        BotReactButton botReactButton1 = Arrays.stream(values())
                .filter(botReactButton -> botReactButton.getName().equals(name))
                .findFirst()
                .orElseGet(null);
        return botReactButton1 != null;
    }
}
