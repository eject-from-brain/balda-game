package org.ejectfb.balda.game;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import org.ejectfb.balda.mode.ModeSelectionView;
import org.ejectfb.balda.mode.ModeSelector;
import org.ejectfb.balda.network.ClientNetworkService;
import org.ejectfb.balda.network.NetworkService;
import org.ejectfb.balda.network.ServerNetworkService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class BaldaApp extends Application {

    private GameSaver gameSaver = new GameSaver();

    @Override
    public void start(Stage primaryStage) {
        ModeSelectionView modeSelection = new ModeSelectionView(mode -> {
            Platform.runLater(() -> {
                try {
                    if (mode == ModeSelector.Mode.SERVER) {
                        showServerGameSelection(primaryStage);
                    } else {
                        startClient(primaryStage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        modeSelection.show();

        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    private void startServer(Stage primaryStage, BaldaGame game) throws IOException {
        NetworkService networkService = new ServerNetworkService();
        networkService.connect("localhost");

        GameUI gameUI = new GameUI(game, networkService, true, gameSaver);

        Scene scene = new Scene(gameUI.getRoot(), 700, 700); // Увеличим размер для отображения слов
        primaryStage.setScene(scene);
        primaryStage.setTitle("Балда (Сервер) - " + game.getGameName());
        primaryStage.show();
    }

    private void startClient(Stage primaryStage) {
        TextInputDialog dialog = new TextInputDialog("localhost");
        dialog.setTitle("Подключение к серверу");
        dialog.setHeaderText("Введите IP адрес сервера");
        dialog.setContentText("IP:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(ip -> {
            try {
                NetworkService networkService = new ClientNetworkService();
                networkService.connect(ip);

                String clientGameName = "Игра с: " + ip;
                BaldaGame game = new BaldaGame(clientGameName);
                GameUI gameUI = new GameUI(game, networkService, false, null);

                Scene scene = new Scene(gameUI.getRoot(), 600, 600);
                primaryStage.setScene(scene);
                primaryStage.setTitle("Балда (Клиент)");
                primaryStage.show();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка подключения");
                alert.setHeaderText("Не удалось подключиться к серверу");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });
    }

    private void showServerGameSelection(Stage primaryStage) {
        List<String> savedGames = gameSaver.getSavedGames();

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Новая игра", savedGames);
        dialog.setTitle("Выбор игры");
        dialog.setHeaderText("Выберите существующую игру или создайте новую");
        dialog.setContentText("Игра:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(gameName -> {
            try {
                BaldaGame game;
                if (gameName.equals("Новая игра")) {
                    TextInputDialog nameDialog = new TextInputDialog("Игра1");
                    nameDialog.setTitle("Новая игра");
                    nameDialog.setHeaderText("Введите название новой игры");
                    nameDialog.setContentText("Название:");

                    Optional<String> nameResult = nameDialog.showAndWait();
                    if (nameResult.isPresent()) {
                        game = new BaldaGame(nameResult.get());
                    } else {
                        return;
                    }
                } else {
                    game = gameSaver.loadGame(gameName);
                    if (game == null) {
                        new Alert(Alert.AlertType.ERROR, "Не удалось загрузить игру", ButtonType.OK).showAndWait();
                        return;
                    }
                }
                startServer(primaryStage, game);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}