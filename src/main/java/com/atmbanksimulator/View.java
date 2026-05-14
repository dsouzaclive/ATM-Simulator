package com.atmbanksimulator;

import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * <h1>ATM Simulator - View: User Interface (UI)</h1>
 *
 * <p>
 * The {@code View} class represents the user interface of the ATM system.
 * It is responsible for constructing and displaying all JavaFX UI components including
 * the numpad, menus, transaction history, account details, and settings screens.
 * It accepts user input via button clicks and forwards these actions to the {@link Controller}.
 * The display is updated by the {@link UIModel} through the {@code update()} method.
 * </p>
 *
 * @author D'Souza, C. J.
 * @version 3.0
 */
public class View {
    int H = 750;
    int W = 1300;

    Controller controller;

    BorderPane root;
    HBox top;
    VBox left;
    VBox center;
    HBox bottom;
    GridPane right;

    private ArrayList<ArrayList<String>> history;
    private ArrayList<String> accountDetails;

    // Brightness control
    private ColorAdjust colorAdjust;
    private double brightnessLevel = 0.0; // Range: -0.6 (dark) to 0.6 (bright)

    /**
     * Initialises and launches the JavaFX stage, constructing all UI regions and applying the stylesheet.
     * @param stage the primary JavaFX stage provided by the application framework
     */
    public void start(Stage stage) {
        //title
        stage.setTitle("ATM v4.0");
        // Initialize color adjustment for brightness
        colorAdjust = new ColorAdjust();//This creates the ColorAdjust new is just the keyword to create to create this instance
        colorAdjust.setBrightness(brightnessLevel);//This sets the starting brightness

        //main components
        root = new BorderPane();
        root.setEffect(colorAdjust); // Apply brightness effect to entire UI
        Scene scene = new Scene(root, W, H);

        top();

        left();

        center();
        headingPane = new HBox();
        laHeading = new Label(); laHeading.setId("label_heading");
        homeBtn = new Button();
        settingsBtn = new Button();
        la = new Label(); la.setId("label_heading");
        tf = new TextField(); tf.setEditable(false);
        box = new VBox();
        VBox.setVgrow(box, Priority.ALWAYS);

        right();

        bottom();

        //styling
        scene.getStylesheets().add("atm.css"); // tell to use our css file

        //display
        root.setPadding(new Insets(5));
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    /*----------------------------------AUDIO FEEDBACK------------------------------------------*/

    /**
     * Generates a beep sound programmatically
     */
    private void playBeep() {
        new Thread(() -> { //This generates a beep sound from scratch using math
            try {
                int sampleRate = 16000;// how many audio sample it plays
                int duration = 100; //long beep last milliseconds
                int frequency = 800; //the pitch of beep in Hz

                byte[] buf = new byte[sampleRate * duration / 1000];// creates an array to hold sound data
                for (int i = 0; i < buf.length; i++) { // it keep counting from one keep going till i is leass than lenth of array 1600

                    double angle = 2.0 * Math.PI * i * frequency / sampleRate;
                    buf[i] = (byte) (Math.sin(angle) * 80);//Math.sin draws a smooth wave (like a wiggling line), and * 80 controls the volume
                }

                AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                sdl.write(buf, 0, buf.length);
                sdl.drain();
                sdl.close();
            } catch (Exception e) {
                System.out.println("Audio error: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Speaks the given text using system text-to-speech
     * Falls back to console output if TTS unavailable
     */
    private void speakText(String text) {
        new Thread(() -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;

                if (os.contains("win")) {
                    // Windows PowerShell TTS
                    String command = String.format(
                            "Add-Type -AssemblyName System.Speech; " +
                                    "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                                    "$synth.Rate = 3; " +
                                    "$synth.Speak('%s')", text);
                    pb = new ProcessBuilder("powershell", "-Command", command);
                } else if (os.contains("mac")) {
                    // macOS say command
                    pb = new ProcessBuilder("say", "-r", "250", text);
                } else {
                    // Linux espeak
                    pb = new ProcessBuilder("espeak", "-s", "180", text);
                }

                pb.inheritIO();
                Process process = pb.start();
                process.waitFor();
            } catch (Exception e) {
                System.out.println("Speech output: " + text);
            }
        }).start();
    }

    /**
     * Plays beep and speaks the button label
     */
    private void audioFeedback(String buttonLabel) {
        playBeep();
        speakText(buttonLabel);
    }

    /*----------------------------------BRIGHTNESS CONTROL--------------------------------------*/

    private void increaseBrightness() {
        brightnessLevel = Math.min(0.6, brightnessLevel + 0.1);
        colorAdjust.setBrightness(brightnessLevel);
        audioFeedback("Brighter");//Each click moves brightness by one step and then gives audio feedback so a visually impaired user knows the button worked. decreaseBrightness does the same thing in reverse, capped at -0.8 so the screen never goes fully black.
    }

    private void decreaseBrightness() {
        brightnessLevel = Math.max(-0.6, brightnessLevel - 0.1); // Don't go fully dark
        colorAdjust.setBrightness(brightnessLevel);
        audioFeedback("Dimmer");// it makes the screen dimmer
    }

    /*----------------------------------------TOP-----------------------------------------------*/
    /** Builds the top navigation bar containing the ATM title label. */
    protected void top(){
        top = new HBox();
        top.setId("top");
        top.setMinHeight(50);

        Label laTitle = new Label("ATM v4.0");
        laTitle.setId("label_heading");
        laTitle.setPadding(new Insets(8));

        // Spacer to push brightness controls to the right
        Region spacer = new Region();//A Region is an invisible blank element.
        HBox.setHgrow(spacer, Priority.ALWAYS);//tells it to stretch and fill all available horizontal space

        // Brightness control buttons
        Button brighterBtn = new Button("☀+");//creates the button with that label
        brighterBtn.setOnAction(e -> increaseBrightness());//when clicked, call the brightness method. The e -> part is a lambda, just a shorthand way of saying "when this event happens, do this"
        //this calls decreaseBrightness() instead.
        Button dimmerBtn = new Button("☀-");
        dimmerBtn.setOnAction(e -> decreaseBrightness());

        HBox brightnessControls = new HBox(5);//a horizontal box that arranges its children in a row, with 5 pixels of gap between them
        brightnessControls.setAlignment(Pos.CENTER);//centres everything inside it vertically and horizontally
        brightnessControls.setPadding(new Insets(8));//adds 8 pixels of breathing room around the edges
        brightnessControls.getChildren().addAll(dimmerBtn, brighterBtn);//puts the dim button first, then the bright button, so they appear as ☀- ☀+ left to right

        top.getChildren().addAll(laTitle, spacer, brightnessControls);

        root.setTop(top);
    }

    /*----------------------------------------left----------------------------------------------*/
    /** Builds the left border strip used as a visual effect. */
    protected void left(){
        left = new VBox();
        left.setId("left");
        left.setMinWidth(15);

        root.setLeft(left);
    }

    /*---------------------------------------CENTER-----------------------------------------------*/
    /** Builds the center content region and displays the home screen on startup. */
    protected void center(){
        center = new VBox();
        center.setId("center");

        VBox.setVgrow(center, Priority.ALWAYS);

        showHomeScreen();

        root.setCenter(center);
    }

    /** Clears the center region and displays the home screen with the START button. */
    protected void showHomeScreen(){
        center.getChildren().clear();
        center.getChildren().add(homeScreen());
    }

    /**
     * Clears the center region and displays the main ATM interaction screen
     * containing the heading, separator, label, text field, and content box.
     */
    protected void showScreen(){
        center.getChildren().clear();
        center.getChildren().add(screen());
    }

    /*----------------------------------CUSTOM CENTER SCREENS------------------------------------*/
    /**
     * Constructs and returns the home screen pane containing the welcome message and START button.
     * @return a {@link Pane} representing the home screen
     */
    protected Pane homeScreen(){
        VBox pane = new VBox();
        pane.setMinWidth(350);
        pane.setPadding(new Insets(10));
        pane.setAlignment(Pos.TOP_CENTER);

        Label laTitle = new Label("Welcome to the ATM");
        laTitle.setId("label_heading");

        Button startButton = new Button("START");
        startButton.setOnAction(e -> { showScreen(); controller.process("START"); });

        pane.getChildren().addAll(laTitle, startButton);

        return pane;
    }

    private HBox headingPane;
    private Label laHeading;
    private Button settingsBtn;
    private Button homeBtn;
    private Label la;
    private TextField tf;
    private VBox box;

    /**
     * Constructs and returns the main ATM screen pane, including the heading bar with home and settings buttons,
     * a separator, the message label, the input text field, and the content box.
     * @return a {@link Pane} representing the main ATM screen
     */
    protected Pane screen(){
        VBox pane = new VBox();
        pane.setMinWidth(350);
        pane.setPadding(new Insets(10));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ImageView imgSettings = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/settings.png")),35,35,false,false));
        settingsBtn = new Button();
        settingsBtn.setId("Settings");
        settingsBtn.setVisible(false);
        settingsBtn.setGraphic(imgSettings);
        settingsBtn.setOnAction(this::idButtonClicked);

        ImageView imgHome = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/home.png")),35,35,false,false));
        homeBtn = new Button();
        homeBtn.setId("Home");
        homeBtn.setVisible(false);
        homeBtn.setGraphic(imgHome);
        homeBtn.setOnAction(this::idButtonClicked);

        headingPane.getChildren().clear();
        headingPane.getChildren().addAll(laHeading, spacer, settingsBtn, homeBtn);

        Separator separator = new Separator();
        separator.setPrefHeight(3);
        separator.setId("separater");

        VBox.setVgrow(box, Priority.ALWAYS);
        pane.getChildren().addAll(headingPane, separator, la, tf, box);

        return pane;
    }

    /**
     * Constructs and returns the user main menu with buttons for Deposit, Withdraw, Balance, Transfer, and History.
     * @return a {@link Pane} containing the main menu buttons
     */
    protected Pane userMenu(){
        GridPane pane = new GridPane();
        pane.setMinWidth(350);
        pane.setPadding(new Insets(10));

        pane.setHgap(20);
        pane.setVgap(20);

        Button depositBtn = new Button("Deposit");
        Button withdrawBtn = new Button("Withdraw");
        Button balanceBtn = new Button("Balance");
        Button transferBtn = new Button("Transfer");
        Button historyBtn = new Button("History");

        Button[] buttons = {depositBtn, withdrawBtn, balanceBtn, transferBtn, historyBtn};

        for(Button b : buttons){
            b.setPrefSize(250, 110);
            b.setId("menu_button");
            b.setOnAction(this::buttonClicked);
        }

        int col = 0;

        pane.add(depositBtn, 0, col); pane.add(withdrawBtn, 1, col++);
        pane.add(balanceBtn, 0, col); pane.add(transferBtn, 1, col++);
        pane.add(historyBtn, 0, col);

        pane.setAlignment(Pos.CENTER);

        return pane;
    }

    /**
     * Constructs and returns the deposit menu with predefined amount buttons (£5, £10, £20, £50, £100, £200).
     * @return a {@link Pane} containing the deposit amount buttons
     */
    protected Pane depositMenu(){
        GridPane pane = new GridPane();
        pane.setMinWidth(350);
        pane.setPadding(new Insets(10));

        pane.setHgap(20);
        pane.setVgap(20);

        Button btn1 = new Button("5"); btn1.setId("deposit_5"); btn1.setOnAction(this::idButtonClicked);
        Button btn2 = new Button("10"); btn2.setId("deposit_10"); btn2.setOnAction(this::idButtonClicked);
        Button btn3 = new Button("20"); btn3.setId("deposit_20");btn3.setOnAction(this::idButtonClicked);
        Button btn4 = new Button("50"); btn4.setId("deposit_50"); btn4.setOnAction(this::idButtonClicked);
        Button btn5 = new Button("100"); btn5.setId("deposit_100"); btn5.setOnAction(this::idButtonClicked);
        Button btn6 = new Button("200"); btn6.setId("deposit_200"); btn6.setOnAction(this::idButtonClicked);

        Button[] buttons = {btn1, btn2, btn3, btn4, btn5, btn6};

        for(Button b : buttons){
            b.setPrefSize(250, 110);
            b.setStyle("-fx-font-size: 30px; -fx-font-weight: bold");
        }

        int col = 0;
        pane.add(btn1, 0, col); pane.add(btn2, 1, col++);
        pane.add(btn3, 0, col); pane.add(btn4, 1, col++);
        pane.add(btn5, 0, col); pane.add(btn6, 1, col++);

        pane.setAlignment(Pos.CENTER);
        return pane;
    }

    /**
     * Constructs and returns a scrollable transaction history view, displaying all records retrieved from the {@link Controller}.
     * @return a {@link ScrollPane} containing the formatted transaction history
     */
    protected ScrollPane transactionHistoryMenu(){
        VBox pane = new VBox();
        ScrollPane scrollPane = new ScrollPane(pane);
        pane.setMinWidth(350);
        pane.setPadding(new Insets(10));

        GridPane headings = new GridPane();

        ColumnConstraints colNo = new ColumnConstraints(); colNo.setMinWidth(25);
        ColumnConstraints colTime = new ColumnConstraints(); colTime.setMinWidth(280);
        ColumnConstraints colType = new ColumnConstraints(); colType.setMinWidth(360);
        ColumnConstraints colAmount = new ColumnConstraints(); colAmount.setMinWidth(200);

        headings.getColumnConstraints().addAll(colNo, colTime, colType, colAmount);

        Label laNo = new Label("No.");
        Label laTime = new Label("Date");
        Label laType = new Label("Type Of Transaction");
        Label laAmount = new Label("Amount (£)");

        headings.add(laNo,0,0); headings.add(laTime,1,0); headings.add(laType, 2, 0); headings.add(laAmount, 3, 0);
        headings.setHgap(20);
        headings.setVgap(8);
        headings.setPadding(new Insets(15));

        Separator separator = new Separator();
        separator.setPrefHeight(3);
        separator.setId("separater");

        pane.getChildren().addAll(headings, separator);

        ArrayList<ArrayList<String>> history = controller.getTransactionHistory();

        for(ArrayList<String> record : history){
            System.out.println(record);
            pane.getChildren().add(transactionRecord(record));
        }

        history.clear();

        pane.setAlignment(Pos.CENTER);
        return scrollPane;
    }

    /**
     * Constructs and returns a single row in the transaction history table.
     * @param record a list of strings containing transactionID, datetime, type, and amount
     * @return a {@link GridPane} row representing one transaction record
     */
    protected GridPane transactionRecord(ArrayList<String> record){
        GridPane pane = new GridPane();

        ColumnConstraints colNo = new ColumnConstraints(); colNo.setMinWidth(25);
        ColumnConstraints colTime = new ColumnConstraints(); colTime.setMinWidth(280);
        ColumnConstraints colType = new ColumnConstraints(); colType.setMinWidth(360);
        ColumnConstraints colAmount = new ColumnConstraints(); colAmount.setMinWidth(200);

        pane.getColumnConstraints().addAll(colNo, colTime, colType, colAmount);

        Label laNo = new Label(record.get(0));
        Label laTime = new Label(record.get(1));
        Label laType = new Label(record.get(2));
        Label laAmount = new Label(record.get(3));

        pane.add(laNo,0,0); pane.add(laTime,1,0); pane.add(laType, 2, 0); pane.add(laAmount, 3, 0);
        pane.setHgap(20);
        pane.setVgap(8);
        pane.setPadding(new Insets(15));

        return pane;
    }

    /**
     * Constructs and returns the settings menu with buttons for Account Details, Change Password,
     * Change Plan, and Change Withdrawal Limit.
     * @return a {@link Pane} containing the settings menu buttons
     */
    protected Pane settingsMenu(){
        GridPane pane = new GridPane();
        pane.setMinWidth(350);
        pane.setPadding(new Insets(10));

        pane.setHgap(20);
        pane.setVgap(20);

        Button accountDetailsBtn = new Button("Account Details");
        accountDetailsBtn.setOnAction(this::buttonClicked);

        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setOnAction(this::buttonClicked);

        Button changePlanBtn = new Button("Change Plan");
        changePlanBtn.setOnAction(this::buttonClicked);

        Button changeWithdrawalLimit = new Button("Change Withdrawal Limit");
        changeWithdrawalLimit.setOnAction(this::buttonClicked);

        accountDetails = new ArrayList<>(controller.getAccountDetails());

        Button[] buttons = {accountDetailsBtn, changePasswordBtn, changePlanBtn, changeWithdrawalLimit};

        for(Button b : buttons){
            b.setPrefSize(250, 110);
            b.setStyle("-fx-font-size: 25");
            b.setWrapText(true);
            b.setId("menu_button");
        }

        int col = 0;

        pane.add(accountDetailsBtn, 0, col); pane.add(changePasswordBtn, 1, col++);
        pane.add(changePlanBtn, 0, col); pane.add(changeWithdrawalLimit, 1, col++);

        pane.setAlignment(Pos.CENTER);

        return pane;
    }

    /**
     * Constructs and returns a scrollable account details view, displaying personal and account information
     * retrieved from the {@link Controller}.
     * @return a {@link ScrollPane} containing the formatted account details
     */
    protected ScrollPane accountDetailsMenu(){
        VBox pane = new VBox();
        ScrollPane scrollPane = new ScrollPane(pane);
        pane.setMinWidth(350);
        pane.setPadding(new Insets(10));
        pane.setId("pane");
        pane.setFillWidth(true);
        scrollPane.setFitToWidth(true);

        ArrayList<String> headings = new ArrayList<>();

        //Account Number
        headings.add("Account Number");                         //0

        //Personal Information
        headings.add("Customer ID");                            //1
        headings.add("Name");
        headings.add("Date of Birth (DOB)");
        headings.add("Phone Number");
        headings.add("Email");
        headings.add("Address");
        headings.add("Postcode");
        headings.add("Monthly Income");                         //8

        //Account Information
        headings.add("Sortcode");                               //9
        headings.add("Account Type (Basic/Student)");
        headings.add("Account Tier (Normal/Pro/Prime)");
        headings.add("Account Balance");
        headings.add("Recent Activity Date");
        headings.add("Recent Activity Information");
        headings.add("Withdrawal Limit");                       //15

        //make a message

        VBox personalInformation = accountDetailCategory("Personal Details", headings, accountDetails, 1, 8);
        VBox accountInformation = accountDetailCategory("Account Details", headings, accountDetails, 9, 15);

        Separator separator = new Separator();
        separator.setPrefHeight(3);
        separator.setId("separater");

        pane.getChildren().addAll(personalInformation, separator, accountInformation);

        pane.setAlignment(Pos.CENTER);
        return scrollPane;
    }

    /**
     * Constructs and returns a collapsible category section for the account details view,
     * displaying a heading row and an expandable grid of label-value pairs.
     * @param heading  the category heading text
     * @param headings the list of all field label strings
     * @param values   the list of all corresponding value strings
     * @param start    the inclusive start index into headings/values
     * @param stop     the inclusive stop index into headings/values
     * @return a {@link VBox} containing the collapsible category section
     */
    protected VBox accountDetailCategory(String heading, ArrayList<String> headings, ArrayList<String> values, int start, int stop){
        VBox pane = new VBox();
        pane.setPadding(new Insets(10));

        HBox topView = new HBox();
        topView.setAlignment(Pos.CENTER_LEFT);
        topView.setPrefWidth(Double.MAX_VALUE);
        topView.setPadding(new Insets(5));

        Label la_title = new Label(heading);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ImageView image_expand = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/expandBlack.png")),20,10,false,false));
        Button button_expand = new Button();
        button_expand.setId("expand");
        button_expand.setId("button_expand");
        button_expand.setGraphic(image_expand);
        GridPane.setHalignment(button_expand, HPos.RIGHT);

        topView.getChildren().addAll(la_title, spacer, button_expand);

        GridPane bottomView = new GridPane();
        bottomView.setVisible(false); bottomView.setManaged(false);
        bottomView.setPadding(new Insets(5));

        ColumnConstraints col1 = new ColumnConstraints(); col1.setMinWidth(450);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setMinWidth(500);

        bottomView.getColumnConstraints().addAll(col1, col2);

        int row = 0;

        for(int i=start; i<=stop; i++){
            Label label_heading = new Label(headings.get(i));
            Label label_value = new Label(values.get(i));

            bottomView.add(label_heading, 0, row); bottomView.add(label_value, 1, row++);
        }

        pane.getChildren().addAll(topView, bottomView);

        button_expand.setOnAction(e -> {
            boolean show = !bottomView.isVisible();
            bottomView.setVisible(show); bottomView.setManaged(show);
            if(show){ button_expand.setRotate(180); }
            else{ button_expand.setRotate(0); }
        });

        return pane;
    }

    /**
     * Constructs and returns the Change Plan screen, allowing the user to select a new account type and tier
     * via radio buttons, with the corresponding withdrawal limit displayed and a save button to confirm.
     * @return a {@link Pane} containing the plan selection controls
     */
    protected Pane changePlanMenu(){
        GridPane pane = new GridPane();
        pane.setMinWidth(350);
        pane.setPadding(new Insets(10));

        pane.setHgap(20);
        pane.setVgap(20);

        String type = accountDetails.get(10), tier = accountDetails.get(11);

        Label laWithdrawalLimit = new Label("Withdrawal Limit: ");
        Text txtWithdrawalLimit = new Text(type); txtWithdrawalLimit.setId("label_heading");

        Label laType = new Label("Account Type: ");
        Text txtType = new Text(type); laWithdrawalLimit.setId("label_heading");
        Label laTier = new Label("Account Tier: ");
        Text txtTier = new Text(tier); laWithdrawalLimit.setId("label_heading");

        Label[] labels = {laWithdrawalLimit, laType, laTier};
        for(Label l : labels){l.setId("label_heading");}

        Text[] texts = {txtWithdrawalLimit, txtType, txtTier};
        for(Text t : texts){t.setId("label_heading");}

        switch(type+tier){
            case "BasicNormal": txtWithdrawalLimit.setText("£300"); break;
            case "BasicPro": txtWithdrawalLimit.setText("£500"); break;
            case "BasicPrime": txtWithdrawalLimit.setText("£700"); break;
            case "StudentNormal": txtWithdrawalLimit.setText("£250"); break;
            case "StudentPro": txtWithdrawalLimit.setText("£400"); break;
            case "StudentPrime": txtWithdrawalLimit.setText("£600"); break;
        }

        ToggleGroup group1 = new ToggleGroup();
        RadioButton btn1_1 = new RadioButton("Basic"); btn1_1.setToggleGroup(group1);
        RadioButton btn1_2 = new RadioButton("Student"); btn1_2.setToggleGroup(group1);

        RadioButton[] btnGroup1 = {btn1_1, btn1_2};
        for(RadioButton btn : btnGroup1){ btn.setSelected(type.equals(btn.getText())); btn.setId("label_heading");}

        btn1_1.setOnAction(e -> {
            txtType.setText("Basic");
            switch(txtTier.getText()){
                case "Normal": txtWithdrawalLimit.setText("£300"); break;
                case "Pro": txtWithdrawalLimit.setText("£500"); break;
                case "Prime": txtWithdrawalLimit.setText("£700"); break;
            }
        });
        btn1_2.setOnAction(e -> {
            txtType.setText("Student");
            switch(txtTier.getText()){
                case "Normal": txtWithdrawalLimit.setText("£250"); break;
                case "Pro": txtWithdrawalLimit.setText("£400"); break;
                case "Prime": txtWithdrawalLimit.setText("£600"); break;
            }
        });

        ToggleGroup group2 = new ToggleGroup();
        RadioButton btn2_1 = new RadioButton("Normal"); btn2_1.setToggleGroup(group2);
        RadioButton btn2_2 = new RadioButton("Pro"); btn2_2.setToggleGroup(group2);
        RadioButton btn2_3 = new RadioButton("Prime"); btn2_3.setToggleGroup(group2);
        RadioButton[] btnGroup2 = {btn2_1, btn2_2, btn2_3};
        for(RadioButton btn : btnGroup2){ btn.setSelected(tier.equals(btn.getText())); btn.setId("label_heading");}

        btn2_1.setOnAction(e -> {
            txtTier.setText("Normal");
            switch(txtType.getText()){
                case "Basic": txtWithdrawalLimit.setText("£300"); break;
                case "Student": txtWithdrawalLimit.setText("£250"); break;
            }
        });
        btn2_2.setOnAction(e -> {
            txtTier.setText("Pro");
            switch(txtType.getText()){
                case "Basic": txtWithdrawalLimit.setText("£500"); break;
                case "Student": txtWithdrawalLimit.setText("£400"); break;
            }
        });
        btn2_3.setOnAction(e -> {
            txtTier.setText("Prime");
            switch(txtType.getText()){
                case "Basic": txtWithdrawalLimit.setText("£700"); break;
                case "Student": txtWithdrawalLimit.setText("£600"); break;
            }
        });

        Button btnSave = new Button("SAVE CHANGES");
        btnSave.setId("saveChangePlan");
        btnSave.setMinSize(60, 45);
        btnSave.setId("label_heading");
        btnSave.setOnAction(e -> {
            System.out.println( "View::buttonClicked: label = "+ btnSave.getText() );
            controller.saveChangePlan(txtType.getText(), txtTier.getText(), txtWithdrawalLimit.getText().replace("£", ""));
        });

        int row = 0;

        pane.add(laWithdrawalLimit, 0, row, 2, 1); pane.add(txtWithdrawalLimit, 2, row++, 2, 1);
        pane.add(laTier, 0, row); pane.add(txtType, 1, row); pane.add(laType, 2, row); pane.add(txtTier, 3, row++);
                                    pane.add(btn1_1, 1, row);                               pane.add(btn2_1, 3, row++);
                                    pane.add(btn1_2, 1, row);                               pane.add(btn2_2, 3, row++);
                                                                                               pane.add(btn2_3, 3, row++);
                                                                                               pane.add(btnSave, 3, row);

        pane.setAlignment(Pos.CENTER);
        return pane;
    }

    /*----------------------------------CUSTOM CENTER SCREENS BOXS--------------------------------*/
    /** Clears the content box, showing a blank area. */
    protected void showBlankBox(){
        box.getChildren().clear();
    }

    /** Displays the user main menu inside the content box. */
    protected void showUserMenu(){
        box.getChildren().clear();
        box.getChildren().add(userMenu());
    }

    /** Displays the deposit predefined amounts menu inside the content box. */
    protected void showDepositMenu(){
        box.getChildren().clear();
        box.getChildren().add(depositMenu());
    }

    /** Displays the transaction history menu inside the content box. */
    protected void showTransactionMenu(){
        box.getChildren().clear();
        box.getChildren().add(transactionHistoryMenu());
    }

    /** Displays the settings menu inside the content box. */
    protected void showSettingsMenu(){
        box.getChildren().clear();
        box.getChildren().add(settingsMenu());
    }

    /** Displays the account details view inside the content box. */
    protected void showAccountDetails(){
        box.getChildren().clear();
        box.getChildren().add(accountDetailsMenu());
    }

    /** Displays the change plan menu inside the content box. */
    protected void showChangePlanMenu(){
        box.getChildren().clear();
        box.getChildren().add(changePlanMenu());
    }

    /*-------------------------------------------RIGHT--------------------------------------------*/
    /**
     * Builds the right numpad panel containing digit buttons, decimal point,
     * CLEAR, ENTER, BACKSPACE, and EXIT, and adds it to the right side.
     */
    protected void right(){
        right = new GridPane();
        right.setMinHeight(665);
        right.setId("right");

        showNumPad();

        root.setRight(right);
    }

    /**
     * Constructs and returns the numpad GridPane containing all input buttons.
     * @return a {@link GridPane} representing the full numpad
     */
    protected GridPane numPad(){
        GridPane pane = new GridPane();
        pane.setPadding(new Insets(10));
        pane.setVgap(8);
        pane.setHgap(8);

        String[][] buttonTexts = {
                {"1", "2", "3"},
                {"4", "5", "6"},
                {"7", "8", "9"},
                {"",  "0", "."}
        };

        String[] controlButtonTexts = {"  CLEAR  ", "  ENTER  ", "BACKSPACE", "  EXIT   "};

        int j = 0;

        for(int i = 0; i<buttonTexts.length; i++){
            for(j = 0; j<buttonTexts[i].length; j++){
                if(!buttonTexts[i][j].isEmpty()){
                    Button btn = new Button(buttonTexts[i][j]);
                    btn.setId("button_numpad");
                    btn.setOnAction( this::buttonClicked );
                    pane.add(btn, j, i);
                } else { pane.getChildren().add(new Text()); }
            }
        }

        j++;

        for(String k : controlButtonTexts){
            Button btn = new Button(k);
            btn.setId("button_numpad");
            btn.setOnAction( this::numPadButtonClicked ); // Changed to numPadButtonClicked
            pane.add(btn,0, j, 3, 1);
            j++;
        }

        pane.setAlignment(Pos.CENTER);
        return pane;
    }

    /**
     * Handler for numpad buttons - plays audio feedback
     */
    private void numPadButtonClicked(ActionEvent event) {
        Button b = ((Button) event.getSource());// gets the thing that was clicked, but JavaFX returns it as a generic Object, so (Button) converts it into a proper Button so you can use button-specific methods on it
        String text = b.getText().trim();//reads the label on the button, e.g. "1", "ENTER", "CLEAR"
        //.trim() — removes any accidental spaces from either end of the text, because some button labels in the code have padding like "  CLEAR  "
        System.out.println("View::numPadButtonClicked: label = " + text);

        // Play audio feedback for numpad buttons (0-9, CLEAR, ENTER, BACKSPACE)
        if (text.equals("CLEAR") || text.equals("ENTER") || text.equals("BACKSPACE")||text.equals("EXIT")) {
            audioFeedback(text);
        }

        controller.process(text);//After the audio, the button press is forwarded to the controller which handles the actual ATM logic
    }

    /**
     * Event handler for buttons that use their text label as the action identifier.
     * Passes the trimmed button text to {@link Controller#process(String)}.
     * @param event the ActionEvent triggered by the button click
     */
    private void buttonClicked(ActionEvent event) {
        Button b = ((Button) event.getSource());
        String text = b.getText().trim();
        System.out.println( "View::buttonClicked: label = "+ text );

        if (text.matches("[0-9]") || text.equals(".")) {
            if(text.equals(".")){text="period";}
            audioFeedback(text);
        }
        
        controller.process( text );
    }

    /**
     * Event handler for buttons that use their ID as the action identifier.
     * Passes the trimmed button ID to {@link Controller#process(String)}.
     * @param event the ActionEvent triggered by the button click
     */
    private void idButtonClicked(ActionEvent event){
        Button b = ((Button) event.getSource());
        String text = b.getId().trim();
        System.out.println( "View::buttonClicked: label = "+ text );
        controller.process( text );
    }

    /*----------------------------------CUSTOM RIGHT SCREENS------------------------------------*/
    /** Clears and rebuilds the numpad in the right region. */
    protected void showNumPad(){
        right.getChildren().clear();
        right.getChildren().addAll(numPad());
    }

    /*---------------------------------------BOTTOM-----------------------------------------------*/
    /** Builds the bottom status bar used as a visual effect. */
    protected void bottom(){
        bottom = new HBox();
        bottom.setId("bottom");
        bottom.setMinHeight(15);

        root.setBottom(bottom);
    }

    
    /**
     * Updates the display based on the current ATM state.
     * Controls visibility of the home and settings buttons, the text field, and the content box.
     * Called by {@link UIModel} whenever state or data changes.
     * @param title      the heading text to display
     * @param message    the message label text to display
     * @param numPadInput the current numpad input string to show in the text field
     * @param result     the current state string used to determine which screen to show
     */
    public void update(String title, String message, String numPadInput, String result) {

        switch(result){
            case "account_no":
                homeBtn.setVisible(false); settingsBtn.setVisible(false);
                showBlankBox();
                break;
            case "password":
                homeBtn.setVisible(false); settingsBtn.setVisible(false);
                showBlankBox();
                break;
            case "logged_in":
                homeBtn.setVisible(true); settingsBtn.setVisible(true);
                tf.setVisible(false); tf.setManaged(false);
                showUserMenu();
                break;
            case "deposit":
                homeBtn.setVisible(true); settingsBtn.setVisible(false);
                tf.setVisible(true); tf.setManaged(true);
                showDepositMenu();
                break;
            case "withdraw":
                homeBtn.setVisible(true); settingsBtn.setVisible(false);
                tf.setVisible(true); tf.setManaged(true);
                showBlankBox();
                break;
            case "balance":
                homeBtn.setVisible(true); settingsBtn.setVisible(false);
                tf.setVisible(false); tf.setManaged(false);
                showBlankBox();
                break;
            case "transactions":
                homeBtn.setVisible(true); settingsBtn.setVisible(false);
                tf.setVisible(false); tf.setManaged(false);
                showTransactionMenu();
                break;
            case "transfer_account":
                homeBtn.setVisible(true); settingsBtn.setVisible(false);
                tf.setVisible(true); tf.setManaged(true);
                showBlankBox();
                break;
            case "startpage":
                homeBtn.setVisible(false); settingsBtn.setVisible(false);
                tf.setVisible(true); tf.setManaged(true);
                showHomeScreen();
                break;
            case "settings":
                homeBtn.setVisible(true); settingsBtn.setVisible(true);
                tf.setVisible(false); tf.setManaged(false);
                showSettingsMenu();
                break;
            case "settings_account_details":
                homeBtn.setVisible(true); settingsBtn.setVisible(true);
                tf.setVisible(false); tf.setManaged(false);
                showAccountDetails();
                break;
            case "settings_change_password":
                homeBtn.setVisible(true); settingsBtn.setVisible(true);
                tf.setVisible(true); tf.setManaged(true);
                showBlankBox();
                break;
            case "settings_change_new_password":
                homeBtn.setVisible(true); settingsBtn.setVisible(true);
                tf.setVisible(true); tf.setManaged(true);
                showBlankBox();
                break;
            case "settings_change_plan":
                homeBtn.setVisible(true); settingsBtn.setVisible(true);
                tf.setVisible(false); tf.setManaged(false);
                showChangePlanMenu();
                break;
            case "settings_change_withdrawal_limit":
                homeBtn.setVisible(true); settingsBtn.setVisible(true);
                tf.setVisible(true); tf.setManaged(true);
                showBlankBox();
                break;
            default:
        }

        laHeading.setText(title);
        la.setText(message);
        tf.setText(numPadInput);
    }
}