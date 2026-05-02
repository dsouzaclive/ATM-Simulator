package com.atmbanksimulator;

/**
 * <h1>ATM Simulator - Bank Account</h1>
 *
 * <p>
 * The {@code BankAccount} class represents a single bank account within the ATM system.
 * It stores the account credentials and balance, and provides methods to deposit, withdraw, and transfer funds.
 * All balance changes are sent to the {@link SQLite} database and recorded as transactions.
 * </p>
 *
 * @author D'Souza, C. J.
 * @version 3.0
 */
public class BankAccount {
    private String accNumber = "";
    private String accPasswd = "";
    private float balance = 0;
    private SQLite sqlite;

    /** Default constructor. Creates an empty {@code BankAccount2} instance with no credentials or balance set. */
    public BankAccount() {}

    /**
     * Constructs a {@code BankAccount2} with the given credentials, balance, and database connection.
     * @param a      the account number
     * @param p      the account PIN/password
     * @param b      the initial balance in GBP (£)
     * @param sqlite the {@link SQLite} instance used for database operations
     */
    public BankAccount(String a, String p, float b, SQLite sqlite) {
        this.accNumber = a;
        this.accPasswd = p;
        this.balance = b;
        this.sqlite = sqlite;
    }

    /**
     * Withdraws the given amount from this account. The withdrawal will be rejected if the amount is negative,
     * exceeds the current balance, or would leave the balance below the minimum of £20.
     * On success, the balance is updated in the database and a transaction record is inserted.
     * @param amount the amount to withdraw in GBP (£)
     * @return {@code true} if the withdrawal was successful, {@code false} otherwise
     */
    public boolean withdraw( float amount ) {
        if (amount < 0 || balance < amount) {
            return false;
        } else {
            if(balance-amount<=20){ return false; }
            else{
                balance = balance - amount;  // subtract amount from balance
                sqlite.changeBalance(accNumber, balance);   //updates the balance in the DB
                sqlite.insertTransaction(accNumber, "Withdrawal", amount);  //inserts the transaction into the DB
                return true;
            }
        }
    }

    /**
     * Debits the given amount from this account as part of an outgoing transfer.
     * Applies the same minimum balance and validation rules as {@link #withdraw(float)}.
     * On success, the balance is updated in the database and a transaction record is inserted referencing the receiver's account number.
     * @param accountNumber_receiver the account number of the transfer recipient, used in the transaction record
     * @param amount                 the amount to debit in GBP (£)
     * @return {@code true} if the debit was successful, {@code false} otherwise
     */
    public boolean withdraw_transferred(String accountNumber_receiver, float amount) {
        if (amount < 0 || balance < amount) {
            return false;
        } else {
            if(balance-amount<=20){ return false; }
            else{
                balance = balance - amount;  // subtract amount from balance
                sqlite.changeBalance(accNumber, balance);   //updates the balance in the DB
                sqlite.insertTransaction(accNumber, "Sent to Account Number " + accountNumber_receiver, amount);  //inserts the transaction into the DB
                return true;
            }
        }
    }

    /**
     * Deposits the given amount into this account. The deposit will be rejected if the amount is negative.
     * On success, the balance is updated in the database and a transaction record is inserted.
     * @param amount the amount to deposit in GBP (£)
     * @return {@code true} if the deposit was successful, {@code false} if the amount is negative
     */
    public boolean deposit( float amount ) {
        if (amount < 0) {
            return false;
        } else {
            balance = balance + amount;  // add amount to balance
            sqlite.changeBalance(accNumber, balance);   //updates the balance in the DB
            sqlite.insertTransaction(accNumber, "Deposit", amount);  //inserts the transaction into the DB
            return true;
        }
    }

    /**
     * Credits the given amount to a receiver's account as part of an incoming transfer.
     * This method does not modify the current account's balance — it updates the receiver's balance directly in the
     * database and inserts a transaction record for the receiver referencing this account number.
     * @param accountNumber_receiver the account number to credit
     * @param amount                 the amount to credit in GBP (£)
     * @return {@code true} if the credit was successful, {@code false} if the amount is negative
     */
    public boolean deposit_transferred (String accountNumber_receiver, float amount){
        if (amount < 0) {
            return false;
        } else {
            float balance_receiver = sqlite.getBalance(accountNumber_receiver) + amount;  // add amount to balance
            sqlite.changeBalance(accountNumber_receiver, balance_receiver);   //updates the balance in the DB
            sqlite.insertTransaction(accountNumber_receiver, "Received from Account Number " + accNumber, amount);  //inserts the transaction into the DB
            return true;
        }
    }

    /**
     * Returns the PIN/password associated with this account.
     * @return the account PIN as a string
     */
    public String getAccPasswd(){
        return accPasswd;
    }

    /**
     * Updates the in memory PIN/password for this account.
     * This does not persist to the database — use {@link SQLite#updatePin(String, String)} for database persistence.
     * @param passwd the new PIN string to set
     */
    public void setAccPasswd(String passwd){
        this.accPasswd = passwd;
    }

    /**
     * Returns the current in memory balance for this account.
     * @return the current balance in GBP (£)
     */
    public float getBalance() {
        return balance;
    }
}
