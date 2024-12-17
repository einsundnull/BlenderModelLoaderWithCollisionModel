package com.notorein.threedmodeling;

public class Vector2D {
    public double u;
    public double v;

    public Vector2D(double u, double v) {
        this.u = u;
        this.v = v;
    }

    @Override
    public String toString() {
        return "Vector2D(" + u + ", " + v + ")";
    }
}
