package com.com.cgvsu.model;

import java.util.ArrayList;
import java.util.List;

public class Polygon {
    private ArrayList<Integer> vertexIndices;
    private ArrayList<Integer> textureVertexIndices;
    private ArrayList<Integer> normalIndices;

    public Polygon() {
        vertexIndices = new ArrayList<>();
        textureVertexIndices = new ArrayList<>();
        normalIndices = new ArrayList<>();
    }

    public void setVertexIndices(ArrayList<Integer> vertexIndices) {
        assert vertexIndices.size() >= 3;
        this.vertexIndices = vertexIndices;
    }

    public void setTextureVertexIndices(ArrayList<Integer> textureVertexIndices) {
        assert textureVertexIndices.size() >= 3;
        this.textureVertexIndices = textureVertexIndices;
    }

    public void setNormalIndices(ArrayList<Integer> normalIndices) {
        assert normalIndices.size() >= 3;
        this.normalIndices = normalIndices;
    }

    public ArrayList<Integer> getVertexIndices() {
        return vertexIndices;
    }

    public ArrayList<Integer> getTextureVertexIndices() {
        return textureVertexIndices;
    }

    public ArrayList<Integer> getNormalIndices() {
        return normalIndices;
    }

    public List<Polygon> triangulate() {
        List<Polygon> triangles = new ArrayList<>();
        int n = vertexIndices.size();
        if (n < 3) return triangles;

        for (int i = 1; i < n - 1; i++) {
            Polygon triangle = new Polygon();
            triangle.setVertexIndices(new ArrayList<>(List.of(vertexIndices.get(0), vertexIndices.get(i), vertexIndices.get(i + 1))));
            triangle.setTextureVertexIndices(new ArrayList<>(List.of(textureVertexIndices.get(0), textureVertexIndices.get(i), textureVertexIndices.get(i + 1))));
            triangle.setNormalIndices(new ArrayList<>(List.of(normalIndices.get(0), normalIndices.get(i), normalIndices.get(i + 1))));
            triangles.add(triangle);
        }
        return triangles;
    }
}