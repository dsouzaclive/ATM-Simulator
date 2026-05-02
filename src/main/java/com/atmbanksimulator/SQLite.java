package com.atmbanksimulator;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * <h1>ATM Simulator - SQLite Database: Memory</h1>
 *
 * <p>
 * The {@code SQLite} class manages all direct interactions with the SQLite database.
 * It provides methods to query and update account details, customer information, transaction history, PINs, plans,
 * and withdrawal limits. It serves as the data layer for the entire ATM system, used by {@link Bank} and {@link BankAccount}.
 * </p>
 *
 * @author D'Souza, C. J.
 * @version 3.0
 */
public class SQLite {
    private Connection conn;
    private String query;

    /**
     * Establishes a connection to the SQLite database at the configured URL.
     * Prints "Success" to the console on a successful connection.
     * @throws RuntimeException if the database connection fails
     */
    public SQLite(){
        String url = "jdbc:sqlite:src/main/resources/bank.db";  //URL for the Bank Database (SQLite DB)
        try { conn = DriverManager.getConnection(url); }    //Connection object for the Bank DB
        catch (SQLException e) { throw new RuntimeException(e); }
        System.out.println("Success");
    }

    /**
     * Checks whether an account with the given number exists in the AccountDetails table.
     * @param accountNumber the account number to search for
     * @return {@code true} if found, {@code false} otherwise
     * @throws RuntimeException if a database error occurs
     */
    public boolean checkAccountExist (String accountNumber){
        query = String.format("SELECT accountNumber FROM AccountDetails WHERE accountNumber = %d", convertToInt(accountNumber));
        try(Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)){
            while(rs.next()){
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the stored PIN for the given account number.
     * @param accountNumber the account number to query
     * @return the PIN string, or an empty string if not found
     * @throws RuntimeException if a database error occurs
     */
    public String getPasswd(String accountNumber){
        query = String.format("SELECT pin FROM AccountDetails WHERE accountNumber = %d", convertToInt(accountNumber));
        try(Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)){
            while(rs.next()){
                return rs.getString("pin");
            }
            return "";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the current balance for the given account number.
     * @param accountNumber the account number to query
     * @return the balance in GBP (£), or {@code -1} if not found
     * @throws RuntimeException if a database error occurs
     */
    public float getBalance(String accountNumber){
        query = String.format("SELECT balance FROM AccountDetails WHERE accountNumber = %d", convertToInt(accountNumber));
        try(Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)){
            while(rs.next()){
                return rs.getFloat("balance");
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the balance for the given account number in the database.
     * @param accountNumber the account number to update
     * @param balance       the new balance value in GBP (£)
     * @throws RuntimeException if a database error occurs
     */
    public void changeBalance(String accountNumber, float balance){
        query = String.format("UPDATE AccountDetails SET balance = %f WHERE accountNumber = %d", balance, convertToInt(accountNumber));
        try(Statement stmt = conn.createStatement()){
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the full name of the customer linked to the given account number, formed by
     * joining the Customer and AccountDetails tables.
     * @param accountNumber the account number to look up
     * @return the customer's full name in uppercase, or an empty string if not found
     * @throws RuntimeException if a database error occurs
     */
    public String getName(String accountNumber){
        query = String.format(
                "SELECT c.firstName, c.lastName FROM Customer c INNER JOIN AccountDetails a " +
                "ON c.customerID=a.customerID WHERE a.accountNumber = %d",
                convertToInt(accountNumber)
        );
        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)){
            while(rs.next()){
                return rs.getString("firstName").toUpperCase() + " " + rs.getString("lastName").toUpperCase();
            }
            return "";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Inserts a new transaction record into the TransactionHistory table and
     * updates the recentActivity timestamp on the associated account.
     * @param accountNumber the account number the transaction belongs to
     * @param type          the type of transaction (e.g. "Deposit", "Withdrawal")
     * @param amount        the transaction amount in GBP (£)
     * @throws RuntimeException if a database error occurs
     */
    public void insertTransaction (String accountNumber, String type, Float amount){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = now.format(formatter);
        query = String.format(
                "INSERT INTO TransactionHistory(accountNumber, typeOfTransaction, amount, timeOfTransaction) VALUES(%d, '%s', %f, '%s')",
                convertToInt(accountNumber), type, amount, dateTime
        );
        try(Statement stmt = conn.createStatement()){
            stmt.executeUpdate(query);
            updateRecentActivity(accountNumber, dateTime);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the recentActivity timestamp for the given account number.
     * Called automatically after every transaction insertion.
     * @param accountNumber the account number to update
     * @param dateTime      the formatted datetime string to store
     * @throws RuntimeException if a database error occurs
     */
    public void updateRecentActivity(String accountNumber, String dateTime){
        query = String.format(
                "UPDATE AccountDetails SET recentActivity = '%s' WHERE accountNumber = %d",
                dateTime, convertToInt(accountNumber)
        );
        try(Statement stmt = conn.createStatement()){
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the full transaction history for the given account number, ordered by most recent first.
     * @param accountNumber the account number to query
     * @return a list of transaction records, each containing transactionID, timeOfTransaction, typeOfTransaction, and amount as strings
     * @throws RuntimeException if a database error occurs
     */
    public ArrayList<ArrayList<String>> getTransactionHistory(String accountNumber){
        ArrayList<ArrayList<String>> history = new ArrayList<>();

        query = String.format(
                "SELECT transactionID, timeOfTransaction, typeOfTransaction, amount FROM TransactionHistory WHERE accountNumber = %d ORDER BY timeOfTransaction DESC",
                convertToInt(accountNumber)
        );
        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)){
            while(rs.next()){
                ArrayList<String> record = new ArrayList<>();
                int id = rs.getInt("transactionID");
                String dateTime = rs.getString("timeOfTransaction");
                String type = rs.getString("typeOfTransaction");
                Float amount = rs.getFloat("amount");
                record.add(String.valueOf(id)); record.add(dateTime); record.add(type); record.add(String.valueOf(amount));
                history.add(record);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return history;
    }

    /**
     * Retrieves all account and personal details for the given account number by joining the Customer,
     * AccountDetails, and TransactionHistory tables. Returns only the most recent transaction record via LIMIT 1.
     * @param accountNumber the account number to query
     * @return a list of strings in fixed positional order: account number,
     *         customerID, name, DOB, phone, email, address, postcode,
     *         monthlyIncome, sortcode, accountType, accountTier, balance,
     *         recentActivity, lastTransactionType, withdrawalLimit
     * @throws RuntimeException if a database error occurs
     */
    public ArrayList<String> getAccountDetails(String accountNumber){
        ArrayList<String> accountDetails = new ArrayList<>();

        query = String.format(
                "SELECT c.customerID, c.firstName, c.middleName, c.lastName, c.DOB, " +
                "a.sortcode, a.accountType, a.accountTier, a.balance, a.recentActivity, " +
                "t.typeOfTransaction, c.phoneNumber, c.email, c.address, c.postcode, c.monthlyIncome, a.withdrawalLimit " +
                "FROM Customer c LEFT JOIN AccountDetails a ON c.customerID=a.customerID " +
                "LEFT JOIN TransactionHistory t ON a.accountNumber=t.accountNumber " +
                "WHERE a.accountNumber = %d ORDER BY t.timeOfTransaction DESC LIMIT 1", convertToInt(accountNumber)
        );

        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)){
            while(rs.next()){
                int customerID = rs.getInt("CustomerID");
                String name = rs.getString("firstName") + " " + rs.getString("middleName") + " " + rs.getString("lastName");
                String DOB = rs.getString("DOB");
                String sortcode = rs.getString("sortcode");
                String accountType = rs.getString("accountType");
                String accountTier = rs.getString("accountTier");
                float balance = rs.getFloat("balance");
                String lastActivity = rs.getString("recentActivity");
                String lastActivityType = rs.getString("typeOfTransaction");
                String phoneNumber = rs.getString("phoneNumber");
                String email = rs.getString("email");
                String address = rs.getString("address");
                String postcode = rs.getString("postcode");
                float monthlyIncome = rs.getFloat("monthlyIncome");
                float withdrawalLimit = rs.getFloat("withdrawalLimit");

                //Account Number
                accountDetails.add(accountNumber);

                //Personal Information
                accountDetails.add(String.valueOf(customerID));
                accountDetails.add(name);
                accountDetails.add(DOB);
                accountDetails.add(phoneNumber);
                accountDetails.add(email);
                accountDetails.add(address);
                accountDetails.add(postcode);
                accountDetails.add(String.valueOf(monthlyIncome));

                //Account Details
                accountDetails.add(sortcode);
                accountDetails.add(accountType);
                accountDetails.add(accountTier);
                accountDetails.add(String.valueOf(balance));
                accountDetails.add(lastActivity);
                accountDetails.add(lastActivityType);
                accountDetails.add(String.valueOf(withdrawalLimit));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return accountDetails;
    }

    /**
     * Updates the PIN for the given account number in the database.
     * @param accountNumber the account number to update
     * @param pin           the new PIN string
     * @throws RuntimeException if a database error occurs
     */
    public void updatePin(String accountNumber, String pin){
        query = String.format(
                "UPDATE AccountDetails SET pin = '%s' WHERE accountNumber = %d",
                pin, convertToInt(accountNumber)
        );
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the account type, tier, and withdrawal limit for the given account number.
     * @param accountNumber   the account number to update
     * @param type            the new account type — {@code "Basic"} or {@code "Student"}
     * @param tier            the new account tier — {@code "Normal"}, {@code "Pro"}, or {@code "Prime"}
     * @param withdrawalLimit the new withdrawal limit in GBP (£)
     * @throws RuntimeException if a database error occurs
     */
    public void updatePlan(String accountNumber, String type, String tier, float withdrawalLimit){
        query = String.format(
                "UPDATE AccountDetails SET accountType = '%s', accountTier = '%s', withdrawalLimit = %f WHERE accountNumber = %d",
                type, tier, withdrawalLimit, convertToInt(accountNumber)
        );
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the current one-time withdrawal limit for the given account number.
     * @param accountNumber the account number to query
     * @return the withdrawal limit in GBP (£), or {@code 0} if not found
     * @throws RuntimeException if a database error occurs
     */
    public float getWithdrawalLimit(String accountNumber){
        query = String.format(
                "SELECT withdrawalLimit FROM AccountDetails WHERE accountNumber = %d", convertToInt(accountNumber)
        );
        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)){
            while(rs.next()){
                return rs.getFloat("withdrawalLimit");
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the one-time withdrawal limit for the given account number in the database.
     * @param accountNumber   the account number to update
     * @param withdrawalLimit the new withdrawal limit in GBP (£)
     * @throws RuntimeException if a database error occurs
     */
    public void updateWithdrawalLimit(String accountNumber, float withdrawalLimit){
        query = String.format(
                "UPDATE AccountDetails SET withdrawalLimit = %f WHERE accountNumber = %d",
                withdrawalLimit, convertToInt(accountNumber)
        );
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a numeric string to an integer for use in SQL queries.
     * @param input the string to convert
     * @return the integer value of the string
     * @throws NumberFormatException if the string is not a valid integer
     */
    private int convertToInt(String input){ return Integer.parseInt(input); }
}
