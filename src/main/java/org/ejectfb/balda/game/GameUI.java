package org.ejectfb.balda.game;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
        root.getStyleClass().add("game-container");
        root.setAlignment(Pos.TOP_CENTER);

        Text title = new Text("БАЛДА");
        title.getStyleClass().add("game-title");

        Text gameInfo = new Text("Игра: " + game.getGameName());
        gameInfo.getStyleClass().add("game-info");

        playerInfo = new Text();
        playerInfo.getStyleClass().add("player-info");

        VBox textContainer = new VBox(5, title, gameInfo, playerInfo);
        textContainer.setAlignment(Pos.CENTER);

        gameGrid = new GridPane();
        gameGrid.setHgap(8);
        gameGrid.setVgap(8);
        gameGrid.setAlignment(Pos.CENTER);

        VBox myWordsBox = new VBox(5);
        Text myWordsLabel = new Text(isServer ? "Мои слова" : "Слова противника");
        myWordsLabel.getStyleClass().add("words-label");
        serverWordsList = new ListView<>();
        serverWordsList.getStyleClass().add("word-list");
        myWordsBox.getChildren().addAll(myWordsLabel, serverWordsList);
        myWordsBox.setAlignment(Pos.CENTER);

        VBox opponentWordsBox = new VBox(5);
        Text opponentWordsLabel = new Text(isServer ? "Слова противника" : "Мои слова");
        opponentWordsLabel.getStyleClass().add("words-label");
        clientWordsList = new ListView<>();
        clientWordsList.getStyleClass().add("word-list");
        opponentWordsBox.getChildren().addAll(opponentWordsLabel, clientWordsList);
        opponentWordsBox.setAlignment(Pos.CENTER);

        HBox wordsPanel = new HBox(20, isServer ? myWordsBox : opponentWordsBox,
                isServer ? opponentWordsBox : myWordsBox);
        wordsPanel.setAlignment(Pos.CENTER);

        HBox buttonPanel = new HBox();
        buttonPanel.setAlignment(Pos.CENTER);
        if (isServer || gameSaver != null) {
            Button saveButton = new Button("💾 Сохранить");
            saveButton.getStyleClass().add("save-button");
            saveButton.setOnAction(e -> {
                if (gameSaver != null) {
                    gameSaver.saveGame(game);
                    showAlert("Информация", "Игра сохранена");
                }
            });
            buttonPanel.getChildren().add(saveButton);
        }

        Text watermark = new Text("made by EjectFB");
        watermark.getStyleClass().add("watermark");
        StackPane.setAlignment(watermark, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(watermark, new Insets(0, 10, 10, 0));

        StackPane mainContainer = new StackPane();
        mainContainer.getChildren().addAll(root, watermark);
        root.getChildren().addAll(textContainer, gameGrid, wordsPanel, buttonPanel);
        updateUI();
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
                cell.getStyleClass().add("grid-cell");

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

    public void updateGameState(BaldaGame newGameState) {
        this.game = newGameState;
        updateUI();
    }

    public Pane getRoot() {
        StackPane mainContainer = new StackPane();
        Text watermark = new Text("made by EjectFB");
        watermark.getStyleClass().add("watermark");
        StackPane.setAlignment(watermark, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(watermark, new Insets(0, 10, 10, 0));

        mainContainer.getChildren().addAll(root, watermark);
        return mainContainer;
    }
}