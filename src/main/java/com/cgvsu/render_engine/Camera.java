package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4f;

public class Camera {
    private Vector3f position;
    private Vector3f target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;
    private final float defaultDistance;

    public Camera(Vector3f position, Vector3f target,
                  float fov, float aspectRatio,
                  float nearPlane, float farPlane) {
        this.position = position;
        this.target = target;
        this.fov = Math.max(0.1f, Math.min(3.0f, fov));
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        this.defaultDistance = position.subtract(target).length();
    }

    public void move(Vector3f translation) {
        position = position.add(translation);
        target = target.add(translation);
    }

    public void orbit(float deltaX, float deltaY) {
        Vector3f relativePos = position.subtract(target);
        float radius = relativePos.length();

        float theta = (float) Math.atan2(relativePos.x, relativePos.z);
        float phi = (float) Math.asin(relativePos.y / radius);

        theta += deltaX;
        phi += deltaY;

        // Ограничение углов
        float phiLimit = (float) (Math.PI / 2 - 0.1);
        phi = Math.max(-phiLimit, Math.min(phiLimit, phi));

        float newX = radius * (float) (Math.sin(phi) * Math.sin(theta));
        float newY = radius * (float) Math.cos(phi);
        float newZ = radius * (float) (Math.sin(phi) * Math.cos(theta));

        position = new Vector3f(newX, newY, newZ).add(target);
    }

    public void zoom(float delta) {
        Vector3f direction = target.subtract(position).normalize();
        float newDistance = getDistanceFromTarget() + delta;

        if (newDistance < 0.1f) newDistance = 0.1f;
        if (newDistance > defaultDistance * 5) newDistance = defaultDistance * 5;

        position = target.subtract(direction.multiply(newDistance));
    }

    public void setFov(float fov) {
        this.fov = Math.max(0.1f, Math.min(3.0f, fov));
    }

    public void reset() {
        Vector3f direction = target.subtract(position).normalize();
        position = target.subtract(direction.multiply(defaultDistance));
        fov = (float) Math.toRadians(60.0);
    }

    // Геттеры и сеттеры
    public Vector3f getPosition() { return position; }
    public Vector3f getTarget() { return target; }
    public float getFov() { return fov; }
    public float getDistanceFromTarget() { return position.subtract(target).length(); }
    public void setAspectRatio(float aspectRatio) { this.aspectRatio = aspectRatio; }

    public Matrix4f getViewMatrix() {
        return GraphicConveyor.lookAt(position, target);
    }

    public Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }
}