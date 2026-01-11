package com.com.сgvsu.model;

import com.com.сgvsu.math.Vector3f;
import com.com.сgvsu.math.Point2f;

import java.util.ArrayList;

public class Model {
    public ArrayList<Vector3f> vertices = new ArrayList<>();
    public ArrayList<Point2f> textureVertices = new ArrayList<>();
    public ArrayList<Vector3f> normals = new ArrayList<>();
    public ArrayList<Polygon> polygons = new ArrayList<>();
}