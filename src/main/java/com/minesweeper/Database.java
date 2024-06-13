package com.minesweeper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private final String url = "jdbc:postgresql://minesweeper-3632.postgresql.b.osc-fr1.scalingo-dbs.com:31044/minesweeper_3632?sslmode=prefer";
    private final String user = "alexander";
    private final String password = "Alex1234";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void createTable() {
        String createStatsTableSQL = "CREATE TABLE IF NOT EXISTS stats ("
                + "username VARCHAR(100) NOT NULL, "
                + "wins INTEGER NOT NULL DEFAULT 0, "
                + "losses INTEGER NOT NULL DEFAULT 0, "
                + "best_time INTEGER DEFAULT NULL"
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
        String insertPlayerSQL = "INSERT INTO stats(username) VALUES(?)";

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
        String updateWinsSQL = "UPDATE stats SET wins = wins + 1 WHERE username = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(updateWinsSQL)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            System.out.println("Wins incremented for user ID: " + name);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void incrementLosses(String name) {
        String updateLossesSQL = "UPDATE stats SET losses = losses + 1 WHERE username = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(updateLossesSQL)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            System.out.println("Losses incremented for user ID: " + name);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateBestTime(String name, int time) {
        String updateBestTimeSQL = "UPDATE stats SET best_time = ? WHERE username = ? AND (best_time IS NULL OR best_time > ?)";

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
    }

    public List<String> getHighScores() {
        String getHighScoresSQL = "SELECT username, best_time FROM stats WHERE best_time IS NOT NULL ORDER BY best_time ASC LIMIT 10";
        List<String> highScores = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(getHighScoresSQL)) {
            while (rs.next()) {
                String username = rs.getString("username");
                int time = rs.getInt("best_time");
                highScores.add(username + ": " + time + " seconds");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return highScores;
    }

    public static void main(String[] args) {
        Database db = new Database();
        db.incrementLosses("Alex");
    }
}