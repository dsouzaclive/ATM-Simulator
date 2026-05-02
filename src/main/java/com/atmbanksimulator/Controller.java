package com.atmbanksimulator;

import java.util.ArrayList;

/**
 * <h1>ATM Simulator - Controller</h1>
 *
 * <p>
 * The {@code Controller} class acts as the nervous system of the ATM, creating connections for button press
 * events received from the {@link View} to the appropriate processing methods in {@link UIModel}.
 * It contains no business logic of its own, it solely maps user actions to UIModel instructions.
 * </p>
 *
 * @author D'Souza, C. J.
 * @version 3.0
 */
public class Controller {

    UIModel UIModel;

    /**
     * Connects a button press action from the {@link View} to the appropriate method in {@link UIModel}.
     * Acts purely as a dispatcher or UI method mapper with no business logic.
     * @param action the text label or ID of the button that was pressed
     */
    protected void process( String action ){
        switch (action) {
            case "START":
                UIModel.processStart();
                break;
            case "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "BACKSPACE", ".":
                UIModel.processNumber(action);
                break;
            case "CLEAR":
                UIModel.processClear();
                break;
            case "ENTER":
                UIModel.processEnter();
                break;
            case "Withdraw":
                UIModel.processWithdraw();
                break;
            case "Deposit":
                UIModel.processDeposit();
                break;

                    case "deposit_5": UIModel.processDeposit_PreDefined("5"); break;
                    case "deposit_10": UIModel.processDeposit_PreDefined("10"); break;
                    case "deposit_20": UIModel.processDeposit_PreDefined("20"); break;
                    case "deposit_50": UIModel.processDeposit_PreDefined("50"); break;
                    case "deposit_100": UIModel.processDeposit_PreDefined("100"); break;
                    case "deposit_200": UIModel.processDeposit_PreDefined("200"); break;

            case "Balance":
                UIModel.processBalance();
                break;
            case "Transfer":
                UIModel.processTransfer();
                break;
            case "History":
                UIModel.processTransaction();
                break;
            case "EXIT":
                UIModel.processExit();
                break;
            case "Home":
                UIModel.processHome();
                break;
            case "Settings":
                UIModel.processSettings();
                break;
            case "Account Details":
                UIModel.processAccountDetails();
                break;
            case "Change Password":
                UIModel.processChangePassword();
                break;
            case "Change Plan":
                UIModel.processChangePlan();
                break;
            case "Change Withdrawal Limit":
                UIModel.processChangeWithdrawalLimit();
                break;
            default:
                UIModel.processUnknownKey(action);
                break;
        }
    }

    /**
     * Forwards a plan change request to {@link UIModel} after the user confirms their selection in the Change Plan screen.
     * @param type            the selected account type — {@code "Basic"} or {@code "Student"}
     * @param tier            the selected account tier — {@code "Normal"}, {@code "Pro"}, or {@code "Prime"}
     * @param withdrawalLimit the corresponding withdrawal limit as a string
     */
    protected void saveChangePlan(String type, String tier, String withdrawalLimit){
        UIModel.updatePlan(type, tier, withdrawalLimit);
    }

    /**
     * Retrieves the transaction history from {@link UIModel}.
     * @return a list of transaction records each as a list of strings
     */
    protected ArrayList<ArrayList<String>> getTransactionHistory(){ return UIModel.getTransactionHistory(); }

    /**
     * Retrieves the account details from {@link UIModel}.
     * @return a list of account detail strings in a fixed positional order
     */
    protected ArrayList<String> getAccountDetails(){ return UIModel.getAccountDetails(); }
}


