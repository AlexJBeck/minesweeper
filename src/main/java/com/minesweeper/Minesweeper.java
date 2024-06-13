package com.minesweeper;

import java.util.Random;

public class Minesweeper {

    private final int size;
    private final int numMines;
    private final String[][] board;
    private final boolean[][] revealed;
    private final Random random = new Random();

    public Minesweeper(Database database, String username, int size, int numMines) {
        this.size = size;
        this.numMines = numMines;
        this.board = new String[size][size];
        this.revealed = new boolean[size][size];

        initializeBoard();
        placeMines();
        calculateNumbers();
    }

    private void initializeBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = " ";
            }
        }
    }

    private void placeMines() {
        int minesPlaced = 0;
        while (minesPlaced < numMines) {
            int x = random.nextInt(size);
            int y = random.nextInt(size);
            if (!board[x][y].equals("X")) {
                board[x][y] = "X";
                minesPlaced++;
            }
        }
    }

    private void calculateNumbers() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (!board[i][j].equals("X")) {
                    int count = countAdjacentMines(i, j);
                    if (count > 0) {
                        board[i][j] = String.valueOf(count);
                    }
                }
            }
        }
    }

    private int countAdjacentMines(int x, int y) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (isValidCell(nx, ny) && board[nx][ny].equals("X")) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isValidCell(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public boolean isMine(int x, int y) {
        return board[x][y].equals("X");
    }

    public String getCell(int x, int y) {
        return board[x][y];
    }

    public boolean reveal(int x, int y) {
        if (revealed[x][y]) {
            return false; // Already revealed
        }
        revealed[x][y] = true;
        return true;
    }

    public boolean isRevealed(int x, int y) {
        return revealed[x][y];
    }

    public boolean checkWin() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (!revealed[i][j] && !board[i][j].equals("X")) {
                    return false;
                }
            }
        }
        return true;
    }

    public void openAdjacentCells(int x, int y) {

        // Iteriere über die 8 angrenzenden Felder (um das Feld x, y)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                // Überprüfe, ob das Feld gültig ist und noch nicht geöffnet wurde
                if (isValidCell(nx, ny) && !isRevealed(nx, ny) && getCell(nx, ny).equals("0")) {
                        openAdjacentCells(nx, ny);
                    }
                }
            }
        }
    }