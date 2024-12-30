package com.notorein.threedmodeling;

import com.notorein.threedmodeling.utils.Vector2D;
import com.notorein.threedmodeling.utils.Vector3D;

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

    public boolean intersects(ObjectModelTriangle other) {
        return satIntersection(this, other);
    }

    private boolean satIntersection(ObjectModelTriangle t1, ObjectModelTriangle t2) {
        Vector3D[] axes = {
                t1.v1.subtract(t1.v0).cross(t1.v2.subtract(t1.v0)).normalize(),
                t2.v1.subtract(t2.v0).cross(t2.v2.subtract(t2.v0)).normalize(),
                t1.v1.subtract(t1.v0).cross(t2.v1.subtract(t2.v0)).normalize(),
                t1.v1.subtract(t1.v0).cross(t2.v2.subtract(t2.v0)).normalize(),
                t1.v2.subtract(t1.v0).cross(t2.v1.subtract(t2.v0)).normalize(),
                t1.v2.subtract(t1.v0).cross(t2.v2.subtract(t2.v0)).normalize()
        };

        for (Vector3D axis : axes) {
            if (!projectAndCheck(t1, t2, axis)) {
                return false;
            }
        }
        return true;
    }

    private boolean projectAndCheck(ObjectModelTriangle t1, ObjectModelTriangle t2, Vector3D axis) {
        double[] t1Projection = project(t1, axis);
        double[] t2Projection = project(t2, axis);

        return !(t1Projection[1] < t2Projection[0] || t2Projection[1] < t1Projection[0]);
    }

    private double[] project(ObjectModelTriangle triangle, Vector3D axis) {
        double min = axis.dot(triangle.v0);
        double max = min;

        double projection = axis.dot(triangle.v1);
        if (projection < min) min = projection;
        if (projection > max) max = projection;

        projection = axis.dot(triangle.v2);
        if (projection < min) min = projection;
        if (projection > max) max = projection;

        return new double[]{min, max};
    }
}
