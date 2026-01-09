package com.com.сgvsu.math;

public class Matrix4f {
    public float[] m = new float[16];

    public Matrix4f() {
        m[0] = 1; m[5] = 1; m[10] = 1; m[15] = 1;
    }

    public Matrix4f(float[] values) {
        System.arraycopy(values, 0, m, 0, 16);
    }

    public void mul(Matrix4f other) {
        float[] result = new float[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i * 4 + j] = 0;
                for (int k = 0; k < 4; k++) {
                    result[i * 4 + j] += m[i * 4 + k] * other.m[k * 4 + j];
                }
            }
        }
        System.arraycopy(result, 0, m, 0, 16);
    }

    public Vector3f multiply(Vector3f v) {
        float w = m[3] * v.x + m[7] * v.y + m[11] * v.z + m[15];
        if (w == 0) w = 1; // avoid division by zero
        return new Vector3f(
                (m[0] * v.x + m[4] * v.y + m[8] * v.z + m[12]) / w,
                (m[1] * v.x + m[5] * v.y + m[9] * v.z + m[13]) / w,
                (m[2] * v.x + m[6] * v.y + m[10] * v.z + m[14]) / w
        );
    }
}