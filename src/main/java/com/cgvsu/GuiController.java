package com.cgvsu;

import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objreader.ObjReaderException;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.RenderEngine;

public class GuiController {
    final private float TRANSLATION = 0.1F;
    final private float ROTATION_SPEED = 0.01F;
    final private float ZOOM_SPEED = 0.1F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;
    @FXML
    private CheckMenuItem wireframeCheck;
    @FXML
    private CheckMenuItem textureCheck;
    @FXML
    private CheckMenuItem lightingCheck;
    @FXML
    private MenuItem toggleThemeItem;

    private Model mesh = null;
    private WritableImage texture = new WritableImage(1, 1);
    private boolean useTexture = false;
    private boolean useLighting = true;
    private boolean drawWireframe = true;
    private boolean isDarkTheme = false;

    private Camera camera = new Camera(
            new Vector3f(0, 0, 5),
            new Vector3f(0, 0, 0),
            (float) Math.toRadians(60.0),
            1.0f,
            0.1F,
            100.0f
    );

    private Timeline timeline = new Timeline();
    private double lastMouseX, lastMouseY;
    private boolean isMousePressed = false;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        canvas.setFocusTraversable(true);

        // Обработка клавиатуры
        canvas.setOnKeyPressed(this::handleKeyPress);

        // Обработка мыши для вращения
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);

        // Обработка колесика для zoom
        canvas.setOnScroll(this::handleMouseScroll);

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            if (mesh != null) {
                RenderEngine.render(
                        canvas.getGraphicsContext2D(),
                        camera,
                        mesh,
                        (int) width,
                        (int) height,
                        texture,
                        useTexture,
                        useLighting
                );
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        wireframeCheck.selectedProperty().addListener((obs, old, newVal) -> drawWireframe = newVal);
        textureCheck.selectedProperty().addListener((obs, old, newVal) -> useTexture = newVal);
        lightingCheck.selectedProperty().addListener((obs, old, newVal) -> useLighting = newVal);
    }

    // ========== ПЕРЕКЛЮЧЕНИЕ ТЕМЫ ==========
    @FXML
    private void toggleTheme() {
        Scene scene = canvas.getScene();
        if (scene == null) return;

        scene.getStylesheets().clear();

        if (isDarkTheme) {
            scene.getStylesheets().add(getClass().getResource("light-theme.css").toExternalForm());
        } else {
            scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
        }

        isDarkTheme = !isDarkTheme;
    }

    // ========== ЗАГРУЗКА МОДЕЛИ ==========
    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            mesh = ObjReader.read(fileContent);
            System.out.println("Model loaded successfully: " +
                    mesh.getVertices().size() + " vertices, " +
                    mesh.getPolygons().size() + " polygons");
        } catch (IOException exception) {
            System.err.println("Error reading file: " + exception.getMessage());
            showErrorDialog("File Error", "Cannot read file: " + exception.getMessage());
        } catch (ObjReaderException exception) {
            System.err.println("Error parsing OBJ: " + exception.getMessage());
            showErrorDialog("Parse Error", "Cannot parse OBJ file: " + exception.getMessage());
        } catch (Exception exception) {
            System.err.println("Unexpected error: " + exception.getMessage());
            exception.printStackTrace();
            showErrorDialog("Error", "Unexpected error: " + exception.getMessage());
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ========== ОБРАБОТКА МЫШИ ==========

    private void handleMousePressed(MouseEvent event) {
        lastMouseX = event.getX();
        lastMouseY = event.getY();
        isMousePressed = true;
        canvas.requestFocus();
    }

    private void handleMouseDragged(MouseEvent event) {
        if (!isMousePressed) return;

        double deltaX = event.getX() - lastMouseX;
        double deltaY = event.getY() - lastMouseY;

        // Вращаем камеру вокруг target
        camera.orbit(
                (float) (deltaX * ROTATION_SPEED),
                (float) (deltaY * ROTATION_SPEED)
        );

        lastMouseX = event.getX();
        lastMouseY = event.getY();
    }

    private void handleMouseReleased(MouseEvent event) {
        isMousePressed = false;
    }

    private void handleMouseScroll(ScrollEvent event) {
        // Zoom через изменение позиции камеры
        float delta = (float) (event.getDeltaY() > 0 ? -ZOOM_SPEED : ZOOM_SPEED);
        camera.zoom(delta);
        canvas.requestFocus();
    }

    // ========== ОБРАБОТКА КЛАВИАТУРЫ ==========

    private void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case W:
                // Движение вперед
                camera.move(new Vector3f(0, 0, -TRANSLATION));
                break;
            case S:
                // Движение назад
                camera.move(new Vector3f(0, 0, TRANSLATION));
                break;
            case A:
                // Движение влево
                camera.move(new Vector3f(-TRANSLATION, 0, 0));
                break;
            case D:
                // Движение вправо
                camera.move(new Vector3f(TRANSLATION, 0, 0));
                break;
            case UP:
            case E:
                // Движение вверх
                camera.move(new Vector3f(0, TRANSLATION, 0));
                break;
            case DOWN:
            case Q:
                // Движение вниз
                camera.move(new Vector3f(0, -TRANSLATION, 0));
                break;
            case LEFT:
                // Вращение камеры влево
                camera.orbit(-ROTATION_SPEED * 10, 0);
                break;
            case RIGHT:
                // Вращение камеры вправо
                camera.orbit(ROTATION_SPEED * 10, 0);
                break;
            case ADD:
            case EQUALS:
                // Увеличить FOV (шире угол обзора)
                camera.setFov(camera.getFov() - 0.1f);
                break;
            case SUBTRACT:
            case MINUS:
                // Уменьшить FOV (увеличить zoom)
                camera.setFov(camera.getFov() + 0.1f);
                break;
            case R:
                // Сброс камеры
                camera = new Camera(
                        new Vector3f(3, 2, 5),
                        new Vector3f(0, 0, 0),
                        (float) Math.toRadians(60.0),
                        (float) (canvas.getWidth() / canvas.getHeight()),
                        0.1F, 100);
                break;
        }
    }

    // ========== УПРАВЛЕНИЕ КАМЕРОЙ ЧЕРЕЗ МЕНЮ ==========

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.move(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.move(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.move(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.move(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.move(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.move(new Vector3f(0, -TRANSLATION, 0));
    }

    @FXML
    public void handleCameraZoomIn(ActionEvent actionEvent) {
        // Увеличить FOV
        camera.setFov(camera.getFov() - 0.1f);
    }

    @FXML
    public void handleCameraZoomOut(ActionEvent actionEvent) {
        // Уменьшить FOV
        camera.setFov(camera.getFov() + 0.1f);
    }
    

    @FXML
    public void onExitMenuItemClick() {
        timeline.stop();
        Stage stage = (Stage) canvas.getScene().getWindow();
        stage.close();
    }


    @FXML
    public void onAboutMenuItemClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Simple3DViewer");
        alert.setHeaderText("3D Model Viewer");
        alert.setContentText("A simple 3D model viewer with OBJ file support.\n" +
                "Features:\n" +
                "- Load and display OBJ models\n" +
                "- Camera controls (orbit, pan, zoom)\n" +
                "- Lighting and texture support\n" +
                "- Wireframe rendering\n" +
                "- Light/dark theme");
        alert.showAndWait();
    }
}