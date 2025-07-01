package org.ejectfb.balda;

import org.ejectfb.balda.game.BaldaApp;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    public static void main(String[] args) {
        Logger.getLogger("javafx").setLevel(Level.OFF); // Отключаем логи JavaFX

        Logger rootLogger = Logger.getLogger("org.ejectfb.balda");
        rootLogger.setLevel(Level.ALL);
        rootLogger.setUseParentHandlers(false);


        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter() {
            private static final String FORMAT = "[%s] %s: %s%n";
            @Override
            public synchronized String format(java.util.logging.LogRecord record) {
                return String.format(FORMAT, getNowTime(),
                        record.getLevel().getLocalizedName(),
                        record.getMessage()
                );
            }
        });

        rootLogger.addHandler(handler);
        BaldaApp.main(args);
    }

    private static String getNowTime() {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return currentTime.format(formatter);
    }
}