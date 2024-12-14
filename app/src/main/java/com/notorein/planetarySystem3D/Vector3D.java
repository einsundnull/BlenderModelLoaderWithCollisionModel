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

    public double dot(Vector3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public double distanceTo(Vector3D other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2) + Math.pow(this.z - other.z, 2));
    }

    public static Vector3D zero() {
        return new Vector3D(0, 0, 0);
    }

    public Vector3D negate() {
        return new Vector3D(-this.x, -this.y, -this.z);
    }


    public Vector3D cross(Vector3D other) {
        return new Vector3D(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    public double magnitudeSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    @Override
    public String toString() {
        return "Vector3D{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}