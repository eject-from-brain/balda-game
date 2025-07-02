package org.ejectfb.balda.game;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GameSaver {
    private static final String SAVES_DIR = System.getProperty("user.home") + File.separator + "balda-game";

    public GameSaver() {
        try {
            Files.createDirectories(Paths.get(SAVES_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveGame(BaldaGame game) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVES_DIR + File.separator + game.getGameName() + ".balda"))) {
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