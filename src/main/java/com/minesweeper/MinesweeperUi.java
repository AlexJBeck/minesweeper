package com.minesweeper;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class MinesweeperUi extends Application {

    private static final int SIZE = 10;
    private static final int MINES = 10;
    private Minesweeper game;
    Database database = new Database();          //Todo how to click a Button to restart game without closeing window

    @Override
    public void start(Stage primaryStage) {
        String username = new String("Alex"); //TODO change to user input!

        game = new Minesweeper(database, username, SIZE, MINES);
        GridPane gridPane = createBoard();

        Scene scene = new Scene(gridPane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Minesweeper");
        primaryStage.show();
    }

    private GridPane createBoard() {
        GridPane gridPane = new GridPane();
        Button[][] buttons = new Button[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Button button = new Button();
                button.setMinWidth(30);
                button.setMinHeight(30);
                final int x = i;
                final int y = j;
                button.setOnAction((EventHandler<ActionEvent>) new ButtonHandler(x, y, button));
                buttons[i][j] = button;
                gridPane.add(button, j, i);
            }
        }
        return gridPane;
    }

    private class ButtonHandler implements EventHandler<ActionEvent> {
        private int x;
        private int y;
        private Button button;

        public ButtonHandler(int x, int y, Button button) {
            this.x = x;
            this.y = y;
            this.button = button;
        }

        //@Override
        public void handle(ActionEvent event) {
            if (game.reveal(x, y)) {
                button.setText(game.getCell(x, y));
                if (game.isMine(x, y)) {
                    database.incrementLosses("Alex"); //Todo change user input
                    gameOver();
                } else if (game.checkWin()) {
                    database.incrementWins("Alex");//Todo change user input
                    gameWon();
                }
            }
        }
    }

    private void gameOver() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("You hit a mine! Game over.");
        alert.showAndWait();
    }

    private void gameWon() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Congratulations");
        alert.setHeaderText(null);
        alert.setContentText("You've cleared the minefield! You win!");
        alert.showAndWait();
    }
}
