package com.notorein.planetarySystem3D;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CollisionModelBVHNode {
    private static final int TRIANGLE_THRESHOLD = 1; // Define the threshold
    CollisionModelBoundingVolume boundingVolume;
    private List<CollisionModelBVHNode> children;
    private List<CollisionModelTriangle> collisionModelTriangles;

    public CollisionModelBVHNode(CollisionModelBoundingVolume boundingVolume, List<CollisionModelTriangle> collisionModelTriangles) {
        this.boundingVolume = boundingVolume;
        this.collisionModelTriangles = collisionModelTriangles;
        this.children = new ArrayList<>();
    }

    public void addChild(CollisionModelBVHNode child) {
        children.add(child);
    }

    public boolean detectCollision(CollisionModelBVHNode other) {
        if (!this.boundingVolume.intersects(other.boundingVolume)) {
            return false;
        }
        if (this.children.isEmpty() && other.children.isEmpty()) {
            return checkTriangleCollision(this.collisionModelTriangles, other.collisionModelTriangles);
        }
        for (CollisionModelBVHNode child : this.children) {
            if (child.detectCollision(other)) {
                return true;
            }
        }
        for (CollisionModelBVHNode otherChild : other.children) {
            if (detectCollision(otherChild)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkTriangleCollision(List<CollisionModelTriangle> triangles1, List<CollisionModelTriangle> triangles2) {
        for (CollisionModelTriangle t1 : triangles1) {
            for (CollisionModelTriangle t2 : triangles2) {
                if (t1.intersects(t2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private CollisionModelBVHNode buildBVH(List<CollisionModelTriangle> collisionModelTriangles, float size) {
        if (collisionModelTriangles.size() <= TRIANGLE_THRESHOLD) {
            CollisionModelAABB boundingVolume = calculateBoundingVolume(collisionModelTriangles, size);
            return new CollisionModelBVHNode(boundingVolume, collisionModelTriangles);
        }

        List<CollisionModelTriangle> leftCollisionModelTriangles = new ArrayList<>();
        List<CollisionModelTriangle> rightCollisionModelTriangles = new ArrayList<>();
        Vector3D centroid = calculateCentroid(collisionModelTriangles);

        for (CollisionModelTriangle collisionModelTriangle : collisionModelTriangles) {
            if (collisionModelTriangle.getCentroid().x < centroid.x) {
                leftCollisionModelTriangles.add(collisionModelTriangle);
            } else {
                rightCollisionModelTriangles.add(collisionModelTriangle);
            }
        }

        CollisionModelBVHNode leftChild = buildBVH(leftCollisionModelTriangles, size);
        CollisionModelBVHNode rightChild = buildBVH(rightCollisionModelTriangles, size);

        CollisionModelAABB boundingVolume = calculateBoundingVolume(collisionModelTriangles, size);
        CollisionModelBVHNode node = new CollisionModelBVHNode(boundingVolume, collisionModelTriangles);
        node.addChild(leftChild);
        node.addChild(rightChild);

        return node;
    }

    private CollisionModelAABB calculateBoundingVolume(List<CollisionModelTriangle> collisionModelTriangles, float size) {
        Vector3D min = new Vector3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3D max = new Vector3D(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

        for (CollisionModelTriangle collisionModelTriangle : collisionModelTriangles) {
            for (Vector3D vertex : collisionModelTriangle.getVertices()) {
                min.x = Math.min(min.x, vertex.x * size);
                min.y = Math.min(min.y, vertex.y * size);
                min.z = Math.min(min.z, vertex.z * size);
                max.x = Math.max(max.x, vertex.x * size);
                max.y = Math.max(max.y, vertex.y * size);
                max.z = Math.max(max.z, vertex.z * size);
            }
        }

        Log.i(TAG, "calculateBoundingVolume: Min: " + min + " Max: " + max);
        return new CollisionModelAABB(min, max);
    }

    private Vector3D calculateCentroid(List<CollisionModelTriangle> collisionModelTriangles) {
        Vector3D centroid = new Vector3D(0, 0, 0);
        for (CollisionModelTriangle collisionModelTriangle : collisionModelTriangles) {
            centroid = centroid.add(collisionModelTriangle.getCentroid());
        }
        centroid = centroid.scale(1.0f / collisionModelTriangles.size());
        return centroid;
    }
}
