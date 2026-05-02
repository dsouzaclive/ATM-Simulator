package com.atmbanksimulator;

import java.util.ArrayList;

/**
 * <h1>ATM Simulator - Bank: Bank Manager</h1>
 *
 * <p>
 * The {@code Bank} class represents the bank system that manages account authentication and operations.
 * It acts as an intermediary between the {@link UIModel} and the {@link SQLite} database,
 * that is responsible for all data operations through {@link BankAccount} and {@link SQLite}.
 * </p>
 *
 * @author D'Souza, C. J.
 * @version 3.0
 */
public class Bank {
    private SQLite sqlite;

    BankAccount loggedInAccount = null;

    /** Initialises the Bank and establishes a connection to the SQLite database. */
    public Bank(){ sqlite = new SQLite(); }

    /**
     * Method to create a new BankAccount2 instance.
     * @param accNumber the account number
     * @param accPasswd the account PIN/password
     * @param balance   the current balance
     * @param sqlite    the SQLite connection to use
     * @return a new BankAccount object
     */
    public BankAccount setUpBankAccount(String accNumber, String accPasswd, float balance, SQLite sqlite){
        return new BankAccount(accNumber, accPasswd, balance, sqlite);
    }

    /**
     * Checks whether an account with the given number exists in the database.
     * @param accountNumber the account number to check
     * @return true if the account exists, false otherwise
     */
    public boolean login_AccountExist(String accountNumber){
        return sqlite.checkAccountExist(accountNumber);
    }

    /**
     * Attempts to log in with the given account number and password.
     * Logs out any previously logged in account before attempting login.
     * @param accountNumber the account number
     * @param password      the PIN/password to verify
     * @return true if login is successful, false otherwise
     */
    public boolean login(String accountNumber, String password) {
        logout();

        if (sqlite.checkAccountExist(accountNumber)){
            if(sqlite.getPasswd(accountNumber).equals(password)){
                loggedInAccount = setUpBankAccount(accountNumber, password, sqlite.getBalance(accountNumber), sqlite);
                return true;
            }
        }
        loggedInAccount = null;
        return false;
    }

    /** Logs out the currently logged in account by setting loggedInAccount to null. */
    public void logout() {
        if (loggedIn()) { loggedInAccount = null;}
    }

    /**
     * Checks whether a user is currently logged in.
     * @return true if an account is logged in, false otherwise
     */
    public boolean loggedIn() {
        return loggedInAccount != null;
    }

    /**
     * Deposits the given amount into the logged in account.
     * @param amount the amount to deposit
     * @return true if deposit was successful, false if not logged in
     */
    public boolean deposit(float amount){
        if (loggedIn()) { return loggedInAccount.deposit(amount); }
        else { return false; }
    }

    /**
     * Credits the given amount into a receiver's account as part of a transfer.
     * @param accountNumber_receiver the account number to receive the funds
     * @param amount                 the amount to credit
     * @return true if successful, false if not logged in
     */
    public boolean deposit_transferred(String accountNumber_receiver, float amount){
        if (loggedIn()) { return loggedInAccount.deposit_transferred(accountNumber_receiver, amount); }
        else { return false; }
    }

    /**
     * Retrieves the one-time withdrawal limit for the given account.
     * @param accountNumber the account number to query
     * @return the withdrawal limit, or 0 if not logged in
     */
    public float getWithdrawalLimit(String accountNumber){
        if(loggedIn()){ return sqlite.getWithdrawalLimit(accountNumber);}
        else{ return 0; }
    }

    /**
     * Updates the one-time withdrawal limit for the given account.
     * @param accountNumber   the account number to update
     * @param withdrawalLimit the new withdrawal limit
     */
    public void updateWithdrawalLimit(String accountNumber, float withdrawalLimit){
        if(loggedIn()){ sqlite.updateWithdrawalLimit(accountNumber, withdrawalLimit); }
    }

    /**
     * Withdraws the given amount from the logged in account.
     * @param amount the amount to withdraw
     * @return true if withdrawal was successful, false otherwise
     */
    public boolean withdraw(float amount){
        if (loggedIn()) { return loggedInAccount.withdraw(amount); }
        else { return false; }
    }

    /**
     * Debits the given amount from the logged in account as part of a transfer.
     * @param accountNumber_receiver the receiver's account number (for transaction record)
     * @param amount                 the amount to debit
     * @return true if successful, false otherwise
     */
    public boolean withdraw_transferred(String accountNumber_receiver, float amount){
        if (loggedIn()) { return loggedInAccount.withdraw_transferred(accountNumber_receiver, amount); }
        else { return false; }
    }

    /**
     * Returns the current balance of the logged in account.
     * @return the balance, or -1 if not logged in
     */
    public float getBalance(){
        if (loggedIn()) { return loggedInAccount.getBalance(); }
        else { return -1; } // use -1 as an indicator of an error
    }

    /**
     * Retrieves the full name of the customer associated with the given account number.
     * @param accountNumber the account number to look up
     * @return the customer's full name in uppercase, or empty string if not logged in
     */
    public String getName(String accountNumber){
        if(loggedIn()){ return sqlite.getName(accountNumber);}
        else{ return ""; }
    }

    /**
     * Retrieves the full transaction history for the given account number.
     * @param accountNumber the account number to query
     * @return a list of transaction records, each as a list of strings, or null if not logged in
     */
    public ArrayList<ArrayList<String>> getTransactionHistory(String accountNumber){
        if(loggedIn()){ return sqlite.getTransactionHistory(accountNumber); }
        else{ return null; }
    }

    /**
     * Retrieves all account and customer details for the given account number.
     * @param accountNumber the account number to query
     * @return a list of detail strings in a fixed order, or null if not logged in
     */
    public ArrayList<String> getAccountDetails(String accountNumber){
        if(loggedIn()){ return sqlite.getAccountDetails(accountNumber); }
        else{ return null; }
    }

    /**
     * Checks whether the given input matches the logged in account's PIN.
     * @param userInput the PIN entered by the user
     * @return true if the PIN matches, false otherwise
     */
    public boolean checkPin(String userInput){
        if(loggedIn()){ return loggedInAccount.getAccPasswd().equals(userInput); }
        else{ return false; }
    }

    /**
     * Updates the PIN for the given account in both the database and the in memory account.
     * @param accountNumber the account number to update
     * @param newPin        the new PIN string
     */
    public void updatePin(String accountNumber, String newPin){
        if(loggedIn()){ sqlite.updatePin(accountNumber, newPin); loggedInAccount.setAccPasswd(newPin);}
    }

    /**
     * Updates the account type, tier, and withdrawal limit for the given account.
     * @param accountNumber   the account to update
     * @param type            the new account type (Basic or Student)
     * @param tier            the new account tier (Normal, Pro, or Prime)
     * @param withdrawalLimit the new withdrawal limit
     */
    public void updatePlan(String accountNumber, String type, String tier, float withdrawalLimit){
        if(loggedIn()){ sqlite.updatePlan(accountNumber, type, tier, withdrawalLimit); }
    }
}
