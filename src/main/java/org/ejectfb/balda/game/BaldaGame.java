package org.ejectfb.balda.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BaldaGame implements Serializable {
    private char[][] grid;
    private List<String> usedWords;
    private String currentWord;
    private int currentPlayer;
    private int gridSize = 7; // Начальный размер

    public BaldaGame() {
        grid = new char[gridSize][gridSize];
        usedWords = new ArrayList<>();
        initializeGrid();
    }

    private void initializeGrid() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                grid[i][j] = ' ';
            }
        }
    }

    // Новые геттеры
    public int getGridSize() {
        return gridSize;
    }

    public char getLetterAt(int x, int y) {
        if (x >= 0 && x < gridSize && y >= 0 && y < gridSize) {
            return grid[x][y];
        }
        return ' '; // или можно бросить исключение
    }

    public List<String> getUsedWords() {
        return new ArrayList<>(usedWords);
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public boolean makeMove(int x, int y, char letter, String word) {
        if (!isValidMove(x, y, letter, word)) {
            return false;
        }

        grid[x][y] = letter;
        usedWords.add(word);
        currentWord = word;

        expandGridIfNeeded(x, y);
        currentPlayer = (currentPlayer + 1) % 2;

        return true;
    }

    private boolean isValidMove(int x, int y, char letter, String word) {
        if (x < 0 || x >= gridSize || y < 0 || y >= gridSize) {
            return false;
        }
        if (grid[x][y] != ' ') return false;
        if (usedWords.contains(word)) return false;
        // Дополнительные проверки по правилам Балды
        return true;
    }

    private void expandGridIfNeeded(int x, int y) {
        boolean expandTop = x == 0;
        boolean expandBottom = x == gridSize - 1;
        boolean expandLeft = y == 0;
        boolean expandRight = y == gridSize - 1;

        if (expandTop || expandBottom || expandLeft || expandRight) {
            int newSize = gridSize + 1;
            char[][] newGrid = new char[newSize][newSize];

            // Инициализация новой сетки
            for (int i = 0; i < newSize; i++) {
                for (int j = 0; j < newSize; j++) {
                    newGrid[i][j] = ' ';
                }
            }

            // Копирование старой сетки
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
}