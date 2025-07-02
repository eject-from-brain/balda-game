package org.ejectfb.balda.game;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.ejectfb.balda.network.NetworkService;

import java.util.Optional;
import java.util.logging.Logger;

public class GameUI {
    private static final Logger logger = Logger.getLogger(GameUI.class.getName());

    private GridPane gameGrid;
    private BaldaGame game;
    private NetworkService networkService;
    private VBox root;
    private GameSaver gameSaver;
    private boolean isServer;
    private ListView<String> serverWordsList;
    private ListView<String> clientWordsList;
    private Text playerInfo;

    public GameUI(BaldaGame game, NetworkService networkService, boolean isServer, GameSaver gameSaver) {
        this.game = game;
        this.networkService = networkService;
        this.isServer = isServer;
        this.gameSaver = gameSaver;

        if (networkService != null) {
            networkService.setGameStateListener(gameState -> {
                logger.info("Получено новое состояние игры");
                this.game = gameState;
                updateUI();
            });
        }

        initializeUI();
    }

    private void initializeUI() {
        root = new VBox(10);
        root.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 20;");
        root.setAlignment(Pos.TOP_CENTER);

        // Заголовок и информация об игре
        Text title = new Text("БАЛДА");
        title.setStyle("-fx-font-size: 42px; -fx-fill: #FFA726; -fx-font-family: 'Segoe UI Semibold', 'Roboto Medium', sans-serif;");

        Text gameInfo = new Text("Игра: " + game.getGameName());
        gameInfo.setStyle("-fx-font-size: 20px; -fx-fill: #aaaaaa; -fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");

        playerInfo = new Text();
        playerInfo.setStyle("-fx-font-size: 18px; -fx-fill: #e0e0e0; -fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");

        VBox textContainer = new VBox(5, title, gameInfo, playerInfo);
        textContainer.setAlignment(Pos.CENTER);

        // Игровое поле
        gameGrid = new GridPane();
        gameGrid.setHgap(8);
        gameGrid.setVgap(8);
        gameGrid.setAlignment(Pos.CENTER);

        // Создаем блоки для списков слов
        VBox myWordsBox = new VBox(5);
        Text myWordsLabel = new Text(isServer ? "Мои слова" : "Слова противника");
        myWordsLabel.setStyle("-fx-font-size: 16px; -fx-fill: white; -fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");
        serverWordsList = createStyledListView();
        myWordsBox.getChildren().addAll(myWordsLabel, serverWordsList);
        myWordsBox.setAlignment(Pos.CENTER);

        VBox opponentWordsBox = new VBox(5);
        Text opponentWordsLabel = new Text(isServer ? "Слова противника" : "Мои слова");
        opponentWordsLabel.setStyle("-fx-font-size: 16px; -fx-fill: white; -fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");
        clientWordsList = createStyledListView();
        opponentWordsBox.getChildren().addAll(opponentWordsLabel, clientWordsList);
        opponentWordsBox.setAlignment(Pos.CENTER);

        HBox wordsPanel = new HBox(20, isServer ? myWordsBox : opponentWordsBox,
                isServer ? opponentWordsBox : myWordsBox);
        wordsPanel.setAlignment(Pos.CENTER);

        // Кнопка сохранения
        HBox buttonPanel = new HBox();
        buttonPanel.setAlignment(Pos.CENTER);
        if (isServer || gameSaver != null) {
            Button saveButton = createStyledButton("💾 Сохранить");
            saveButton.setStyle("-fx-background-color: #FFA726; -fx-text-fill: #1a1a1a; -fx-font-weight: bold;");
            saveButton.setOnAction(e -> {
                if (gameSaver != null) {
                    gameSaver.saveGame(game);
                    showAlert("Информация", "Игра сохранена");
                }
            });
            buttonPanel.getChildren().add(saveButton);
        }

        // Скрытая подпись
        Text watermark = new Text("made by EjectFB");
        watermark.setStyle("-fx-font-size: 10px; -fx-fill: #333; -fx-font-style: italic;");
        StackPane.setAlignment(watermark, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(watermark, new Insets(0, 10, 10, 0));

        StackPane mainContainer = new StackPane();
        mainContainer.getChildren().addAll(root, watermark);
        root.getChildren().addAll(textContainer, gameGrid, wordsPanel, buttonPanel);
        updateUI();
    }

    private ListView<String> createStyledListView() {
        ListView<String> listView = new ListView<>();
        listView.setPrefWidth(250);
        listView.setPrefHeight(150);
        listView.setStyle("-fx-control-inner-background: #2d2d2d; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-border-color: #3d3d3d; " +
                "-fx-border-radius: 5; " +
                "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");
        return listView;
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #2d2d2d; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 8 16; " +
                "-fx-background-radius: 5; " +
                "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #FFA726; " +
                "-fx-text-fill: #1a1a1a; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 8 16; " +
                "-fx-background-radius: 5; " +
                "-fx-font-weight: bold;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #2d2d2d; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 8 16; " +
                "-fx-background-radius: 5; " +
                "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;"));
        return btn;
    }

    private void updateUI() {
        updateGrid();
        updateWordsLists();

        if (isServer) {
            playerInfo.setText(game.isServerTurn() ? "▶ Мой ход" : "⏸ Ход противника");
        } else {
            playerInfo.setText(game.isServerTurn() ? "⏸ Ход противника" : "▶ Мой ход");
        }
    }

    private void updateGrid() {
        gameGrid.getChildren().clear();

        for (int i = 0; i < game.getGridSize(); i++) {
            for (int j = 0; j < game.getGridSize(); j++) {
                char letter = game.getLetterAt(i, j);
                Button cell = new Button(letter == ' ' ? "" : String.valueOf(letter));
                cell.setMinSize(50, 50);
                cell.setStyle("-fx-font-size: 18px; " +
                        "-fx-background-color: #2d2d2d; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: #3d3d3d; " +
                        "-fx-border-radius: 3; " +
                        "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");

                cell.setOnMouseEntered(e -> {
                    if (cell.getText().isEmpty()) {
                        cell.setStyle("-fx-font-size: 18px; " +
                                "-fx-background-color: #FFA726; " +
                                "-fx-text-fill: #1a1a1a; " +
                                "-fx-border-color: #FFA726; " +
                                "-fx-border-radius: 3; " +
                                "-fx-font-weight: bold;");
                    }
                });
                cell.setOnMouseExited(e -> {
                    cell.setStyle("-fx-font-size: 18px; " +
                            "-fx-background-color: #2d2d2d; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-color: #3d3d3d; " +
                            "-fx-border-radius: 3; " +
                            "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");
                });

                int finalI = i;
                int finalJ = j;
                cell.setOnAction(e -> handleCellClick(finalI, finalJ));
                gameGrid.add(cell, j, i);
            }
        }
    }

    private void updateWordsLists() {
        if (isServer) {
            serverWordsList.getItems().setAll(game.getServerWords());
            clientWordsList.getItems().setAll(game.getClientWords());
        } else {
            // На клиенте меняем местами отображение слов
            serverWordsList.getItems().setAll(game.getClientWords());
            clientWordsList.getItems().setAll(game.getServerWords());
        }
    }

    private void handleCellClick(int x, int y) {
        if (!game.canMakeMove(isServer)) {
            showAlert("Ошибка", "Сейчас не ваш ход!");
            return;
        }
        if (game.getLetterAt(x, y) != ' ') {
            showAlert("Ошибка", "Эта клетка уже занята!");
            return;
        }

        TextInputDialog letterDialog = new TextInputDialog();
        letterDialog.setTitle("Ваш ход");
        letterDialog.setHeaderText("Введите букву");
        letterDialog.setContentText("Буква:");
        styleDialog(letterDialog.getDialogPane());

        Optional<String> letterResult = letterDialog.showAndWait();
        if (letterResult.isPresent() && letterResult.get().length() == 1) {
            char letter = letterResult.get().toUpperCase().charAt(0);

            TextInputDialog wordDialog = new TextInputDialog();
            wordDialog.setTitle("Ваш ход");
            wordDialog.setHeaderText("Введите слово");
            wordDialog.setContentText("Слово:");
            styleDialog(wordDialog.getDialogPane());

            Optional<String> wordResult = wordDialog.showAndWait();
            if (wordResult.isPresent() && !wordResult.get().isEmpty()) {
                String word = wordResult.get().toUpperCase();

                if (game.makeMove(x, y, letter, word)) {
                    updateUI();
                    if (networkService != null) {
                        networkService.sendGameState(game);
                    }
                } else {
                    showAlert("Ошибка", "Недопустимый ход!");
                }
            }
        }
    }

    private void styleDialog(DialogPane dialogPane) {
        dialogPane.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setTitle(title);
        styleDialog(alert.getDialogPane());
        alert.showAndWait();
    }

    public Pane getRoot() {
        // Возвращаем mainContainer вместо root
        StackPane mainContainer = new StackPane();
        Text watermark = new Text("made by EjectFB");
        watermark.setStyle("-fx-font-size: 10px; -fx-fill: #333; -fx-font-style: italic;");
        StackPane.setAlignment(watermark, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(watermark, new Insets(0, 10, 10, 0));

        mainContainer.getChildren().addAll(root, watermark);
        return mainContainer;
    }
}