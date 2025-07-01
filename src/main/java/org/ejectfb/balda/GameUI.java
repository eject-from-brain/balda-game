package org.ejectfb.balda;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Optional;

public class GameUI {
    private GridPane gameGrid;
    private BaldaGame game;
    private NetworkService networkService;
    private VBox root;

    public GameUI(BaldaGame game, NetworkService networkService) {
        this.game = game;
        this.networkService = networkService;
        initializeUI();
    }

    private void initializeUI() {
        root = new VBox(10);
        gameGrid = new GridPane();
        gameGrid.setHgap(5);
        gameGrid.setVgap(5);

        // Добавляем информацию о текущем игроке
        Text playerInfo = new Text("Ход игрока: " + (game.getCurrentPlayer() + 1));

        // Добавляем кнопку обновления
        Button refreshButton = new Button("Обновить");
        refreshButton.setOnAction(e -> updateGrid());

        root.getChildren().addAll(playerInfo, refreshButton, gameGrid);
        updateGrid();
    }

    public void updateGrid() {
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

    private void handleCellClick(int x, int y) {
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
                    // Если ход успешен, обновляем UI и отправляем состояние на сервер
                    updateGrid();
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