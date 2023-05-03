package com.homer.util;

import com.homer.exception.HomerException;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@UtilityClass
public class HomerUtil {

    @NotNull
    public static InputStream getFile(@NotNull String introFilename) {
        InputStream audioStream;
        try {
            audioStream = new FileInputStream(introFilename);
        } catch (FileNotFoundException e) {
            throw new HomerException("File upload error", e);
        }
        return audioStream;
    }
}
