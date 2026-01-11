package com.сgvsu.model;

import com.сgvsu.math.Vector3f;
import com.сgvsu.math.Vector2f;

import java.util.ArrayList;

public class Model {
    public ArrayList<Vector3f> vertices = new ArrayList<>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<>();
    public ArrayList<Vector3f> normals = new ArrayList<>();
    public ArrayList<Polygon> polygons = new ArrayList<>();
}