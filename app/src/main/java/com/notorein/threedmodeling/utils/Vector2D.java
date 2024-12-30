package com.notorein.threedmodeling.utils;

public class Vector2D {
    private double u;
    private double v;

    public Vector2D(double u, double v) {
        this.u = u;
        this.v = v;
    }

    public native double magnitude();
    public native double distanceTo(Vector2D other);
    public native Vector2D add(double u, double v);
    public native Vector2D subtract(double u, double v);
    public native Vector2D scale(double scalar);
    public native double dot(Vector2D other);
    public native Vector2D normalize();


}
