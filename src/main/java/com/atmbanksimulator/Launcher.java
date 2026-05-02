package com.atmbanksimulator;

import javafx.application.Application;

/**
 * <h1>ATM Simulator - Launcher</h1>
 *
 * <p>
 * The {@code Launcher} class provides an entry point for launching the JavaFX application.
 * It's responsible for launching the application, allowing {@link Main} to focus solely on
 * MVC setup without acting as the entry point.
 * </p>
 *
 * @author D'Souza, C. J.
 * @version 3.0
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
