package com.notorein.planetarySystem3D;

public class CollisionModelAABBHEAVY extends CollisionModelBoundingVolume {
    private Vector3D min;
    private Vector3D max;

    public CollisionModelAABBHEAVY(Vector3D min, Vector3D max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean intersects(CollisionModelBoundingVolume other) {
        if (other instanceof CollisionModelAABBHEAVY) {
            CollisionModelAABBHEAVY otherCollisionModelAABB = (CollisionModelAABBHEAVY) other;
            return (this.min.x <= otherCollisionModelAABB.max.x && this.max.x >= otherCollisionModelAABB.min.x) &&
                    (this.min.y <= otherCollisionModelAABB.max.y && this.max.y >= otherCollisionModelAABB.min.y) &&
                    (this.min.z <= otherCollisionModelAABB.max.z && this.max.z >= otherCollisionModelAABB.min.z);
        }
        return false;
    }

    public Vector3D[] getVertices() {
        return new Vector3D[] {
                new Vector3D(min.x, min.y, min.z),
                new Vector3D(max.x, min.y, min.z),
                new Vector3D(max.x, max.y, min.z),
                new Vector3D(min.x, max.y, min.z),
                new Vector3D(min.x, min.y, max.z),
                new Vector3D(max.x, min.y, max.z),
                new Vector3D(max.x, max.y, max.z),
                new Vector3D(min.x, max.y, max.z)
        };
    }
}
