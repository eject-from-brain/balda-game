package org.ejectfb.balda.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BaldaGame implements Serializable {
    private char[][] grid;
    private List<String> serverWords;
    private List<String> clientWords;
    private String currentWord;
    private int currentPlayer;
    private int gridSize;
    private String gameName;
    private boolean isServerTurn = true;
    private boolean clientConnected = false;
    private String startWord;

    public BaldaGame(String gameName, String startWord) {
        this.gameName = gameName;
        this.startWord = startWord;
        this.serverWords = new ArrayList<>();
        this.clientWords = new ArrayList<>();
        initializeGrid();
    }

    private void initializeGrid() {
        if (!WordValidator.isValidStartWord(startWord)) {
            throw new IllegalArgumentException("Стартовое слово должно содержать ровно 5 русских букв");
        }

        gridSize = Math.max(startWord.length() + 2, 5); // Размер сетки = длина слова + 2, но не меньше 5
        grid = new char[gridSize][gridSize];

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                grid[i][j] = ' ';
            }
        }

        int centerRow = gridSize / 2;
        int centerCol = (gridSize - startWord.length()) / 2;

        for (int i = 0; i < startWord.length(); i++) {
            grid[centerRow][centerCol + i] = startWord.toUpperCase().charAt(i);
        }
    }

    public boolean canMakeMove(boolean isServer) {
        return (isServer && isServerTurn && clientConnected) ||
                (!isServer && !isServerTurn);
    }

    public boolean makeMove(int x, int y, char letter, String word) {
        if (!isValidMove(x, y, letter, word)) {
            return false;
        }

        grid[x][y] = letter;
        if (isServerTurn) {
            serverWords.add(word);
        } else {
            clientWords.add(word);
        }
        currentWord = word;

        expandGridIfNeeded(x, y);
        isServerTurn = !isServerTurn;
        currentPlayer = (currentPlayer + 1) % 2;

        return true;
    }

    private boolean isValidMove(int x, int y, char letter, String word) {
        if (x < 0 || x >= gridSize || y < 0 || y >= gridSize) {
            return false;
        }
        if (grid[x][y] != ' ') {
            return false;
        }

        return WordValidator.isValidWord(grid, x, y, letter, word, serverWords, clientWords);
    }

    private void expandGridIfNeeded(int x, int y) {
        boolean expandTop = x == 0;
        boolean expandBottom = x == gridSize - 1;
        boolean expandLeft = y == 0;
        boolean expandRight = y == gridSize - 1;

        if (expandTop || expandBottom || expandLeft || expandRight) {
            int newSize = gridSize + 1;
            char[][] newGrid = new char[newSize][newSize];

            for (int i = 0; i < newSize; i++) {
                for (int j = 0; j < newSize; j++) {
                    newGrid[i][j] = ' ';
                }
            }

            int offsetX = expandTop ? 1 : 0;
            int offsetY = expandLeft ? 1 : 0;

            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    newGrid[i + offsetX][j + offsetY] = grid[i][j];
                }
            }

            grid = newGrid;
            gridSize = newSize;
        }
    }

    public int getGridSize() {
        return gridSize;
    }

    public List<String> getServerWords() {
        return new ArrayList<>(serverWords);
    }

    public List<String> getClientWords() {
        return new ArrayList<>(clientWords);
    }

    public String getGameName() {
        return gameName;
    }

    public char getLetterAt(int x, int y) {
        if (x >= 0 && x < gridSize && y >= 0 && y < gridSize) {
            return grid[x][y];
        }
        return ' ';
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public void setClientConnected(boolean connected) {
        this.clientConnected = connected;
    }

    public boolean isClientConnected() {
        return clientConnected;
    }


    public boolean isServerTurn() {
        return isServerTurn;
    }

    public String getStartWord() {
        return startWord;
    }
}