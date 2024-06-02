package com.minesweeper;

import java.sql.*;

public class Database {
    private final String url = "jdbc:postgresql://minesweeper-3632.postgresql.b.osc-fr1.scalingo-dbs.com:31044/minesweeper_3632?sslmode=prefer";

    private final String user = "alexander";
    private final String password = "Alex1234";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void createTable() {
        String createStatsTableSQL = "CREATE TABLE IF NOT EXISTS stats ("
                //+ "id SERIAL PRIMARY KEY, "
                + "username VARCHAR(100) NOT NULL, "
                + "wins INTEGER NOT NULL DEFAULT 0, "
                + "losses INTEGER NOT NULL DEFAULT 0 "
                //+ "board_size INTEGER NOT NULL DEFAULT -100, "
                //+ "FOREIGN KEY (username) REFERENCES users(name)"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createStatsTableSQL);
            System.out.println("Table 'users' created successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addPlayer(String name) {
        //String insertPlayerSQL = "INSERT INTO users(name) VALUES(?)";
        String insertPlayerSQL = "INSERT INTO stats(username) VALUES(?)";


        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertPlayerSQL)) {
            pstmt.setString(1, name);
            //pstmt.setString(2, email);
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



    public static void main(String[] args) {
        Database db = new Database();
        db.incrementLosses("Alex");

    }
}
