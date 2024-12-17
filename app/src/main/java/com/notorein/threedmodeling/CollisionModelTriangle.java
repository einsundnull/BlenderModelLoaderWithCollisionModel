package com.notorein.threedmodeling;

public class CollisionModelTriangle {
    private Vector3D v0;
    private Vector3D v1;
    private Vector3D v2;
    private Vector3D n0;
    private Vector3D n1;
    private Vector3D n2;
    private Vector2D t0;
    private Vector2D t1;
    private Vector2D t2;

    public CollisionModelTriangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D n0, Vector3D n1, Vector3D n2, Vector2D t0, Vector2D t1, Vector2D t2) {
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

    public Vector3D[] getVertices() {
        return new Vector3D[]{v0, v1, v2};
    }

    public void setVertices(Vector3D[] vertices) {
        if (vertices.length == 3) {
            this.v0 = vertices[0];
            this.v1 = vertices[1];
            this.v2 = vertices[2];
        }
    }

    public Vector3D[] getNormals() {
        return new Vector3D[]{n0, n1, n2};
    }

    public Vector2D[] getTexCoords() {
        return new Vector2D[]{t0, t1, t2};
    }

    public Vector3D getCentroid() {
        return new Vector3D(
                (v0.x + v1.x + v2.x) / 3.0,
                (v0.y + v1.y + v2.y) / 3.0,
                (v0.z + v1.z + v2.z) / 3.0
        );
    }

    public boolean intersects(CollisionModelTriangle other) {
        return triangleTriangleIntersection(this, other);
    }

    private  boolean triangleTriangleIntersection(CollisionModelTriangle t1, CollisionModelTriangle t2) {
        Vector3D v0 = t1.v0;
        Vector3D v1 = t1.v1;
        Vector3D v2 = t1.v2;
        Vector3D u0 = t2.v0;
        Vector3D u1 = t2.v1;
        Vector3D u2 = t2.v2;

        Vector3D e1 = v1.subtract(v0);
        Vector3D e2 = v2.subtract(v0);
        Vector3D n1 = e1.cross(e2);
        double d = n1.dot(v0);

        Vector3D e3 = u1.subtract(u0);
        Vector3D e4 = u2.subtract(u0);
        Vector3D n2 = e3.cross(e4);
        double d2 = n2.dot(u0);

        Vector3D dir = n1.cross(n2);
        double denom = dir.dot(dir);

        if (denom == 0.0) {
            return false; // Triangles are parallel
        }

        Vector3D diff = v0.subtract(u0);
        double t = dir.dot(diff) / denom;
        Vector3D p1 = v0.add(n1.scale(t));

        double u = dir.dot(e3) / denom;
        double v = dir.dot(e4) / denom;

        if (u >= 0.0 && v >= 0.0 && (u + v) <= 1.0) {
            return true;
        }

        return false;
    }
}
