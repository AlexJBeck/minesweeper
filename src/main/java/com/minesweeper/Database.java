package com.minesweeper;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private final String url = "jdbc:postgresql://minesweeper-3632.postgresql.b.osc-fr1.scalingo-dbs.com:31044/minesweeper_3632?sslmode=prefer";
    private final String user = "alexander";
    private final String password = "Alex1234";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
/*
    public void createTable() {
        String createStatsTableSQL = "CREATE TABLE IF NOT EXISTS highscoreExpert ("
                + "name VARCHAR(100) NOT NULL, "
                + "wins INTEGER NOT NULL DEFAULT 0, "
                + "losses INTEGER NOT NULL DEFAULT 0, "
                + "best_time TIMESTAMP DEFAULT NULL, "
                + "difficulty VARCHAR(20) NOT NULL DEFAULT 'Beginner'"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createStatsTableSQL);
            System.out.println("Table 'stats' created successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addPlayer(String name) {
        String insertPlayerSQL = "INSERT INTO users(name) VALUES(?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertPlayerSQL)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            System.out.println("Player added successfully: " + name);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void incrementWins(String name) {
        String updateWinsSQL = "UPDATE stats SET wins = wins + 1 WHERE name = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(updateWinsSQL)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            System.out.println("Wins incremented for user: " + name);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void incrementLosses(String name) {
        String updateLossesSQL = "UPDATE stats SET losses = losses + 1 WHERE name = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(updateLossesSQL)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            System.out.println("Losses incremented for user: " + name);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateBestTime(String name, int time) {
        String updateBestTimeSQL = "UPDATE stats SET best_time = ? WHERE name = ? AND (best_time IS NULL OR best_time > ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(updateBestTimeSQL)) {
            pstmt.setInt(1, time);
            pstmt.setString(2, name);
            pstmt.setInt(3, time);
            pstmt.executeUpdate();
            System.out.println("Best time updated for user: " + name);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    } */

    public void updateBestTime(String name, String difficulty, long newTime) {
        String highscoreTable = getHighscoreTable(difficulty);
        String checkBestTimeSQL = "SELECT best_time FROM " + highscoreTable + " WHERE name = ? ORDER BY best_time ASC LIMIT 1";
        String insertHighscoreSQL = "INSERT INTO " + highscoreTable + " (name, best_time) VALUES (?, ?)";
        String updateHighscoreSQL = "UPDATE " + highscoreTable + " SET best_time = ? WHERE name = ?"; /* AND best_time > ?*/

        try (Connection conn = connect();
             PreparedStatement pstmtCheck = conn.prepareStatement(checkBestTimeSQL);
             PreparedStatement pstmtInsert = conn.prepareStatement(insertHighscoreSQL);
             PreparedStatement pstmtUpdate = conn.prepareStatement(updateHighscoreSQL)) {

            pstmtCheck.setString(1, name);
            ResultSet rs = pstmtCheck.executeQuery();

            if (rs.next()) {
                long bestTime = rs.getLong("best_time");
                if (newTime < bestTime) {
                    // Update the existing record with the new best time
                    pstmtUpdate.setLong(1, newTime);
                    pstmtUpdate.setString(2, name);
                    //pstmtUpdate.setLong(3, bestTime);
                    pstmtUpdate.executeUpdate();
                    System.out.println("Best time updated for user: " + name + " in table " + highscoreTable);
                } else {
                    System.out.println("New time is not better than the existing best time for user: " + name);
                }
            } else {
                // Insert a new record if no existing best time is found
                pstmtInsert.setString(1, name);
                pstmtInsert.setLong(2, newTime);
                pstmtInsert.executeUpdate();
                System.out.println("New best time inserted for user: " + name + " in table " + highscoreTable);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


            private String getHighscoreTable(String difficulty) {
        switch (difficulty) {
            case "Intermediate":
                return "highscoreIntermediate";
            case "Expert":
                return "highscoreExpert";
            default:
                return "highscoreBeginner";
        }
    }

    public List<String> getHighScores(String difficulty) {
        String highscoreTable = getHighscoreTable(difficulty);
        String getHighScoresSQL = "SELECT name, best_time FROM " + highscoreTable + " WHERE best_time IS NOT NULL ORDER BY best_time ASC LIMIT 10";
        List<String> highScores = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(getHighScoresSQL)) {
            while (rs.next()) {
                String name = rs.getString("name");
                long time = rs.getLong("best_time");
                String formattedTime = formatTime(time);
                highScores.add(name + ": " + formattedTime);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return highScores;
    }

    private String formatTime(long time) {
        long seconds = (time / 1000) % 60;
        long minutes = (time / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


    public static void main(String[] args) {
        Database db = new Database();
        //db.createTables(); // Create the high score tables if they don't exist

        String testName = "testUser";
        String testDifficulty = "Expert";
        Timestamp testTime1 = Timestamp.from(Instant.now());

        // Sleep for 2 seconds to create a later timestamp
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Timestamp testTime2 = Timestamp.from(Instant.now());
        /*
        // Add a player with an initial best time
        System.out.println("Inserting initial best time...");
        db.updateBestTime(testName, testDifficulty, testTime1);

        // Try to update with a worse time
        System.out.println("Trying to update with a worse time...");
        db.updateBestTime(testName, testDifficulty, testTime2);

        // Try to update with a better time
        System.out.println("Trying to update with a better time...");
        db.updateBestTime(testName, testDifficulty, testTime1);

         */
    }
}
