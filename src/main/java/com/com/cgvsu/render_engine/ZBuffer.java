package com.com.cgvsu.render_engine;

public class ZBuffer {
    private float[][] buffer;

    public ZBuffer(int width, int height) {
        buffer = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                buffer[x][y] = Float.MAX_VALUE;
            }
        }
    }

    public boolean shouldDraw(int x, int y, float z) {
        if (z < buffer[x][y]) {
            buffer[x][y] = z;
            return true;
        }
        return false;
    }
}
