package org.ejectfb.balda;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GameSaver {
    private static final String SAVES_DIR = "saves";

    public GameSaver() {
        // Создание папки saves, если её нет
        new File(SAVES_DIR).mkdirs();
    }

    public void saveGame(BaldaGame game, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVES_DIR + File.separator + filename + ".balda"))) {
            oos.writeObject(game);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BaldaGame loadGame(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(SAVES_DIR + File.separator + filename + ".balda"))) {
            return (BaldaGame) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getSavedGames() {
        File[] files = new File(SAVES_DIR).listFiles((dir, name) -> name.endsWith(".balda"));
        if (files == null) return Collections.emptyList();

        return Arrays.stream(files)
                .map(f -> f.getName().replace(".balda", ""))
                .collect(Collectors.toList());
    }
}