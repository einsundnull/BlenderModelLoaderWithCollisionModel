package com.notorein.threedmodeling;

import com.notorein.threedmodeling.utils.Vector3D;

public class Plane {
    private Vector3D normal;
    private float distance;

    public Plane(Vector3D normal, float distance) {
        this.normal = normal;
        this.distance = distance;
    }

    public double getDistanceToPoint(Vector3D point) {
        return normal.dot(point) + distance;
    }
}
