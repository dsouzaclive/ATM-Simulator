module com.atmbanksimulator {
    requires javafx.controls;
    requires java.sql;
    requires java.desktop;


    opens com.atmbanksimulator to javafx.fxml;
    exports com.atmbanksimulator;
}