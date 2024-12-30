package com.notorein.threedmodeling.utils;

public class Vector3D {
    public double x;
    public double y;
    public double z;

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

    public double dot(Vector3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3D cross(Vector3D other) {
        return new Vector3D(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    public double magnitude() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public Vector3D normalize() {
        double mag = magnitude();
        return new Vector3D(this.x / mag, this.y / mag, this.z / mag);
    }

    public double distanceTo(Vector3D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public Vector3D negate() {
        return new Vector3D(-this.x, -this.y, -this.z);
    }

    @Override
    public String toString() {
        return "Vector3D(" + x + ", " + y + ", " + z + ")";
    }
}
