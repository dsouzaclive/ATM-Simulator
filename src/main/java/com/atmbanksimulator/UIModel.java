package com.atmbanksimulator;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.Optional;

/**
 * <h1>ATM Simulator - UI Model: Central Processor</h1>
 *
 * <p>
 * The {@code UIModel} class acts as the brain of the ATM system, managing all UI logic,
 * session state, and user input processing. It receives instructions from the {@link Controller},
 * maintains the current ATM state and, communicates with the {@link Bank} domain layer to execute banking operations,
 * and instructs the {@link View} to update the display.
 * </p>
 *
 * @author D'Souza, C. J.
 * @version 3.0
 */
public class UIModel {
    View view;
    private Bank bank;

    private boolean onATMScreen = false;

    private final String STATE_ACCOUNT_NO = "account_no";
    private final String STATE_PASSWORD = "password";
    private final String STATE_LOGGED_IN = "logged_in";
    private final String STATE_DEPOSIT = "deposit";
    private final String STATE_WITHDRAW = "withdraw";
    private final String STATE_TRANSFER_ACCOUNT_NO = "transfer_account";
    private final String STATE_TRANSFER_AMOUNT = "transfer_amount";
    private final String STATE_BALANCE = "balance";
    private final String STATE_TRANSACTIONS = "transactions";
    private final String STATE_SETTINGS_MENU = "settings";
    private final String STATE_SETTINGS_ACCOUNT_DETAILS = "settings_account_details";
    private final String STATE_SETTINGS_CHANGE_PASSWORD = "settings_change_password";
    private final String STATE_SETTINGS_CHANGE_NEW_PASSWORD = "settings_change_new_password";
    private final String STATE_SETTINGS_CHANGE_PLAN = "settings_change_plan";
    private final String STATE_SETTINGS_CHANGE_WITHDRAWAL_LIMIT = "settings_change_withdrawal_limit";

    private String state = STATE_ACCOUNT_NO;
    private String accNumber = "";
    private String accPasswd = "";

    private int failedLoginAttempts = 0;
    private final int maxLogInAttempts = 3;

    private String title;
    private String message;
    private String numberPadInput = "";
    private String boxStatus;
    private String accPinInput = "";

    private ArrayList<ArrayList<String>> history;
    private String accNumber_Transfer = "";
    private ArrayList<String> accountDetails;

    public UIModel(Bank bank) {
        this.bank = bank;
    }

    /** Initialises the ATM to its default state, clearing all session data and resetting the display. */
    public void initialise() {
        onATMScreen = false;
        setState(STATE_ACCOUNT_NO);
        numberPadInput = "";
        accNumber = "";
        accPasswd = "";
        accPinInput = "";
        failedLoginAttempts = 0;
        accountDetails = null;
        history = null;
        title = "INPUT ACCOUNT NUMBER";
        message = "Enter the Account Number:";
        boxStatus = "account_no";
        update();
    }

    /**
     * Resets the ATM state to STATE_ACCOUNT_NO with the given title and message.
     * Used after invalid actions or failed login attempts.
     * @param tit the title to display
     * @param msg the message to display
     */
    private void reset(String tit, String msg) {
        setState(STATE_ACCOUNT_NO);
        numberPadInput = "";
        title = tit;
        message = msg;
    }

    /**
     * Transitions the ATM to a new state and logs the change to the console.
     * @param newState the new state to transition to
     */
    private void setState(String newState) {
        if ( !state.equals(newState) ) {
            String oldState = state;
            state = newState;
            System.out.println("UIModel::setState: changed state from "+ oldState + " to " + newState);
        }
    }

    /** Called when the user clicks START. Sets onATMScreen to true and initialises the session. */
    public void processStart() {
        initialise();
        onATMScreen = true;
    }

    /**
     * Handles numpad digit, decimal point, and backspace input.
     * Validates decimal point usage based on the current state.
     * @param numberOnButton the button label pressed
     */
    public void processNumber(String numberOnButton) {
        if(numberOnButton.equals("BACKSPACE")) {
            if (!numberPadInput.isEmpty()) {
                numberPadInput = numberPadInput.substring(0, numberPadInput.length() - 1);
                if(state.equals(STATE_PASSWORD)){ accPinInput = accPinInput.substring(0, accPinInput.length() - 1); }
            }
            numberOnButton = "";
        }
        else if(numberOnButton.equals(".")){
            if(state.equals(STATE_PASSWORD) || state.equals(STATE_ACCOUNT_NO) || numberPadInput.contains(".") || state.equals(STATE_TRANSFER_ACCOUNT_NO)
                || state.equals(STATE_SETTINGS_CHANGE_PASSWORD) || state.equals(STATE_SETTINGS_CHANGE_NEW_PASSWORD)){
                numberOnButton = "";
            } else if(numberPadInput.isEmpty()){
                numberPadInput += "0";
            }
        }
        else if(state.equals(STATE_PASSWORD)){ accPinInput += numberOnButton; numberOnButton="*"; }
        if(numberPadInput.contains(".") && numberPadInput.length()-numberPadInput.indexOf(".")>2){ numberOnButton = ""; }

        numberPadInput += numberOnButton;
        update();
    }

    /** Clears the current numberPadInput and refreshes the display. */
    public void processClear() {
        if (!numberPadInput.isEmpty()) {
            if(state.equals(STATE_PASSWORD)){ accPinInput = ""; }
            numberPadInput = "";
            update();
        }
    }

    /**
     * Handles the ENTER button press. It's behaviour depends on the current state —
     * validates account number, authenticates password, processes transactions, or confirms settings changes.
     */
    public void processEnter(){
        numberPadInput = numberPadInput.trim();
        switch ( state ) {
            case STATE_ACCOUNT_NO:
                if(numberPadInput.isEmpty()){
                    numberPadInput = "";
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Enter a Valid Account Number");
                    alert.show();
                    reset(title, message);
                }
                else if(bank.login_AccountExist(numberPadInput)){
                    accNumber = numberPadInput;
                    numberPadInput = "";
                    setState(STATE_PASSWORD);
                    title = "INPUT PASSWORD FOR ACCOUNT NUMBER: " + accNumber;
                    message = "Enter the PIN/Password:";
                }
                else{
                    numberPadInput = "";
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Account Not Found - please enter a valid account number");
                    alert.show();
                    reset(title, message);
                }
                break;

            case STATE_PASSWORD:
                accPasswd = accPinInput;
                accPinInput = "";
                numberPadInput = "";
                if (bank.login(accNumber, accPasswd)) {
                    failedLoginAttempts = 0; // reset on successful login
                    setState(STATE_LOGGED_IN);
                    title = "WELCOME, " + bank.getName(accNumber);
                    message = "";
                    boxStatus = state;
                } else {
                    failedLoginAttempts++;
                    if (failedLoginAttempts >= maxLogInAttempts) {
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                "Your account has been locked due to " + maxLogInAttempts + " failed login attempts. Please contact your bank.");
                        alert.show();
                        failedLoginAttempts = 0;
                        accPinInput = "";
                        reset("INPUT ACCOUNT NUMBER", "Enter the Account Number:");
                    } else {
                        int remaining = maxLogInAttempts - failedLoginAttempts;
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                "Invalid PIN. " + remaining + " attempt(s) remaining.");
                        alert.show();
                    }
                }
                break;

            case STATE_DEPOSIT:
                float amount_deposit = parseValidAmount(numberPadInput);
                if(amount_deposit>0){
                    bank.deposit(amount_deposit);
                    Alert alert_deposit = new Alert(Alert.AlertType.INFORMATION, "An amount of £" + numberPadInput + " was successfully deposited into your account.\nAccount Balance: £" + bank.getBalance());
                    setState(STATE_LOGGED_IN);
                    title = "WELCOME, " + bank.getName(accNumber);
                    message = "";
                    boxStatus = state;
                    alert_deposit.show();
                }else{
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Amount");
                    alert.show();
                }
                numberPadInput="";
                break;

            case STATE_WITHDRAW:
                float amount_withdraw = parseValidAmount(numberPadInput);
                float limit_custom = bank.getWithdrawalLimit(accNumber);
                if(amount_withdraw > 0) {
                    if(amount_withdraw> limit_custom){
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Withdrawal Amount exceeds the one-time withdrawal limit of £" + limit_custom);
                        alert.show();
                    } else{
                        if (bank.withdraw(amount_withdraw)) {
                            Alert alert_withdraw = new Alert(Alert.AlertType.INFORMATION, "An amount of £" + numberPadInput + " was successfully withdrawn from your account.\nAccount Balance: £" + bank.getBalance());
                            alert_withdraw.show();
                            setState(STATE_LOGGED_IN);
                            title = "WELCOME, " + bank.getName(accNumber);
                            message = "";
                            boxStatus = state;
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Withdrawal Forfeited due to:\n1. Exceeded the Account Balance of £"+bank.getBalance()+"\n2. Withdrawal fails to maintain a minimum balance of £20");
                            alert.show();
                        }
                    }
                } else{
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Amount");
                    alert.show();
                }
                numberPadInput="";
                break;

            case STATE_BALANCE, STATE_TRANSACTIONS:
                setState(STATE_LOGGED_IN);
                title = "WELCOME, " + bank.getName(accNumber);
                message = "";
                boxStatus = state;
                numberPadInput="";
                break;

            case STATE_TRANSFER_ACCOUNT_NO:
                if(numberPadInput.isEmpty()){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Enter a Valid Account Number");
                    alert.show();
                }else if(numberPadInput.equals(accNumber)){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "You cannot transfer money to yourself.");
                    alert.show();
                }else if(bank.login_AccountExist(numberPadInput)){
                    accNumber_Transfer = numberPadInput;
                    setState(STATE_TRANSFER_AMOUNT);
                    title = "TRANSFER SERVICES";
                    message = "Enter the amount to be transferred:";
                }else{
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Account Not Found - please enter a valid account number");
                    alert.show();
                }
                numberPadInput = "";
                break;

            case STATE_TRANSFER_AMOUNT:
                float amount_transfer = parseValidAmount(numberPadInput);
                if(amount_transfer > 0) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "An amount of £"+numberPadInput+" will be transferred to Account Number: "+accNumber_Transfer);
                    Optional<ButtonType> buttonType = alert.showAndWait();
                    if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
                        if (bank.withdraw_transferred(accNumber_Transfer, amount_transfer)) {
                            bank.deposit_transferred(accNumber_Transfer, amount_transfer);
                            Alert alert_withdraw = new Alert(Alert.AlertType.INFORMATION, "An amount of £" + numberPadInput + " was successfully withdrawn from your account and transferred to Account Number:" + accNumber_Transfer +".\nAccount Balance: £" + bank.getBalance());
                            alert_withdraw.show();
                            setState(STATE_LOGGED_IN);
                            title = "WELCOME, " + bank.getName(accNumber);
                            message = "";
                            boxStatus = state;
                        } else {
                            Alert alert1 = new Alert(Alert.AlertType.ERROR, "Insufficient Funds");
                            alert1.show();
                        }
                    }

                } else{
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Amount");
                    alert.show();
                }
                numberPadInput="";
                break;

            case STATE_SETTINGS_CHANGE_PASSWORD:
                if(bank.checkPin(numberPadInput)){
                    setState(STATE_SETTINGS_CHANGE_NEW_PASSWORD);
                    title = "UPDATE PASSWORD FOR ACCOUNT NUMBER: " + accNumber;
                    message = "Enter the NEW pin/password:";
                    boxStatus = state;
                }else{
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid PIN/Password");
                    alert.show();
                }
                numberPadInput="";
                break;

            case STATE_SETTINGS_CHANGE_NEW_PASSWORD:
                if(numberPadInput.length()==4){
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Your PIN will change after you click OK");
                    Optional<ButtonType> buttonType = alert.showAndWait();
                    if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
                        bank.updatePin(accNumber, numberPadInput);
                        setState(STATE_SETTINGS_MENU);
                        title = "SETTINGS";
                        message = "";
                        boxStatus = state;
                    }
                }else{
                    Alert alert = new Alert(Alert.AlertType.ERROR, "The PIN must be of 4 digits");
                    alert.show();
                }
                numberPadInput="";
                break;

            case STATE_SETTINGS_CHANGE_WITHDRAWAL_LIMIT:
                float amount = parseValidAmount(numberPadInput);
                float limit = 1000;
                switch(accountDetails.get(10)+accountDetails.get(11)){
                    case "BasicNormal": limit = 300; break;
                    case "BasicPro": limit = 500; break;
                    case "BasicPrime": limit = 700; break;
                    case "StudentNormal": limit = 250; break;
                    case "StudentPro": limit = 400; break;
                    case "StudentPrime": limit = 600; break;
                }
                if(amount>=50 && amount<=limit) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Your one-time withdrawal limit will be set to £"+amount);
                    Optional<ButtonType> buttonType = alert.showAndWait();
                    if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
                        bank.updateWithdrawalLimit(accNumber, amount);
                        setState(STATE_SETTINGS_MENU);
                        title = "SETTINGS";
                        message = "";
                        boxStatus = state;
                    }
                }else{
                    Alert alert = new Alert(Alert.AlertType.ERROR, "The account limit can be set from £50.0 - £" + limit);
                    alert.show();
                }
                numberPadInput="";
                break;

            case STATE_LOGGED_IN:
            default:
        }

        update();
    }

    /**
     * Parses a string into a valid float transaction amount.
     * @param number the string to parse
     * @return the parsed float, or 0 if empty or invalid
     */
    private float parseValidAmount(String number) {
        if (number.isEmpty()) { return 0; }
        try { return Float.parseFloat(number); }
        catch (NumberFormatException e) { return 0; }
    }

    /** Handles the Balance button. Displays the current account balance if logged in. */
    public void processBalance() {
        if (state.equals(STATE_LOGGED_IN)) {
            state = STATE_BALANCE;
            float amount_balance = bank.getBalance();
            title = "BALANCE PREVIEW";
            message = "Current Account Balance: £" + amount_balance;
            boxStatus = state;
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You aren't Logged In");
            alert.show();
            reset(title, message);
        }
        update();
    }

    /** Handles the Withdraw button. Transitions to the withdrawal input state if logged in. */
    public void processWithdraw() {
        if (state.equals(STATE_LOGGED_IN)) {
            state = STATE_WITHDRAW;
            title = "WITHDRAWAL SERVICES";
            message = "Enter the Amount (£) to be withdrawn: ";
            boxStatus = state;
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You aren't Logged In");
            alert.show();
            reset(title, message);
        }
        update();
    }

    /** Handles the Deposit button. Transitions to the deposit input state if logged in. */
    public void processDeposit() {
        if (state.equals(STATE_LOGGED_IN)) {
            state = STATE_DEPOSIT;
            title = "DEPOSIT SERVICES";
            message = "Enter the Amount (£) to be deposited: ";
            boxStatus = state;
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You aren't Logged In");
            alert.show();
            reset(title, message);
        }
        update();
    }

    /**
     * Handles predefined deposit amount buttons (£5, £10, £20, £50, £100, £200).
     * Sets the numberPadInput directly without requiring manual numpad entry.
     * @param amount the predefined amount as a string
     */
    public void processDeposit_PreDefined(String amount){
        if(state.equals(STATE_DEPOSIT)){
            switch(amount){
                case "5", "10", "20", "50", "100", "200":
                    numberPadInput = amount;
                    break;
            }

        } else{
            Alert alert = new Alert(Alert.AlertType.ERROR, "You aren't Logged In");
            alert.show();
            reset(title, message);
        }
        update();
    }

    /** Handles the History button. Fetches and stores transaction history if logged in. */
    public void processTransaction(){
        if (state.equals(STATE_LOGGED_IN)) {
            state = STATE_TRANSACTIONS;
            title = "TRANSACTION HISTORY FOR ACCOUNT NUMBER: " + accNumber;
            message = "";
            boxStatus = state;
            history = bank.getTransactionHistory(accNumber);
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You aren't Logged In");
            alert.show();
            reset(title, message);
        }
        update();
    }

    /** Handles the Transfer button. Transitions to the beneficiary account input state if logged in. */
    public void processTransfer(){
        if (state.equals(STATE_LOGGED_IN)) {
            state = STATE_TRANSFER_ACCOUNT_NO;
            title = "TRANSFER SERVICES";
            message = "Enter the Beneficiary Account Number:";
            boxStatus = state;
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You aren't Logged In");
            alert.show();
            reset(title, message);
        }
        update();
    }

    /**
     * Returns the transaction history retrieved during processTransaction().
     * @return list of transaction records
     */
    public ArrayList<ArrayList<String>> getTransactionHistory(){
        return history;
    }

    /** Handles the Home button. Returns to the main logged in menu. */
    public void processHome(){
        setState(STATE_LOGGED_IN);
        title = "WELCOME, " + bank.getName(accNumber);
        message = "";
        boxStatus = state;
        numberPadInput = "";
        update();
    }

    /** Handles the Settings button. Transitions to the settings menu and fetches account details. */
    public void processSettings(){
        setState(STATE_SETTINGS_MENU);
        title = "SETTINGS";
        message = "";
        boxStatus = state;
        numberPadInput = "";
        accountDetails = bank.getAccountDetails(accNumber);
        update();
    }

    /** Handles the Account Details button within settings. Transitions to the account details view. */
    public void processAccountDetails(){
        if(state.equals(STATE_SETTINGS_MENU)){
            setState(STATE_SETTINGS_ACCOUNT_DETAILS);
            title = "ACCOUNT DETAILS FOR ACCOUNT NUMBER: " + accNumber;
            message = "";
            boxStatus = state;
            numberPadInput = "";
            update();
        }
    }

    /**
     * Returns the account details retrieved during processSettings().
     * @return list of account detail strings in a fixed order
     */
    public ArrayList<String> getAccountDetails(){ return accountDetails; }

    /** Handles the Change Password button. Transitions to the current password verification state. */
    public void processChangePassword(){
        if(state.equals(STATE_SETTINGS_MENU)){
            setState(STATE_SETTINGS_CHANGE_PASSWORD);
            title = "UPDATE PASSWORD FOR ACCOUNT NUMBER:" + accNumber;
            message = "Enter the CURRENT password/pin";
            boxStatus = state;
            numberPadInput = "";
            update();
        }
    }

    /** Handles the Change Plan button. Transitions to the plan selection screen. */
    public void processChangePlan(){
        if(state.equals(STATE_SETTINGS_MENU)){
            setState(STATE_SETTINGS_CHANGE_PLAN);
            title = "UPDATE PLAN FOR ACCOUNT NUMBER:" + accNumber;
            message = "Current Plan: " + accountDetails.get(10) + " " + accountDetails.get(11);
            boxStatus = state;
            numberPadInput = "";
            update();
        }
    }

    /**
     * Saves the updated plan selection and refreshes account details.
     * @param type            the new account type (Basic or Student)
     * @param tier            the new account tier (Normal, Pro, or Prime)
     * @param withdrawalLimit the new withdrawal limit as a string
     */
    public void updatePlan(String type, String tier, String withdrawalLimit){
        if(state.equals(STATE_SETTINGS_CHANGE_PLAN)){
            bank.updatePlan(accNumber, type, tier, parseValidAmount(withdrawalLimit));
            setState(STATE_SETTINGS_MENU);
            title = "SETTINGS";
            message = "";
            boxStatus = state;
            numberPadInput = "";
            accountDetails = bank.getAccountDetails(accNumber);
            update();
        }
    }

    /** Handles the Change Withdrawal Limit button. Transitions to the limit input state. */
    public void processChangeWithdrawalLimit(){
        if(state.equals(STATE_SETTINGS_MENU)){
            setState(STATE_SETTINGS_CHANGE_WITHDRAWAL_LIMIT);
            title = "CHANGE WITHDRAWAL LIMIT FOR ACCOUNT NUMBER: " + accNumber;
            message = "Current Withdrawal Limit: " + bank.getWithdrawalLimit(accNumber);
            boxStatus = state;
            numberPadInput = "";
            update();
        }
    }

    /** Handles the EXIT button. Shows a confirmation dialog — if confirmed, logs out and returns to the home screen. */
    public void processExit() {
        if (!onATMScreen) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Are You Sure?");
        alert.setHeaderText("You clicked to Exit");
        alert.setResizable(true);
        alert.setHeight(500);
        alert.setContentText("Do you really want to exit?");
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
            bank.logout();
            accNumber = "";
            accPasswd = "";
            accPinInput = "";
            setState(STATE_ACCOUNT_NO);
            numberPadInput = "";
            title = "Enter the Account Number";
            message = "Account Number";
            boxStatus = "startpage";
        }
        update();

    }

    /**
     * Handles any unrecognised button press.
     * @param action the unrecognised action string
     */
    public void processUnknownKey(String action) {
        reset("Invalid Command", "Invalid Command " + action);
        update();
    }

    /** Notifies the View to refresh the display with the current title, message, input and state. */
    private void update() {
        view.update(title, message, numberPadInput, boxStatus);
    }

}

