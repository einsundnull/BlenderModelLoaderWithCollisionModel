package com.notorein.threedmodeling;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

public class CollisionModelAABB extends CollisionModelBoundingVolume {
    Vector3D min;
    Vector3D max;

    public CollisionModelAABB(Vector3D min, Vector3D max) {
        this.min = min;
        this.max = max;
    }

    private static final float EPSILON = 1e-6f;

    @Override
    public boolean intersects(CollisionModelBoundingVolume other) {
        if (other instanceof CollisionModelAABB) {
            CollisionModelAABB otherAABB = (CollisionModelAABB) other;
            boolean intersects = (this.min.x - EPSILON < otherAABB.max.x && this.max.x + EPSILON > otherAABB.min.x) &&
                    (this.min.y - EPSILON < otherAABB.max.y && this.max.y + EPSILON > otherAABB.min.y) &&
                    (this.min.z - EPSILON < otherAABB.max.z && this.max.z + EPSILON > otherAABB.min.z);

            Log.i(TAG, "intersects: Min: " + this.min + " Max: " + this.max + " Other Min: " + otherAABB.min + " Other Max: " + otherAABB.max);
            return intersects;
        }
        return false;
    }


    @Override
    public String toString() {
        return "AABB[min=" + min + ", max=" + max + "]";
    }
}
