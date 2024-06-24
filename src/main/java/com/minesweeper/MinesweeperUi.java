package com.minesweeper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;

public class MinesweeperUi extends Application {

    private static int SIZE = 10;
    private static int MINES = 10;
    private static String username = "unknown";
    private Minesweeper game;
    private final Database database = new Database();
    private Label timerLabel;
    private Label minesLabel; // Label für die verbleibenden Minen
    private long startTime;
    private boolean gameOver = false;
    private Button[][] buttons;
    private Image flagImage;
    private Image hintImage;
    private Image mineImage;
    private Image happyImage;
    private Image sadImage;
    private ImageView smileyImageView;
    private int remainingMines = MINES; // Anzahl der verbleibenden Minen
    private Thread timerThread; // Timer-Thread als Instanzvariable

        @Override
    public void start(Stage primaryStage) {

            TextInputDialog dialog = new TextInputDialog("unknown");
        dialog.setTitle("Username Input");
        dialog.setHeaderText("Enter your username:");
        Optional<String> result = dialog.showAndWait();
        username = result.orElse("unknown");

        game = new Minesweeper(database, username, SIZE, MINES);

        VBox root = new VBox();

        MenuBar menuBar = createMenuBar();
        root.getChildren().add(menuBar);

        HBox infoBox = new HBox();
        infoBox.setAlignment(Pos.CENTER);

        // Laden der Bilder
        flagImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/flag.png")));
        hintImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hint.png")));
        mineImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/mine.png")));
            Image clockImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/clock.png")));
        happyImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/happy.png")));
        sadImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/sad.png")));

        ImageView mineImageView = new ImageView(mineImage);
        mineImageView.setFitWidth(20);
        mineImageView.setFitHeight(20);
        minesLabel = new Label(" " + remainingMines);
        minesLabel.setStyle("-fx-font-size: 20;");
        HBox minesBox = new HBox(mineImageView, minesLabel);
        minesBox.setAlignment(Pos.CENTER_LEFT);

        ImageView clockImageView = new ImageView(clockImage);
        clockImageView.setFitWidth(20);
        clockImageView.setFitHeight(20);
        timerLabel = new Label("00:00");
        timerLabel.setStyle("-fx-font-size: 20;");
        HBox timerBox = new HBox(timerLabel, clockImageView);
        timerBox.setAlignment(Pos.CENTER_RIGHT);

        smileyImageView = new ImageView(happyImage);
        smileyImageView.setFitWidth(30);
        smileyImageView.setFitHeight(30);
        smileyImageView.setOnMouseClicked(event -> startNewGame());

        // Align minesBox to the left, timerBox to the right, and smileyImageView in the center
        HBox leftBox = new HBox(minesBox);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        HBox rightBox = new HBox(timerBox);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        HBox centerBox = new HBox(smileyImageView);
        centerBox.setAlignment(Pos.CENTER);

        infoBox.getChildren().addAll(leftBox, centerBox, rightBox);
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        HBox.setHgrow(centerBox, Priority.ALWAYS);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
        root.getChildren().add(infoBox);

        GridPane gridPane = createBoard();
        root.getChildren().add(gridPane);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Minesweeper");
        primaryStage.show();

        startTime = System.currentTimeMillis();
        startTimer();

        updateBoard(); // Nach dem Laden der Bilder das Spielfeld aktualisieren
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

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
        buttons = new Button[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Button button = new Button();
                button.setMinWidth(30);
                button.setMinHeight(30);
                button.setStyle("-fx-base: #CCCCCC;");
                final int x = i;
                final int y = j;
                button.setOnAction(new ButtonHandler(x, y, button));
                button.setOnMouseClicked(event -> {
                    if (event.getButton().equals(javafx.scene.input.MouseButton.SECONDARY)) {
                        toggleFlag(button, x, y);
                    }
                });
                buttons[i][j] = button;
                gridPane.add(button, j, i);
            }
        }
        return gridPane;
    }

    private void startNewGame() {
        // Beende den aktuellen Timer-Thread, falls er existiert
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt(); // Timer-Thread unterbrechen
        }

        // Starte ein neues Spiel
        game = new Minesweeper(database, username, SIZE, MINES);
        remainingMines = MINES; // Zurücksetzen der verbleibenden Minen
        minesLabel.setText(" " + remainingMines);
        gameOver = false;
        startTime = System.currentTimeMillis(); // Reset startTime to current time
        timerLabel.setText("00:00"); // Reset timer label to 00:00
        smileyImageView.setImage(happyImage); // Reset smiley image to happy

        // Neu Initialisieren des Buttons-Arrays und des Spielfeldes
        GridPane gridPane = createBoard();

        // Root-Layout aktualisieren
        VBox root = (VBox) timerLabel.getScene().getRoot();
        if (root.getChildren().size() > 2) {
            root.getChildren().remove(2); // Altes Spielfeld entfernen
        }
        root.getChildren().add(gridPane); // Neues Spielfeld hinzufügen

        // Reset all buttons to initial state
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Button button = buttons[i][j];
                button.setText("");
                button.setDisable(false);
                button.setGraphic(null); // Clear any graphics (like mine or flag images)
                button.setStyle("-fx-base: #CCCCCC;"); // Reset background color
            }
        }
        updateBoard(); // Ensure the board is updated after resetting

        // Starte den Timer-Thread neu
        startTimer();
    }


    private void updateBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Button button = buttons[i][j];
                if (game.isRevealed(i, j)) {
                    if (game.isMine(i, j)) {
                        ImageView imageView = new ImageView(mineImage);
                        imageView.setFitWidth(15); // Set width based on button's minimum width
                        imageView.setFitHeight(15); // Set height based on button's minimum height
                        button.setGraphic(imageView);
                        button.setText(""); // Set empty text for consistency
                        if (game.isExploded() && button.getStyle().contains("-fx-background-color: red;")) {
                            continue; // Already set as exploded mine, skip further styling
                        }
                    } else {
                        button.setText(game.getCell(i, j));
                    }
                    button.setDisable(true);
                    setColorForCell(button, game.getCell(i, j));
                } else {
                    button.setText("");
                    button.setDisable(false);
                }
            }
        }
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
            case "6":
                button.setTextFill(Color.TEAL);
                break;
            case "8":
                button.setTextFill(Color.GRAY);
                break;
            default:
                button.setTextFill(Color.BLACK);
                break;
        }
    }
    private void changeDifficulty() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Difficulty");
        dialog.setHeaderText("Select the difficulty level:");

        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("Beginner", "Intermediate", "Expert");
        choiceBox.setValue("Beginner");

        VBox vbox = new VBox(choiceBox);
        vbox.setAlignment(Pos.CENTER_LEFT);
        vbox.setSpacing(10);
        dialog.getDialogPane().setContent(vbox);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String selectedDifficulty = choiceBox.getValue();
                switch (selectedDifficulty) {
                    case "Intermediate":
                        SIZE = 16;
                        MINES = 40;
                        break;
                    case "Expert":
                        SIZE = 22;
                        MINES = 99;
                        break;
                    default:
                        SIZE = 10;
                        MINES = 10;
                        break;
                }
                resetGame((Stage) timerLabel.getScene().getWindow()); // Restart the game with new settings
            }
            return null;
        });

        dialog.showAndWait();
    }


    private void resetGame(Stage primaryStage) {
        game = new Minesweeper(database, username, SIZE, MINES);
        remainingMines = MINES;
        minesLabel.setText(" " + remainingMines);
        gameOver = false;
        startTime = System.currentTimeMillis();
        timerLabel.setText("00:00");
        smileyImageView.setImage(happyImage);

        VBox root = new VBox();

        MenuBar menuBar = createMenuBar();
        root.getChildren().add(menuBar);

        HBox infoBox = new HBox();
        infoBox.setAlignment(Pos.CENTER);

        // Laden der Bilder
        flagImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/flag.png")));
        hintImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hint.png")));
        mineImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/mine.png")));
        Image clockImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/clock.png")));
        happyImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/happy.png")));
        sadImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/sad.png")));

        ImageView mineImageView = new ImageView(mineImage);
        mineImageView.setFitWidth(20);
        mineImageView.setFitHeight(20);
        minesLabel = new Label(" " + remainingMines);
        minesLabel.setStyle("-fx-font-size: 20;");
        HBox minesBox = new HBox(mineImageView, minesLabel);
        minesBox.setAlignment(Pos.CENTER_LEFT);

        ImageView clockImageView = new ImageView(clockImage);
        clockImageView.setFitWidth(20);
        clockImageView.setFitHeight(20);
        timerLabel = new Label("00:00");
        timerLabel.setStyle("-fx-font-size: 20;");
        HBox timerBox = new HBox(timerLabel, clockImageView);
        timerBox.setAlignment(Pos.CENTER_RIGHT);

        smileyImageView = new ImageView(happyImage);
        smileyImageView.setFitWidth(30);
        smileyImageView.setFitHeight(30);
        smileyImageView.setOnMouseClicked(event -> startNewGame());

        HBox leftBox = new HBox(minesBox);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        HBox rightBox = new HBox(timerBox);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        HBox centerBox = new HBox(smileyImageView);
        centerBox.setAlignment(Pos.CENTER);

        infoBox.getChildren().addAll(leftBox, centerBox, rightBox);
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        HBox.setHgrow(centerBox, Priority.ALWAYS);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
        root.getChildren().add(infoBox);

        GridPane gridPane = createBoard();
        root.getChildren().add(gridPane);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        startTimer();
        updateBoard();
    }



    private void showHighScores() {

    }

    private class ButtonHandler implements EventHandler<ActionEvent> {
        private final int x;
        private final int y;
        private final Button button;

        public ButtonHandler(int x, int y, Button button) {
            this.x = x;
            this.y = y;
            this.button = button;
        }

        @Override
        public void handle(ActionEvent event) {
            if (!gameOver) {
                game.reveal(x, y);
                updateBoard();
                if (game.isMine(x, y)) {
                    button.setStyle("-fx-background-color: red;"); // Mark the exploded mine with red color
                    smileyImageView.setImage(sadImage); // Change smiley image to sad
                    gameOver = true;
                    showGameOverAlert(false);
                } else if (game.checkWin()) {
                    gameOver = true;
                    showGameOverAlert(true);
                }
            }
        }
    }

    private void showGameOverAlert(boolean won) {
        // Beende den Timer-Thread, bevor das Spiel neu gestartet oder beendet wird
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        if (won) {
            alert.setHeaderText("Congratulations, you won!");
        } else {
            alert.setHeaderText("Game Over! You hit a mine.");
        }
        alert.setContentText("Close this message to continue.");
        revealAll();
        alert.showAndWait();
    }

    private void startTimer() {
        timerThread = new Thread(() -> {
            while (!gameOver && !Thread.currentThread().isInterrupted()) {
                long elapsed = System.currentTimeMillis() - startTime;
                long seconds = (elapsed / 1000) % 60;
                long minutes = (elapsed / (1000 * 60)) % 60;
                Platform.runLater(() -> timerLabel.setText(String.format("%02d:%02d", minutes, seconds)));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Set interrupt flag
                }
            }
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }

    private void exitGame() {
        // Beende den Timer-Thread, bevor das Spiel beendet wird
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
        Platform.exit();
    }

    private void toggleFlag(Button button, int x, int y) {
        if (!game.isRevealed(x, y)) {
            if (button.getGraphic() == null) {
                ImageView imageView = new ImageView(flagImage);
                imageView.setFitWidth(15);
                imageView.setFitHeight(15);
                button.setGraphic(imageView);
                remainingMines--; // Decrease the remaining mines count
            } else if (((ImageView) button.getGraphic()).getImage() == flagImage) {
                ImageView imageView = new ImageView(hintImage);
                imageView.setFitWidth(20);
                imageView.setFitHeight(20);
                button.setGraphic(imageView);
                remainingMines++; // Increase the remaining mines count
            } else {
                button.setGraphic(null);
            }
            minesLabel.setText(" " + remainingMines); // Update the label
        }
    }

    private void revealAll() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (!game.isRevealed(i, j)) {
                    Button button = buttons[i][j];
                    button.setText(game.getCell(i, j));
                    button.setDisable(true);
                    setColorForCell(button, game.getCell(i, j));
                    if (game.isMine(i, j)) {
                        ImageView imageView = new ImageView(mineImage);
                        imageView.setFitWidth(15);
                        imageView.setFitHeight(15);
                        button.setGraphic(imageView);
                    } else {
                        button.setGraphic(null);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
