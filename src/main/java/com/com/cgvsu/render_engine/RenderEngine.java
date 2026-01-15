package com.com.cgvsu.render_engine;

import com.com.cgvsu.math.Matrix4f;
import com.com.cgvsu.math.Vector2f;
import com.com.cgvsu.math.Vector3f;
import javafx.scene.canvas.GraphicsContext;
import com.com.cgvsu.model.Model;
import com.com.cgvsu.model.Polygon;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import static com.com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final WritableImage texture,
            boolean useTexture,
            boolean useLighting)
    {
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix.m);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        ZBuffer zBuffer = new ZBuffer(width, height);

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            List<Polygon> triangles = mesh.polygons.get(polygonInd).triangulate();
            for (Polygon triangle : triangles) {
                ArrayList<Vector2f> resultPoints = new ArrayList<>();
                ArrayList<Float> zValues = new ArrayList<>();

                for (int vertexInPolygonInd = 0; vertexInPolygonInd < 3; ++vertexInPolygonInd) {
                    Vector3f vertex = mesh.vertices.get(triangle.getVertexIndices().get(vertexInPolygonInd));

                    Vector3f transformedVertex = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertex);
                    Vector2f resultPoint = vertexToPoint(transformedVertex, width, height);
                    resultPoints.add(resultPoint);
                    zValues.add(transformedVertex.z);
                }

                // Отрисовка треугольника
                for (int i = 0; i < 3; i++) {
                    int next = (i + 1) % 3;
                    graphicsContext.strokeLine(
                            resultPoints.get(i).x,
                            resultPoints.get(i).y,
                            resultPoints.get(next).x,
                            resultPoints.get(next).y);
                }

                // Интерполяция UV (только если есть данные)
                Vector2f uv0 = null, uv1 = null, uv2 = null;
                if (!mesh.textureVertices.isEmpty() && !triangle.getTextureVertexIndices().isEmpty()) {
                    uv0 = mesh.textureVertices.get(triangle.getTextureVertexIndices().get(0));
                    uv1 = mesh.textureVertices.get(triangle.getTextureVertexIndices().get(1));
                    uv2 = mesh.textureVertices.get(triangle.getTextureVertexIndices().get(2));
                }

                // Интерполяция нормалей (только если есть данные)
                Vector3f n0 = new Vector3f(0, 0, 1); // ← нормаль по умолчанию
                Vector3f n1 = new Vector3f(0, 0, 1);
                Vector3f n2 = new Vector3f(0, 0, 1);
                if (!mesh.normals.isEmpty() && !triangle.getNormalIndices().isEmpty()) {
                    n0 = mesh.normals.get(triangle.getNormalIndices().get(0));
                    n1 = mesh.normals.get(triangle.getNormalIndices().get(1));
                    n2 = mesh.normals.get(triangle.getNormalIndices().get(2));
                }

                // Освещение
                Vector3f lightDir = new Vector3f(0, 0, -1); // свет от камеры

                // Заполнение треугольника
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        if (isPointInTriangle(x, y, resultPoints)) {
                            float z = interpolateZ(x, y, resultPoints, zValues);
                            if (zBuffer.shouldDraw(x, y, z)) {
                                if (useLighting) {
                                    Vector2f p = new Vector2f(x + 0.5f, y + 0.5f);
                                    float area = edgeFunction(resultPoints.get(0), resultPoints.get(1), resultPoints.get(2));
                                    if (Math.abs(area) < 1e-6f) continue;

                                    float w0 = edgeFunction(resultPoints.get(1), resultPoints.get(2), p) / area;
                                    float w1 = edgeFunction(resultPoints.get(2), resultPoints.get(0), p) / area;
                                    float w2 = edgeFunction(resultPoints.get(0), resultPoints.get(1), p) / area;

                                    // Интерполяция нормали
                                    Vector3f normal = new Vector3f(
                                            w0 * n0.x + w1 * n1.x + w2 * n2.x,
                                            w0 * n0.y + w1 * n1.y + w2 * n2.y,
                                            w0 * n0.z + w1 * n1.z + w2 * n2.z
                                    ).normalize();

                                    // Интенсивность освещения
                                    float intensity = Math.max(0.3f, -normal.dot(lightDir));

                                    // Серый цвет с освещением
                                    Color color = new Color(intensity, intensity, intensity, 1.0);
                                    graphicsContext.setFill(color);
                                    graphicsContext.fillRect(x, y, 1, 1);
                                } else {
                                    // Простая заливка (если освещение выключено)
                                    graphicsContext.setFill(Color.LIGHTGRAY);
                                    graphicsContext.fillRect(x, y, 1, 1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isPointInTriangle(int x, int y, ArrayList<Vector2f> points) {
        return true;
    }

    private static float interpolateZ(int x, int y, ArrayList<Vector2f> points, ArrayList<Float> zValues) {
        return (zValues.get(0) + zValues.get(1) + zValues.get(2)) / 3;
    }

    private static float edgeFunction(Vector2f a, Vector2f b, Vector2f c) {
        return (c.x - a.x) * (b.y - a.y) - (c.y - a.y) * (b.x - a.x);
    }
}
