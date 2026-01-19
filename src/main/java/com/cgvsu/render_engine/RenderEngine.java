package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import javafx.scene.canvas.GraphicsContext;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.PixelReader;

import java.util.List;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {
    private static final Vector3f DEFAULT_LIGHT_DIR = new Vector3f(0.3f, 0.5f, -0.8f).normalize();
    private static final float AMBIENT_LIGHT = 0.2f;

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final WritableImage texture,
            boolean useTexture,
            boolean useLighting) {

        if (mesh == null || width <= 0 || height <= 0) {
            return;
        }

        // Настройка матриц преобразования
        Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix.m);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        // Создание буферов
        WritableImage frameBuffer = new WritableImage(width, height);
        PixelWriter pixelWriter = frameBuffer.getPixelWriter();
        PixelReader textureReader = texture.getPixelReader();
        ZBuffer zBuffer = new ZBuffer(width, height);

        // Очистка канваса
        graphicsContext.clearRect(0, 0, width, height);

        // Рендеринг каждого полигона
        for (Polygon polygon : mesh.getPolygons()) {
            List<Polygon> triangles = polygon.triangulate();

            for (Polygon triangle : triangles) {
                renderTriangle(triangle, mesh, modelViewProjectionMatrix,
                        pixelWriter, textureReader, zBuffer,
                        width, height, texture, useTexture, useLighting);

                // Отрисовка контура (опционально)
                drawWireframe(triangle, mesh, modelViewProjectionMatrix,
                        graphicsContext, width, height);
            }
        }

        // Отображение результата
        graphicsContext.drawImage(frameBuffer, 0, 0);
    }

    private static void renderTriangle(Polygon triangle, Model mesh,
                                       Matrix4f transformMatrix,
                                       PixelWriter pixelWriter,
                                       PixelReader textureReader,
                                       ZBuffer zBuffer,
                                       int width, int height,
                                       WritableImage texture,
                                       boolean useTexture,
                                       boolean useLighting) {

        List<Integer> vertexIndices = triangle.getVertexIndices();
        if (vertexIndices.size() != 3) {
            return;
        }

        // Преобразование вершин
        Vector3f[] vertices = new Vector3f[3];
        Vector2f[] screenPoints = new Vector2f[3];
        float[] depths = new float[3];

        for (int i = 0; i < 3; i++) {
            Vector3f vertex = mesh.getVertices().get(vertexIndices.get(i));
            Vector3f transformed = multiplyMatrix4ByVector3(transformMatrix, vertex);
            screenPoints[i] = vertexToPoint(transformed, width, height);
            depths[i] = transformed.z;
            vertices[i] = vertex;
        }

        // Получение текстурных координат (если есть)
        Vector2f[] uvCoords = null;
        if (useTexture && !triangle.getTextureVertexIndices().isEmpty()) {
            uvCoords = new Vector2f[3];
            List<Integer> uvIndices = triangle.getTextureVertexIndices();
            for (int i = 0; i < 3; i++) {
                uvCoords[i] = mesh.getTextureVertices().get(uvIndices.get(i));
            }
        }

        // Получение нормалей (если есть)
        Vector3f[] normals = null;
        if (useLighting && !triangle.getNormalIndices().isEmpty()) {
            normals = new Vector3f[3];
            List<Integer> normalIndices = triangle.getNormalIndices();
            for (int i = 0; i < 3; i++) {
                normals[i] = mesh.getNormals().get(normalIndices.get(i));
            }
        }

        // Нахождение bounding box
        int minX = (int) Math.max(0, Math.floor(Math.min(screenPoints[0].x,
                Math.min(screenPoints[1].x, screenPoints[2].x))));
        int maxX = (int) Math.min(width - 1, Math.ceil(Math.max(screenPoints[0].x,
                Math.max(screenPoints[1].x, screenPoints[2].x))));
        int minY = (int) Math.max(0, Math.floor(Math.min(screenPoints[0].y,
                Math.min(screenPoints[1].y, screenPoints[2].y))));
        int maxY = (int) Math.min(height - 1, Math.ceil(Math.max(screenPoints[0].y,
                Math.max(screenPoints[1].y, screenPoints[2].y))));

        if (minX > maxX || minY > maxY) {
            return;
        }

        float area = edgeFunction(screenPoints[0], screenPoints[1], screenPoints[2]);
        if (Math.abs(area) < 1e-6f) {
            return;
        }

        // Растеризация треугольника
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Vector2f p = new Vector2f(x + 0.5f, y + 0.5f);

                float w0 = edgeFunction(screenPoints[1], screenPoints[2], p) / area;
                float w1 = edgeFunction(screenPoints[2], screenPoints[0], p) / area;
                float w2 = edgeFunction(screenPoints[0], screenPoints[1], p) / area;

                if (w0 >= 0 && w1 >= 0 && w2 >= 0) {
                    // Интерполяция глубины
                    float z = w0 * depths[0] + w1 * depths[1] + w2 * depths[2];

                    if (zBuffer.shouldDraw(x, y, z)) {
                        Color color = calculateColor(w0, w1, w2, uvCoords, normals,
                                textureReader, texture,
                                useTexture, useLighting);
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
        }
    }

    private static Color calculateColor(float w0, float w1, float w2,
                                        Vector2f[] uvCoords, Vector3f[] normals,
                                        PixelReader textureReader, WritableImage texture,
                                        boolean useTexture, boolean useLighting) {

        Color baseColor = Color.LIGHTGRAY;

        // Текстурирование
        if (useTexture && uvCoords != null && texture != null) {
            float u = w0 * uvCoords[0].x + w1 * uvCoords[1].x + w2 * uvCoords[2].x;
            float v = w0 * uvCoords[0].y + w1 * uvCoords[1].y + w2 * uvCoords[2].y;

            int texX = (int) (u * (texture.getWidth() - 1));
            int texY = (int) ((1 - v) * (texture.getHeight() - 1));

            texX = Math.max(0, Math.min(texX, (int)texture.getWidth() - 1));
            texY = Math.max(0, Math.min(texY, (int)texture.getHeight() - 1));

            baseColor = textureReader.getColor(texX, texY);
        }

        // Освещение
        if (useLighting && normals != null) {
            Vector3f normal = new Vector3f(
                    w0 * normals[0].x + w1 * normals[1].x + w2 * normals[2].x,
                    w0 * normals[0].y + w1 * normals[1].y + w2 * normals[2].y,
                    w0 * normals[0].z + w1 * normals[1].z + w2 * normals[2].z
            ).normalize();

            float diffuse = Math.max(AMBIENT_LIGHT, normal.dot(DEFAULT_LIGHT_DIR));

            return new Color(
                    baseColor.getRed() * diffuse,
                    baseColor.getGreen() * diffuse,
                    baseColor.getBlue() * diffuse,
                    baseColor.getOpacity()
            );
        }

        return baseColor;
    }

    private static void drawWireframe(Polygon triangle, Model mesh,
                                      Matrix4f transformMatrix,
                                      GraphicsContext gc,
                                      int width, int height) {

        List<Integer> vertexIndices = triangle.getVertexIndices();

        Vector2f[] points = new Vector2f[vertexIndices.size()];
        for (int i = 0; i < vertexIndices.size(); i++) {
            Vector3f vertex = mesh.getVertices().get(vertexIndices.get(i));
            Vector3f transformed = multiplyMatrix4ByVector3(transformMatrix, vertex);
            points[i] = vertexToPoint(transformed, width, height);
        }

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        for (int i = 0; i < points.length; i++) {
            int next = (i + 1) % points.length;
            gc.strokeLine(points[i].x, points[i].y, points[next].x, points[next].y);
        }
    }

    private static float edgeFunction(Vector2f a, Vector2f b, Vector2f c) {
        return (c.x - a.x) * (b.y - a.y) - (c.y - a.y) * (b.x - a.x);
    }
}