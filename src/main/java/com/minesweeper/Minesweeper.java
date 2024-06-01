package com.minesweeper;

import java.util.Random;

public class Minesweeper {
    private int size;
    private int mines;
    private char[][] board;
    private boolean[][] minesGrid;
    private boolean[][] revealed;

    public Minesweeper(int size, int mines) {
        this.size = size;
        this.mines = mines;
        board = new char[size][size];
        minesGrid = new boolean[size][size];
        revealed = new boolean[size][size];
        initializeBoard();
        placeMines();
        calculateNumbers();
    }

    private void initializeBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = '0';
                revealed[i][j] = false;
            }
        }
    }

    private void placeMines() {
        Random random = new Random();
        int placedMines = 0;
        while (placedMines < mines) {
            int x = random.nextInt(size);
            int y = random.nextInt(size);
            if (!minesGrid[x][y]) {
                minesGrid[x][y] = true;
                placedMines++;
            }
        }
    }

    private void calculateNumbers() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (!minesGrid[i][j]) {
                    int count = 0;
                    for (int k = -1; k <= 1; k++) {
                        for (int l = -1; l <= 1; l++) {
                            if (isValid(i + k, j + l) && minesGrid[i + k][j + l]) {
                                count++;
                            }
                        }
                    }
                    board[i][j] = (char) ('0' + count);
                }
            }
        }
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public boolean reveal(int x, int y) {
        if (!isValid(x, y) || revealed[x][y]) return false;
        revealed[x][y] = true;
        return true;
    }

    public boolean isMine(int x, int y) {
        return minesGrid[x][y];
    }

    public String getCell(int x, int y) {
        return String.valueOf(board[x][y]);
    }

    public boolean checkWin() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (!minesGrid[i][j] && !revealed[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
