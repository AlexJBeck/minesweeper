package com.minesweeper;

import javafx.application.Application;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // Verbindung zur Datenbank herstellen
        Database dbConnection = new Database();
        //dbConnection.createTable(); // Optional: Tabelle erstellen, falls noch nicht vorhanden

        try (Connection conn = dbConnection.connect()) {
            if (conn != null) {
                System.out.println("Connected to the PostgreSQL server successfully.");
                // Führen Sie hier Ihre Datenbankoperationen durch, falls erforderlich
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // Starten der JavaFX-Anwendung
        Application.launch(MinesweeperUi.class, args);
    }
}