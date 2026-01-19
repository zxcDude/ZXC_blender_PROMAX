module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    exports com.cgvsu.math;
    exports com.cgvsu.model;
    exports com.cgvsu.objreader;
    exports com.cgvsu.render_engine;

    opens com.cgvsu to javafx.graphics, javafx.fxml;
    opens com.cgvsu.math to javafx.fxml;
    opens com.cgvsu.render_engine to javafx.fxml;
}