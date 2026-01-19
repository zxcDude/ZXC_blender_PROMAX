package com.cgvsu.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Polygon {
    private List<Integer> vertexIndices;
    private List<Integer> textureVertexIndices;
    private List<Integer> normalIndices;

    public Polygon() {
        vertexIndices = new ArrayList<>();
        textureVertexIndices = new ArrayList<>();
        normalIndices = new ArrayList<>();
    }

    public void setVertexIndices(List<Integer> vertexIndices) {
        if (vertexIndices.size() < 3) {
            throw new IllegalArgumentException("Polygon must have at least 3 vertices");
        }
        this.vertexIndices = new ArrayList<>(vertexIndices);
    }

    public void setTextureVertexIndices(List<Integer> textureVertexIndices) {
        // Фильтруем -1 (отсутствующие индексы)
        List<Integer> filtered = new ArrayList<>();
        for (Integer idx : textureVertexIndices) {
            if (idx != null && idx >= 0) {
                filtered.add(idx);
            }
        }
        this.textureVertexIndices = filtered;
    }

    public void setNormalIndices(List<Integer> normalIndices) {
        // Фильтруем -1 (отсутствующие индексы)
        List<Integer> filtered = new ArrayList<>();
        for (Integer idx : normalIndices) {
            if (idx != null && idx >= 0) {
                filtered.add(idx);
            }
        }
        this.normalIndices = filtered;
    }

    public List<Integer> getVertexIndices() {
        return Collections.unmodifiableList(vertexIndices);
    }

    public List<Integer> getTextureVertexIndices() {
        return Collections.unmodifiableList(textureVertexIndices);
    }

    public List<Integer> getNormalIndices() {
        return Collections.unmodifiableList(normalIndices);
    }

    public List<Polygon> triangulate() {
        List<Polygon> triangles = new ArrayList<>();
        int vertexCount = vertexIndices.size();

        if (vertexCount < 3) {
            return triangles;
        }

        boolean hasTexture = !textureVertexIndices.isEmpty() &&
                textureVertexIndices.size() == vertexCount;
        boolean hasNormals = !normalIndices.isEmpty() &&
                normalIndices.size() == vertexCount;

        for (int i = 1; i < vertexCount - 1; i++) {
            Polygon triangle = new Polygon();

            // Вершины
            List<Integer> triVertices = List.of(
                    vertexIndices.get(0),
                    vertexIndices.get(i),
                    vertexIndices.get(i + 1)
            );
            triangle.setVertexIndices(triVertices);

            // Текстурные координаты (если есть и согласованы)
            if (hasTexture) {
                List<Integer> triTextures = List.of(
                        textureVertexIndices.get(0),
                        textureVertexIndices.get(i),
                        textureVertexIndices.get(i + 1)
                );
                triangle.setTextureVertexIndices(triTextures);
            }

            // Нормали (если есть и согласованы)
            if (hasNormals) {
                List<Integer> triNormals = List.of(
                        normalIndices.get(0),
                        normalIndices.get(i),
                        normalIndices.get(i + 1)
                );
                triangle.setNormalIndices(triNormals);
            }

            triangles.add(triangle);
        }

        return triangles;
    }

    public int getVertexCount() {
        return vertexIndices.size();
    }

    public boolean hasTextureCoordinates() {
        return !textureVertexIndices.isEmpty() &&
                textureVertexIndices.size() == vertexIndices.size();
    }

    public boolean hasNormals() {
        return !normalIndices.isEmpty() &&
                normalIndices.size() == vertexIndices.size();
    }
}