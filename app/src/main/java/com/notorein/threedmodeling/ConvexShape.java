package com.notorein.threedmodeling;

import com.notorein.threedmodeling.utils.Vector3D;

import java.util.List;

public class ConvexShape {
    private List<Vector3D> vertices;
    public Vector3D min;
    public Vector3D max;

    public ConvexShape(List<Vector3D> vertices) {
        this.vertices = vertices;
        this.min = new Vector3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        this.max = new Vector3D(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        calculateMinMax();
    }

    private void calculateMinMax() {
        for (Vector3D vertex : vertices) {
            min.x = Math.min(min.x, vertex.x);
            min.y = Math.min(min.y, vertex.y);
            min.z = Math.min(min.z, vertex.z);
            max.x = Math.max(max.x, vertex.x);
            max.y = Math.max(max.y, vertex.y);
            max.z = Math.max(max.z, vertex.z);
        }
    }

    public List<Vector3D> getVertices() {
        return vertices;
    }

    public Vector3D getMin() {
        return min;
    }

    public Vector3D getMax() {
        return max;
    }

    public boolean intersects(ConvexShape other) {
        return gjkIntersection(this, other);
    }

    private boolean gjkIntersection(ConvexShape shape1, ConvexShape shape2) {
        Vector3D[] simplex = new Vector3D[4];
        Vector3D direction = shape2.getVertices().get(0).subtract(shape1.getVertices().get(0));
        int simplexSize = 0;

        while (true) {
            Vector3D support = supportFunction(shape1, shape2, direction);
            if (support.dot(direction) <= 0) {
                return false;
            }
            simplex[simplexSize++] = support;

            if (containsOrigin(simplex, simplexSize)) {
                return true;
            }

            direction = updateDirection(simplex, simplexSize);
        }
    }

    private Vector3D supportFunction(ConvexShape shape1, ConvexShape shape2, Vector3D direction) {
        Vector3D support1 = farthestPointInDirection(shape1.getVertices(), direction);
        Vector3D support2 = farthestPointInDirection(shape2.getVertices(), direction.negate());
        return support1.subtract(support2);
    }

    private Vector3D farthestPointInDirection(List<Vector3D> vertices, Vector3D direction) {
        Vector3D farthestPoint = vertices.get(0);
        double maxDotProduct = farthestPoint.dot(direction);

        for (Vector3D vertex : vertices) {
            double dotProduct = vertex.dot(direction);
            if (dotProduct > maxDotProduct) {
                maxDotProduct = dotProduct;
                farthestPoint = vertex;
            }
        }

        return farthestPoint;
    }

    private boolean containsOrigin(Vector3D[] simplex, int simplexSize) {
        switch (simplexSize) {
            case 2:
                return containsOriginLine(simplex[0], simplex[1]);
            case 3:
                return containsOriginTriangle(simplex[0], simplex[1], simplex[2]);
            case 4:
                return containsOriginTetrahedron(simplex[0], simplex[1], simplex[2], simplex[3]);
            default:
                return false;
        }
    }

    private boolean containsOriginLine(Vector3D a, Vector3D b) {
        Vector3D ab = b.subtract(a);
        Vector3D ao = a.negate();
        Vector3D perp = ab.cross(ao).cross(ab);
        return perp.dot(ao) <= 0;
    }

    private boolean containsOriginTriangle(Vector3D a, Vector3D b, Vector3D c) {
        Vector3D ab = b.subtract(a);
        Vector3D ac = c.subtract(a);
        Vector3D ao = a.negate();
        Vector3D normal = ab.cross(ac);

        Vector3D perp1 = normal.cross(ab);
        if (perp1.dot(ao) > 0) {
            return containsOriginLine(a, b);
        }

        Vector3D perp2 = ac.cross(normal);
        if (perp2.dot(ao) > 0) {
            return containsOriginLine(a, c);
        }

        if (normal.dot(ao) > 0) {
            return containsOriginLine(a, b);
        }

        return true;
    }

    private boolean containsOriginTetrahedron(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {
        Vector3D ab = b.subtract(a);
        Vector3D ac = c.subtract(a);
        Vector3D ad = d.subtract(a);
        Vector3D ao = a.negate();

        Vector3D normal = ab.cross(ac);
        if (normal.dot(ad) * normal.dot(ao) > 0) {
            return containsOriginTriangle(a, b, c);
        }

        normal = ac.cross(ad);
        if (normal.dot(ab) * normal.dot(ao) > 0) {
            return containsOriginTriangle(a, c, d);
        }

        normal = ad.cross(ab);
        if (normal.dot(ac) * normal.dot(ao) > 0) {
            return containsOriginTriangle(a, d, b);
        }

        return true;
    }

    private Vector3D updateDirection(Vector3D[] simplex, int simplexSize) {
        switch (simplexSize) {
            case 2:
                return updateDirectionLine(simplex[0], simplex[1]);
            case 3:
                return updateDirectionTriangle(simplex[0], simplex[1], simplex[2]);
            case 4:
                return updateDirectionTetrahedron(simplex[0], simplex[1], simplex[2], simplex[3]);
            default:
                return new Vector3D(0, 0, 0);
        }
    }

    private Vector3D updateDirectionLine(Vector3D a, Vector3D b) {
        Vector3D ab = b.subtract(a);
        Vector3D ao = a.negate();
        return ab.cross(ab.cross(ao));
    }

    private Vector3D updateDirectionTriangle(Vector3D a, Vector3D b, Vector3D c) {
        Vector3D ab = b.subtract(a);
        Vector3D ac = c.subtract(a);
        Vector3D ao = a.negate();
        Vector3D normal = ab.cross(ac);

        if (normal.dot(ao) > 0) {
            return normal;
        }

        Vector3D perp1 = normal.cross(ab);
        if (perp1.dot(ao) > 0) {
            return perp1;
        }

        Vector3D perp2 = ac.cross(normal);
        if (perp2.dot(ao) > 0) {
            return perp2;
        }

        return normal;
    }

    private Vector3D updateDirectionTetrahedron(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {
        Vector3D ab = b.subtract(a);
        Vector3D ac = c.subtract(a);
        Vector3D ad = d.subtract(a);
        Vector3D ao = a.negate();

        Vector3D normal = ab.cross(ac);
        if (normal.dot(ad) * normal.dot(ao) > 0) {
            return normal;
        }

        normal = ac.cross(ad);
        if (normal.dot(ab) * normal.dot(ao) > 0) {
            return normal;
        }

        normal = ad.cross(ab);
        if (normal.dot(ac) * normal.dot(ao) > 0) {
            return normal;
        }

        return normal;
    }
}
