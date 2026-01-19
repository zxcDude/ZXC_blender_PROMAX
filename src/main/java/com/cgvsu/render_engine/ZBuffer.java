package com.cgvsu.render_engine;

public class ZBuffer {
    private final float[][] buffer;
    private final int width;
    private final int height;

    public ZBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.buffer = new float[width][height];
        clear();
    }

    public void clear() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                buffer[x][y] = Float.MAX_VALUE;
            }
        }
    }

    public boolean shouldDraw(int x, int y, float z) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }

        if (z < buffer[x][y]) {
            buffer[x][y] = z;
            return true;
        }
        return false;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}