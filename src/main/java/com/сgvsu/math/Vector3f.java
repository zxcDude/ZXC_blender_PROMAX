package com.сgvsu.math;

public class Vector3f {
    public float x, y, z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f add(Vector3f other) {
        return new Vector3f(x + other.x, y + other.y, z + other.z);
    }

    public Vector3f subtract(Vector3f other) {
        return new Vector3f(x - other.x, y - other.y, z - other.z);
    }

    public Vector3f multiply(float scalar) {
        return new Vector3f(x * scalar, y * scalar, z * scalar);
    }

    public float dot(Vector3f other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vector3f cross(Vector3f other) {
        return new Vector3f(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }

    public Vector3f normalize() {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (length == 0) return new Vector3f(0, 0, 0);
        return new Vector3f(x / length, y / length, z / length);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public boolean equals(Vector3f other) {
        final float eps = 1e-7f;
        return Math.abs(x - other.x) < eps &&
                Math.abs(y - other.y) < eps &&
                Math.abs(z - other.z) < eps;
    }
}
