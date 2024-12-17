package com.notorein.threedmodeling;

public class CollisionRectangle {
    public double x, y;
    public double width, height;

    public CollisionRectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(CollisionRectangle r) {
        return (r.x >= x &&
                r.x + r.width <= x + width &&
                r.y >= y &&
                r.y + r.height <= y + height);
    }

    public boolean intersects(CollisionRectangle r) {
        return !(r.x > x + width ||
                r.x + r.width < x ||
                r.y > y + height ||
                r.y + r.height < y);
    }
}
