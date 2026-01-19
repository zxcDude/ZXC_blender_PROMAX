package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector2f;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate() {
        float[] matrix = new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1};
        return new Matrix4f(matrix);
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        // Z = (target - eye), нормализуем
        Vector3f resultZ = target.subtract(eye).normalize();

        // X = up × Z, нормализуем
        Vector3f resultX = up.cross(resultZ).normalize();

        // Y = Z × X, нормализуем
        Vector3f resultY = resultZ.cross(resultX).normalize();

        // Строим матрицу вида (row-major порядок)
        float[] matrix = new float[]{
                resultX.x, resultY.x, resultZ.x, 0,
                resultX.y, resultY.y, resultZ.y, 0,
                resultX.z, resultY.z, resultZ.z, 0,
                -resultX.dot(eye), -resultY.dot(eye), -resultZ.dot(eye), 1
        };

        return new Matrix4f(matrix);
    }

    public static Matrix4f perspective(
            final float fov, // в радианах
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        Matrix4f result = new Matrix4f();

        float tanHalfFov = (float) Math.tan(fov * 0.5f);
        float range = farPlane - nearPlane;

        // Правильная матрица перспективы для правой системы координат
        // (row-major порядок, совместимый с вашим Matrix4f.mul())
        result.m[0] = 1.0f / (aspectRatio * tanHalfFov);
        result.m[5] = 1.0f / tanHalfFov;
        result.m[10] = -(farPlane + nearPlane) / range;  // минус для Z, уходящего вглубь
        result.m[11] = -1.0f;  // КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: было 1.0f
        result.m[14] = -(2.0f * farPlane * nearPlane) / range;
        result.m[15] = 0.0f;

        // Обнуляем остальные элементы (вдруг конструктор Matrix4f не сделал этого)
        result.m[1] = result.m[2] = result.m[3] = 0;
        result.m[4] = result.m[6] = result.m[7] = 0;
        result.m[8] = result.m[9] = result.m[12] = result.m[13] = 0;

        return result;
    }

    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {
        final float x = (vertex.x * matrix.m[0]) + (vertex.y * matrix.m[4]) + (vertex.z * matrix.m[8]) + matrix.m[12];
        final float y = (vertex.x * matrix.m[1]) + (vertex.y * matrix.m[5]) + (vertex.z * matrix.m[9]) + matrix.m[13];
        final float z = (vertex.x * matrix.m[2]) + (vertex.y * matrix.m[6]) + (vertex.z * matrix.m[10]) + matrix.m[14];
        final float w = (vertex.x * matrix.m[3]) + (vertex.y * matrix.m[7]) + (vertex.z * matrix.m[11]) + matrix.m[15];

        // Деление на w для перспективного деления
        if (w == 0.0f || w == 1.0f) {
            return new Vector3f(x, y, z);
        }
        return new Vector3f(x / w, y / w, z / w);
    }

    public static Vector2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        // Преобразование из нормализованных координат устройства (-1..1) в экранные координаты
        // vertex.x и vertex.y в диапазоне [-1, 1], vertex.z - глубина
        return new Vector2f(
                (vertex.x + 1.0f) * 0.5f * width,  // [-1,1] -> [0, width]
                (1.0f - vertex.y) * 0.5f * height  // [-1,1] -> [0, height], Y инвертирован
        );
    }

    // Дополнительные полезные методы для матричных преобразований:

    public static Matrix4f createTranslationMatrix(float tx, float ty, float tz) {
        float[] matrix = new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                tx, ty, tz, 1};
        return new Matrix4f(matrix);
    }

    public static Matrix4f createScaleMatrix(float sx, float sy, float sz) {
        float[] matrix = new float[]{
                sx, 0, 0, 0,
                0, sy, 0, 0,
                0, 0, sz, 0,
                0, 0, 0, 1};
        return new Matrix4f(matrix);
    }

    public static Matrix4f createRotationXMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float[] matrix = new float[]{
                1, 0, 0, 0,
                0, cos, sin, 0,
                0, -sin, cos, 0,
                0, 0, 0, 1};
        return new Matrix4f(matrix);
    }

    public static Matrix4f createRotationYMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float[] matrix = new float[]{
                cos, 0, -sin, 0,
                0, 1, 0, 0,
                sin, 0, cos, 0,
                0, 0, 0, 1};
        return new Matrix4f(matrix);
    }

    public static Matrix4f createRotationZMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float[] matrix = new float[]{
                cos, sin, 0, 0,
                -sin, cos, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1};
        return new Matrix4f(matrix);
    }
}