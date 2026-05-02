package com.atmbanksimulator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <h1>ATM Simulator - Bank: JUnit Testing</h1>
 *
 * <p>
 * White Box Testing {@code JUnit} for the {@link Bank} class using JUnit 5.
 * Tests cover authentication, session management, and core transaction logic.
 * Requires a valid bank.db database with test accounts to be present during execution.
 * </p>
 *
 * <p>Test Accounts used:</p>
 * <ol>
 *   <li>Account Number: 12345678 | PIN: 1234</li>  - Valid Account
 *   <li>Account Number: 00000000 | PIN: 0000</li>  - Invalid Account
 * </ol>
 *
 * @author D'Souza, C. J.
 * @version 3.0
 */
class BankTest {

    private Bank bank;

    //Test Accounts
    private static final String VALID_ACC = "12345678";
    private static final String VALID_PIN = "1234";
    private static final String INVALID_ACC = "00000000";
    private static final String INVALID_PIN = "0000";

    /** Creates a new Bank instance before each test to ensure proper results. */
    @BeforeEach
    void setUp() {
        bank = new Bank();
    }

    /** Logs out after each test to reset the testing data. */
    @AfterEach
    void tearDown() {
        bank.logout();
    }

    /*----------------------------------------login_AccountExist()-----------------------------------------------*/

    /** Checks that a valid account number is recognised as existing. */
    @Test
    void login_AccountExist_Valid() {
        assertTrue(bank.login_AccountExist(VALID_ACC), "The valid account should be found in the database.");
    }

    /** Checks that an invalid account number is correctly rejected. */
    @Test
    void login_AccountExist_Invalid() {
        assertFalse(bank.login_AccountExist(INVALID_ACC), "The valid account should *NOT* be found in the database.");
    }

    /*--------------------------------------------login()----------------------------------------------------*/

    /** Checks that a valid account number with the correct PIN should return true. */
    @Test
    void login_ValidDetails() {
        assertTrue(bank.login(VALID_ACC, VALID_PIN), "Successful login.");
    }

    /** Checks that a valid account number with the wrong PIN should return false. */
    @Test
    void login_InvalidPIN() {
        assertFalse(bank.login(VALID_ACC, INVALID_PIN), "Unsuccessful login due to *INVALID* PIN.");
    }

    /** Checks that an invalid account number should return false regardless of PIN. */
    @Test
    void login_InvalidAccountNumber() {
        assertFalse(bank.login(INVALID_ACC, VALID_PIN), "Unsuccessful login due to *INVALID* Account Number.");
    }

    /*------------------------------------------loggedIn()-----------------------------------------------*/

    /** Checks that loggedIn() returns false before a login attempt. */
    @Test
    void loggedIn_BeforeLogin() {
        assertFalse(bank.loggedIn(), "Returns false as *NO* user is logged in.");
    }

    /** Checks that loggedIn() returns false after an unsuccessful login. */
    @Test
    void loggedIn_AfterFailedLogin() {
        bank.login(VALID_ACC, INVALID_PIN);
        assertFalse(bank.loggedIn(), "Returns false as *NO* user is logged in; despite a log in attempt.");
    }

    /** Checks that loggedIn() returns true after a successful login. */
    @Test
    void loggedIn_AfterLogin() {
        bank.login(VALID_ACC, VALID_PIN);
        assertTrue(bank.loggedIn(), "Returns true as a user is logged in.");
    }

    /*--------------------------------------------logout()-------------------------------------------------*/

    /** Checks that loggedIn() returns false after logout. */
    @Test
    void logout(){
        bank.login(VALID_ACC, VALID_PIN);
        bank.logout();
        assertFalse(bank.loggedIn(), "Returns false as *NO* user is logged in; implying logging out is successful.");
    }

    /*------------------------------------------getBalance()-----------------------------------------------*/

    /** Checks that getBalance() returns a positive balance after login. */
    @Test
    void getBalance_LoggedIn() {
        bank.login(VALID_ACC, VALID_PIN);
        assertTrue(bank.getBalance()>0, "Account has a balance greater than £0.");
    }

    /** Checks that getBalance() returns -1 when no user is logged in. */
    @Test
    void getBalance_NotLoggedIn() {
        assertEquals(-1, bank.getBalance(), "Returns -1 as *NO* user is logged in.");
    }

    /*------------------------------------------deposit()-----------------------------------------------*/

    /** Checks that depositing a valid positive amount increases the balance correctly. */
    @Test
    void deposit_ValidAmount() {
        bank.login(VALID_ACC, VALID_PIN);
        float balanceBefore = bank.getBalance();
        assertTrue(bank.deposit(50), "Successful deposit as amount is positive/valid.");
        assertEquals(balanceBefore + 50, bank.getBalance(),"Balance should increase by the deposited amount.");
        bank.withdraw(50);
    }

    /** Checks that depositing an invalid amount returns false and the balance remains the same. */
    @Test
    void deposit_InvalidAmount() {
        bank.login(VALID_ACC, VALID_PIN);
        float balanceBefore = bank.getBalance();
        assertFalse(bank.deposit(-50), "Unsuccessful deposit as amount is negative/invalid.");
        assertEquals(balanceBefore, bank.getBalance(),"Balance should remain unchanged after a failed deposit.");
    }

    /** Checks that depositing a valid positive amount returns false as no account is logged in. */
    @Test
    void deposit_NotLoggedIn() {
        assertFalse(bank.deposit(50),"Unsuccessful deposit as *NO* account is logged in.");
    }


    /*------------------------------------------withdraw()-----------------------------------------------*/

    /** Checks that withdrawing a valid positive amount decreases the balance correctly. */
    @Test
    void withdraw_ValidAmount() {
        bank.login(VALID_ACC, VALID_PIN);
        float balanceBefore = bank.getBalance();
        assertTrue(bank.withdraw(50), "Successful withdrawal as amount is positive & valid.");
        assertEquals(balanceBefore - 50, bank.getBalance(),"Balance should decrease by the withdrawn amount.");
        bank.deposit(50);
    }

    /** Checks that withdrawing a negative amount returns false and the balance remains the same. */
    @Test
    void withdraw_InvalidAmount_NegativeValue() {
        bank.login(VALID_ACC, VALID_ACC);
        float balanceBefore = bank.getBalance();
        assertFalse(bank.withdraw(-10), "Unsuccessful withdrawal as amount is negative.");
        assertEquals(balanceBefore, bank.getBalance(),"Balance should remain unchanged after a failed withdrawal.");
    }

    /** Checks that withdrawing an amount that exceeds the account balance returns false and the balance remains the same. */
    @Test
    void withdraw_InvalidAmount_ExceedsBalance() {
        bank.login(VALID_ACC, VALID_ACC);
        float balanceBefore = bank.getBalance();
        assertFalse(bank.withdraw(10000), "Unsuccessful withdrawal as amount exceeds the account balance.");
        assertEquals(balanceBefore, bank.getBalance(),"Balance should remain unchanged after a failed withdrawal.");
    }

    /** Checks that withdrawing an amount that fails to maintain a minimum balance of £20 returns false and the balance remains the same. */
    @Test
    void withdraw_InvalidAmount_BelowMinimumBalance() {
        bank.login(VALID_ACC, VALID_ACC);
        float balanceBefore = bank.getBalance();
        assertFalse(bank.withdraw(730), "Unsuccessful withdrawal as account fails to maintain a minimum balance of £20.");
        assertEquals(balanceBefore, bank.getBalance(),"Balance should remain unchanged after a failed withdrawal.");
    }

    /** Checks that withdrawing a valid positive amount returns false as no account is logged in. */
    @Test
    void withdraw_NotLoggedIn() {
        assertFalse(bank.withdraw(50), "Unsuccessful withdrawal as *NO* account is logged in.");
    }

    /*------------------------------------------checkPin()-----------------------------------------------*/

    /** Checks that checkPin() returns true when the correct PIN is entered. */
    @Test
    void checkPin_CorrectPIN() {
        bank.login(VALID_ACC, VALID_PIN);
        assertTrue(bank.checkPin(VALID_PIN),"Returns true as PIN is correct.");
    }

    /** Checks that checkPin() returns false when an incorrect PIN is entered. */
    @Test
    void checkPin_IncorrectPIN() {
        bank.login(VALID_ACC, VALID_PIN);
        assertFalse(bank.checkPin(INVALID_PIN),"Returns false as PIN is incorrect.");
    }

    /** Checks that checkPin() returns false when no account is logged in. */
    @Test
    void checkPin_NotLoggedIn() {
        assertFalse(bank.checkPin(VALID_PIN), "Returns false as *NO* account is logged in.");
    }

}