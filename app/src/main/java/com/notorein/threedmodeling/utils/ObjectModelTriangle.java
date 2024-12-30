package com.notorein.threedmodeling.utils;

public class ObjectModelTriangle {
    private Vector3D v0;
    private Vector3D v1;
    private Vector3D v2;
    private Vector3D n0;
    private Vector3D n1;
    private Vector3D n2;
    private Vector2D t0;
    private Vector2D t1;
    private Vector2D t2;

    public ObjectModelTriangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D n0, Vector3D n1, Vector3D n2, Vector2D t0, Vector2D t1, Vector2D t2) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.n0 = n0;
        this.n1 = n1;
        this.n2 = n2;
        this.t0 = t0;
        this.t1 = t1;
        this.t2 = t2;
    }

    public native Vector3D[] getVertices();
    public native Vector3D[] getNormals();
    public native Vector2D[] getTexCoords();
    public native Vector3D getCentroid();
    public native boolean intersects(ObjectModelTriangle other);

    static {
        System.loadLibrary("objectmodeltriangle");
    }
}
