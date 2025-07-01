package org.ejectfb.balda.game;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import org.ejectfb.balda.mode.ModeSelectionView;
import org.ejectfb.balda.mode.ModeSelector;
import org.ejectfb.balda.network.ClientNetworkService;
import org.ejectfb.balda.network.NetworkService;
import org.ejectfb.balda.network.ServerNetworkService;

import java.io.IOException;
import java.util.Optional;

public class BaldaApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        ModeSelectionView modeSelection = new ModeSelectionView(mode -> {
            Platform.runLater(() -> {
                try {
                    if (mode == ModeSelector.Mode.SERVER) {
                        startServer(primaryStage);
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
            Platform.exit(); // Завершаем JavaFX
            System.exit(0);  // Завершаем процесс JVM
        });
    }

    private void startServer(Stage primaryStage) throws IOException {
        NetworkService networkService = new ServerNetworkService();
        networkService.connect("localhost");

        BaldaGame game = new BaldaGame();
        GameUI gameUI = new GameUI(game, networkService);

        Scene scene = new Scene(gameUI.getRoot(), 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Балда (Сервер)");
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

                BaldaGame game = new BaldaGame();
                GameUI gameUI = new GameUI(game, networkService);

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

    public static void main(String[] args) {
        launch(args);
    }
}