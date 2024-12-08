package com.notorein.planetarySystem3D;



public class Vector3D {
    public double x, y, z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D add(Vector3D other) {
        return new Vector3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3D subtract(Vector3D other) {
        return new Vector3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3D scale(double scalar) {
        return new Vector3D(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public double magnitude() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public Vector3D normalize() {
        double mag = magnitude();
        return new Vector3D(this.x / mag, this.y / mag, this.z / mag);
    }

    public static Vector3D zero() {
        return new Vector3D(0, 0, 0);
    }
}

