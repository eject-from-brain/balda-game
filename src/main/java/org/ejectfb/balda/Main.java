package org.ejectfb.balda;

import org.ejectfb.balda.game.BaldaApp;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        // Отключаем подробные логи JavaFX
        Logger.getLogger("javafx").setLevel(Level.WARNING);

        // Настраиваем наши логи
        Logger rootLogger = Logger.getLogger("org.ejectfb.balda");
        rootLogger.setLevel(Level.ALL);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        rootLogger.addHandler(handler);

        // Запуск приложения
        BaldaApp.main(args);
    }
}