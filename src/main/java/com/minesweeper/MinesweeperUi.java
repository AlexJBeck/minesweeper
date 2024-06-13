package com.minesweeper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Optional;

public class MinesweeperUi extends Application {

    private static final int SIZE = 10;
    private static final int MINES = 10;
    private static String username = "unknown";
    private Minesweeper game;
    private Database database = new Database();
    private Stage primaryStage;
    private Label timerLabel;
    private long startTime;
    private boolean gameOver = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        TextInputDialog dialog = new TextInputDialog("unknown");
        dialog.setTitle("Username Input");
        dialog.setHeaderText("Enter your username:");
        Optional<String> result = dialog.showAndWait();
        username = result.orElse("unknown");

        game = new Minesweeper(database, username, SIZE, MINES);

        VBox root = new VBox(); // VBox für vertikale Anordnung

        // Erstellen des Menüs und Hinzufügen zur VBox
        MenuBar menuBar = createMenuBar();
        root.getChildren().add(menuBar);

        // HBox für Timer Label und Platzhalter, um rechtsbündige Ausrichtung zu erreichen
        HBox timerBox = new HBox();
        timerBox.setAlignment(Pos.CENTER_RIGHT);
        timerLabel = new Label("00:00");
        timerLabel.setStyle("-fx-font-size: 20;");
        timerBox.getChildren().add(timerLabel);
        HBox.setHgrow(timerLabel, Priority.ALWAYS); // Timer Label wächst horizontal
        root.getChildren().add(timerBox);

        // Spielfeld (GridPane) hinzufügen
        GridPane gridPane = createBoard();
        root.getChildren().add(gridPane);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Minesweeper");
        primaryStage.show();

        startTime = System.currentTimeMillis();
        startTimer();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Game Menu
        Menu gameMenu = new Menu("Game");
        MenuItem newGameItem = new MenuItem("New Game");
        newGameItem.setOnAction(event -> startNewGame());
        MenuItem changeDifficultyItem = new MenuItem("Change Difficulty");
        changeDifficultyItem.setOnAction(event -> changeDifficulty());
        MenuItem highScoreItem = new MenuItem("High Scores");
        highScoreItem.setOnAction(event -> showHighScores());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(event -> exitGame());
        gameMenu.getItems().addAll(newGameItem, changeDifficultyItem, highScoreItem, new SeparatorMenuItem(), exitItem);

        menuBar.getMenus().addAll(gameMenu);

        return menuBar;
    }

    private GridPane createBoard() {
        GridPane gridPane = new GridPane();
        Button[][] buttons = new Button[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Button button = new Button();
                button.setMinWidth(30);
                button.setMinHeight(30);
                button.setStyle("-fx-base: #CCCCCC;"); // Standardfarbe für geschlossene Felder (hellgrau)
                final int x = i;
                final int y = j;
                button.setOnAction(new ButtonHandler(x, y, button));
                buttons[i][j] = button;
                gridPane.add(button, j, i);
            }
        }
        return gridPane;
    }

    private void startNewGame() {
        game = new Minesweeper(database, username, SIZE, MINES);
        updateBoard();
        gameOver = false;
    }

    private void updateBoard() {
        // Clear board
        for (Node node : ((VBox) primaryStage.getScene().getRoot()).getChildren()) {
            if (node instanceof GridPane) {
                ((GridPane) node).getChildren().clear();
                break;
            }
        }

        // Create new board
        GridPane gridPane = createBoard();
        ((VBox) primaryStage.getScene().getRoot()).getChildren().add(2, gridPane); // Index 2 for board
    }

    private void changeDifficulty() {
        // Implement your logic to change difficulty
        System.out.println("Change difficulty option selected.");
    }

    private void showHighScores() {
        // Implement your logic to show high scores
        System.out.println("Show high scores option selected.");
    }

    private void exitGame() {
        Platform.exit();
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

        @Override
        public void handle(ActionEvent event) {
            if (!gameOver && !game.isRevealed(x, y)) {
                if (game.reveal(x, y)) {
                    button.setText(game.getCell(x, y));
                    button.setDisable(true); // Button nach dem Öffnen deaktivieren
                    setColorForCell(button, game.getCell(x, y));
                    // openAdjacentCells wird automatisch in reveal aufgerufen, keine explizite Notwendigkeit mehr
                    if (game.isMine(x, y)) {
                        database.incrementLosses(username);
                        gameOver = true;
                        gameOver();
                    } else if (game.checkWin()) {
                        database.incrementWins(username);
                        gameOver = true;
                        gameWon();
                    }
                }
            }
        }
    }

    private void gameOver() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("You hit a mine! Game over.");
        alert.showAndWait();
    }

    private void gameWon() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congratulations");
        alert.setHeaderText(null);
        alert.setContentText("You've cleared the minefield! You win!");
        alert.showAndWait();
    }

    private void startTimer() {
        new Thread(() -> {
            while (!gameOver) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long minutes = (elapsedTime / 1000) / 60;
                long seconds = (elapsedTime / 1000) % 60;
                String timeString = String.format("%02d:%02d", minutes, seconds);

                // Update GUI
                Platform.runLater(() -> timerLabel.setText(timeString));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setColorForCell(Button button, String cellValue) {
        button.setStyle("-fx-font-weight: bold; -fx-effect: null; -fx-font-size: 15px;");
        switch (cellValue) {
            case "1":
                button.setTextFill(Color.BLUE);
                break;
            case "2":
                button.setTextFill(Color.GREEN);
                break;
            case "3":
                button.setTextFill(Color.RED);
                break;
            case "4":
                button.setTextFill(Color.DARKBLUE);
                break;
            case "5":
                button.setTextFill(Color.DARKRED);
                break;
            default:
                button.setTextFill(Color.BLACK);
                break;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}