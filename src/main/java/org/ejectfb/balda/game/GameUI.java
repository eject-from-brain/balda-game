package org.ejectfb.balda.game;

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
        gameGrid = new GridPane();
        gameGrid.setHgap(5);
        gameGrid.setVgap(5);

        HBox infoPanel = new HBox(10);
        playerInfo = new Text("Ход игрока: " + (game.getCurrentPlayer() + 1)); // Инициализируем поле
        Text gameInfo = new Text("Игра: " + game.getGameName());
        infoPanel.getChildren().addAll(playerInfo, gameInfo);

        HBox wordsPanel = new HBox(10);
        serverWordsList = new ListView<>();
        serverWordsList.setPrefWidth(200);
        serverWordsList.setPlaceholder(new Label("Слова сервера"));

        clientWordsList = new ListView<>();
        clientWordsList.setPrefWidth(200);
        clientWordsList.setPlaceholder(new Label("Слова клиента"));

        wordsPanel.getChildren().addAll(serverWordsList, clientWordsList);

        HBox buttonPanel = new HBox(10);
        Button refreshButton = new Button("Обновить");
        refreshButton.setOnAction(e -> updateUI());

        Button saveButton = new Button("Сохранить");
        saveButton.setOnAction(e -> {
            gameSaver.saveGame(game);
            new Alert(Alert.AlertType.INFORMATION, "Игра сохранена", ButtonType.OK).showAndWait();
        });

        buttonPanel.getChildren().addAll(refreshButton, saveButton);

        root.getChildren().addAll(infoPanel, gameGrid, wordsPanel, buttonPanel);
        updateUI();
    }

    private void updateUI() {
        updateGrid();
        updateWordsLists();

        if (isServer) {
            playerInfo.setText(game.isServerTurn() ? "Ваш ход (Сервер)" : "Ход клиента");
        } else {
            playerInfo.setText(game.isServerTurn() ? "Ход сервера" : "Ваш ход (Клиент)");
        }
    }

    private void updateGrid() {
        gameGrid.getChildren().clear();

        for (int i = 0; i < game.getGridSize(); i++) {
            for (int j = 0; j < game.getGridSize(); j++) {
                char letter = game.getLetterAt(i, j);
                Button cell = new Button(letter == ' ' ? "" : String.valueOf(letter));
                cell.setMinSize(40, 40);

                int finalI = i;
                int finalJ = j;
                cell.setOnAction(e -> handleCellClick(finalI, finalJ));
                gameGrid.add(cell, j, i);
            }
        }
    }

    private void updateWordsLists() {
        serverWordsList.getItems().setAll(game.getServerWords());
        clientWordsList.getItems().setAll(game.getClientWords());
    }

    private void handleCellClick(int x, int y) {
        if (!game.canMakeMove(isServer)) {
            showAlert("Сейчас не ваш ход!");
            return;
        }
        if (game.getLetterAt(x, y) != ' ') {
            showAlert("Эта клетка уже занята!");
            return;
        }

        TextInputDialog letterDialog = new TextInputDialog();
        letterDialog.setTitle("Ваш ход");
        letterDialog.setHeaderText("Введите букву");
        letterDialog.setContentText("Буква:");

        Optional<String> letterResult = letterDialog.showAndWait();
        if (letterResult.isPresent() && letterResult.get().length() == 1) {
            char letter = letterResult.get().toUpperCase().charAt(0);

            TextInputDialog wordDialog = new TextInputDialog();
            wordDialog.setTitle("Ваш ход");
            wordDialog.setHeaderText("Введите слово");
            wordDialog.setContentText("Слово:");

            Optional<String> wordResult = wordDialog.showAndWait();
            if (wordResult.isPresent() && !wordResult.get().isEmpty()) {
                String word = wordResult.get().toUpperCase();

                if (game.makeMove(x, y, letter, word)) {
                    updateGrid();
                    updateUI();
                    if (networkService != null) {
                        networkService.sendGameState(game);
                    }
                } else {
                    showAlert("Недопустимый ход!");
                }
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.showAndWait();
    }

    public Pane getRoot() {
        return root;
    }
}