module com.com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.com.cgvsu to javafx.fxml;
    exports com.com.cgvsu;
}