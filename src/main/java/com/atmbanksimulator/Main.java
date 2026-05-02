package com.atmbanksimulator;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * <h1>ATM Simulator - Main: Entry Point</h1>
 *
 * <p>
 * The {@code Main} class serves as the entry point for the ATM Banking Simulator application.
 * It initialises and connects all components of the MVC + Domain architecture —
 * {@link View}, {@link Controller}, {@link UIModel}, and {@link Bank} — and launches
 * the JavaFX application window.
 * </p>
 *
 * @author D'Souza, C. J.
 * @version 3.0
 */
public class Main extends Application {
    public static void main( String args[] ) {launch(args);}

    /**
     * Initialises and connects all MVC components — {@link Bank}, {@link UIModel}, {@link View}, and {@link Controller}
     * — then launches the JavaFX window and initialises the UIModel to its default state.
     * @param window the primary {@link Stage} provided by the JavaFX framework
     */
    public void start(Stage window){
        // Create a Bank object add two bank accounts for test
        Bank bank = new Bank();

        //UIModel-View-Controller structure setup
        // Create the UIModel, View and Controller objects and link them together
        UIModel UIModel = new UIModel(bank);   // the UIModel needs the Bank object to 'talk to' the bank
        View view  = new View();
        Controller controller  = new Controller();

        // Link them together so they can talk to each other
        view.controller = controller;
        controller.UIModel = UIModel;
        UIModel.view = view;

        // start up the GUI (view), and then tell the UIModel to initialise itself
        view.start(window);
        UIModel.initialise();
    }
}
