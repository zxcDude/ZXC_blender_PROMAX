package com.cgvsu.math;

public class Matrix4f {
    public float[] m = new float[16];

    public Matrix4f() {
        // Правильная единичная матрица
        m[0] = 1; m[1] = 0; m[2] = 0; m[3] = 0;
        m[4] = 0; m[5] = 1; m[6] = 0; m[7] = 0;
        m[8] = 0; m[9] = 0; m[10] = 1; m[11] = 0;
        m[12] = 0; m[13] = 0; m[14] = 0; m[15] = 1;
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
        float x = m[0] * v.x + m[4] * v.y + m[8] * v.z + m[12];
        float y = m[1] * v.x + m[5] * v.y + m[9] * v.z + m[13];
        float z = m[2] * v.x + m[6] * v.y + m[10] * v.z + m[14];
        float w = m[3] * v.x + m[7] * v.y + m[11] * v.z + m[15];

        if (w == 0) w = 1;
        return new Vector3f(x / w, y / w, z / w);
    }

    // Новый метод для создания матрицы поворота
    public static Matrix4f rotateY(float angle) {
        Matrix4f m = new Matrix4f();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        m.m[0] = cos;  m.m[2] = sin;
        m.m[8] = -sin; m.m[10] = cos;

        return m;
    }
}