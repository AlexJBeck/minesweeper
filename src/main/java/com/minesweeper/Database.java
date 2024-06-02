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
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "id SERIAL PRIMARY KEY, "
                + "name VARCHAR(100) NOT NULL "
                //+ "email VARCHAR(100) NOT NULL UNIQUE"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Table 'users' created successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addPlayer(String name) {
        String insertPlayerSQL = "INSERT INTO users(name, email) VALUES(?, ?)";

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



    public static void main(String[] args) {
        Database db = new Database();
        db.createTable();

    }
}
