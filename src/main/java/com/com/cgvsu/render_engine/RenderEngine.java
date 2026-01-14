package com.com.cgvsu.render_engine;

import com.com.cgvsu.math.Matrix4f;
import com.com.cgvsu.math.Vector2f;
import com.com.cgvsu.math.Vector3f;
import javafx.scene.canvas.GraphicsContext;
import com.com.cgvsu.model.Model;
import com.com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

import static com.com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height)
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

                // Заполнение треугольника
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        if (isPointInTriangle(x, y, resultPoints)) {
                            float z = interpolateZ(x, y, resultPoints, zValues);
                            if (zBuffer.shouldDraw(x, y, z)) {
                                graphicsContext.fillRect(x, y, 1, 1);
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
}
