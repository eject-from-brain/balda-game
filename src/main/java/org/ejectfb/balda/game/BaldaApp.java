package org.ejectfb.balda.game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
        NetworkService networkService = new ServerNetworkService(game);
        networkService.connect("localhost");

        GameUI gameUI = new GameUI(game, networkService, true, gameSaver);

        Scene scene = new Scene(gameUI.getRoot(), 800, 700);
        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Балда (Сервер) - " + game.getGameName());
        primaryStage.show();
    }

    private void startClient(Stage primaryStage) {
        TextInputDialog dialog = new TextInputDialog("localhost");
        dialog.setTitle("Подключение к серверу");
        dialog.setHeaderText("Введите IP адрес сервера");
        dialog.setContentText("IP:");
        styleDialog(dialog.getDialogPane());

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(ip -> {
            try {
                NetworkService networkService = new ClientNetworkService();
                networkService.connect(ip);

                // Создаем временную игру, которая будет заменена при получении данных от сервера
                BaldaGame tempGame = new BaldaGame("играем против " + ip, "балда");
                GameUI gameUI = new GameUI(tempGame, networkService, false, null);

                // Устанавливаем слушатель для получения обновлений от сервера
                networkService.setGameStateListener(gameState -> {
                    Platform.runLater(() -> {
                        gameUI.updateGameState(gameState);
                    });
                });

                Scene scene = new Scene(gameUI.getRoot(), 800, 700);
                scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
                primaryStage.setScene(scene);
                primaryStage.setTitle("Балда (Клиент)");
                primaryStage.show();
            } catch (IOException e) {
                showErrorAlert("Ошибка подключения", "Не удалось подключиться к серверу", e.getMessage());
            }
        });
    }

    private void showServerGameSelection(Stage primaryStage) {
        List<String> savedGames = gameSaver.getSavedGames();

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Новая игра", savedGames);
        dialog.setTitle("Выбор игры");
        dialog.setHeaderText("Выберите существующую игру или создайте новую");
        dialog.setContentText("Игра:");
        styleDialog(dialog.getDialogPane());

        dialog.showAndWait().ifPresent(gameName -> {
            try {
                BaldaGame game;
                if (gameName.equals("Новая игра")) {
                    TextInputDialog nameDialog = new TextInputDialog("Игра1");
                    nameDialog.setTitle("Новая игра");
                    nameDialog.setHeaderText("Введите название новой игры");
                    nameDialog.setContentText("Название:");
                    styleDialog(nameDialog.getDialogPane());

                    Optional<String> nameResult = nameDialog.showAndWait();
                    if (nameResult.isPresent()) {
                        TextInputDialog wordDialog = new TextInputDialog("балда");
                        wordDialog.setTitle("Стартовое слово");
                        wordDialog.setHeaderText("Введите стартовое слово (5 русских букв)");

                        TextField inputField = wordDialog.getEditor();
                        Label hintLabel = new Label("Только 5 русских букв");
                        hintLabel.getStyleClass().add("hint-label");
                        wordDialog.getDialogPane().setContent(new VBox(5,
                                new Label("Слово:"), inputField, hintLabel));
                        styleDialog(wordDialog.getDialogPane());

                        Button okButton = (Button) wordDialog.getDialogPane().lookupButton(ButtonType.OK);
                        okButton.setDisable(true);

                        inputField.textProperty().addListener((observable, oldValue, newValue) -> {
                            okButton.setDisable(!WordValidator.isValidStartWord(newValue));
                        });

                        Optional<String> wordResult = wordDialog.showAndWait();
                        if (wordResult.isPresent() && WordValidator.isValidStartWord(wordResult.get())) {
                            String word = wordResult.get().toLowerCase();
                            game = new BaldaGame(nameResult.get(), word);
                        } else {
                            showErrorAlert("Ошибка", "Слово должно содержать ровно 5 русских букв", "");
                            return;
                        }
                    } else {
                        return;
                    }
                } else {
                    game = gameSaver.loadGame(gameName);
                    if (game == null) {
                        showErrorAlert("Ошибка", "Не удалось загрузить игру", "");
                        return;
                    }

                    if (!WordValidator.isValidStartWord(game.getStartWord())) {
                        showErrorAlert("Ошибка", "Некорректное стартовое слово в сохраненной игре", "");
                        return;
                    }
                }
                startServer(primaryStage, game);
            } catch (IOException e) {
                showErrorAlert("Ошибка", "Не удалось создать игру", e.getMessage());
            } catch (IllegalArgumentException e) {
                showErrorAlert("Ошибка", "Некорректные параметры игры", e.getMessage());
            }
        });
    }

    private void styleDialog(DialogPane dialogPane) {
        dialogPane.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialog(alert.getDialogPane());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}