package org.ejectfb.balda.game;

import java.util.List;

public class WordValidator {

    public static final String RUSSIAN_LETTERS_REGEX = "^[а-яА-Я]{5}$";

    public static boolean isValidStartWord(String word) {
        return word != null && word.matches(RUSSIAN_LETTERS_REGEX);
    }

    public static boolean isValidWord(char[][] grid, int x, int y, char letter, String word,
                                      List<String> serverWords, List<String> clientWords) {
        if (serverWords.contains(word) || clientWords.contains(word)) {
            return false;
        }

        if (word.length() > countLettersOnGrid(grid) + 1) { // +1 для новой буквы
            return false;
        }

        return isWordFormable(grid, x, y, letter, word);
    }

    private static int countLettersOnGrid(char[][] grid) {
        int count = 0;
        for (char[] row : grid) {
            for (char c : row) {
                if (c != ' ') {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean isWordFormable(char[][] grid, int x, int y, char letter, String word) {
        char[][] tempGrid = copyGrid(grid);
        tempGrid[x][y] = letter;

        if (!containsAllLetters(tempGrid, word)) {
            return false;
        }

        return canFormWord(tempGrid, word);
    }

    private static char[][] copyGrid(char[][] original) {
        char[][] copy = new char[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }

    private static boolean containsAllLetters(char[][] grid, String word) {
        for (char c : word.toCharArray()) {
            boolean found = false;
            for (char[] row : grid) {
                for (char gridChar : row) {
                    if (gridChar == c) {
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
            if (!found) return false;
        }
        return true;
    }

    private static boolean canFormWord(char[][] grid, String word) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == word.charAt(0)) {
                    boolean[][] visited = new boolean[grid.length][grid[0].length];
                    if (dfs(grid, visited, i, j, word, 0)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean dfs(char[][] grid, boolean[][] visited,
                               int x, int y, String word, int index) {
        if (index == word.length()) {
            return true;
        }

        if (x < 0 || x >= grid.length || y < 0 || y >= grid[0].length ||
                visited[x][y] || grid[x][y] != word.charAt(index)) {
            return false;
        }

        visited[x][y] = true;

        boolean found = dfs(grid, visited, x+1, y, word, index+1) ||
                dfs(grid, visited, x-1, y, word, index+1) ||
                dfs(grid, visited, x, y+1, word, index+1) ||
                dfs(grid, visited, x, y-1, word, index+1);

        visited[x][y] = false;
        return found;
    }
}