package com.notorein.planetarySystem3D;

import static android.content.ContentValues.TAG;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

public abstract class Object {
    private String name;
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private FloatBuffer normalBuffer;
    private ShortBuffer indexBuffer;
    private int numIndices;

    private FloatBuffer textureBuffer;
    private ArrayList<Vector3D> trail;
    private Vector3D initialPosition;
    private Vector3D initialVelocity;
    private Vector3D position;
    private Vector3D velocity;
    private Vector3D velocityTilt;
    private double mass;
    private int color;
    private int colorTrail;
    private double size;

    private int trailLength;
    private float trailThickness;
    private boolean isTiltEnabled;
    private boolean attractsOther;
    private boolean isAttractedByOther;

    private boolean bouncesOff;
    private boolean followGravity;
    private int positionIndex;

    public Object(int positionIndex, Vector3D position, Vector3D velocity, Vector3D velocityTilt, double mass, int color, double size, boolean isTiltEnabled, boolean followGravity, boolean attractsOther, boolean isAttractedByOther, boolean bouncesOff, String name) {
        this.positionIndex = positionIndex;
        this.position = position;
        this.velocity = velocity;
        this.initialPosition = position;
        this.initialVelocity = velocity;
        this.velocityTilt = velocityTilt;
        this.mass = mass;
        this.color = color;
        this.size = size;
        this.followGravity = followGravity;
        this.isTiltEnabled = isTiltEnabled;
        this.attractsOther = attractsOther;
        this.isAttractedByOther = isAttractedByOther;
        this.bouncesOff = bouncesOff;
        this.name = name;
    }


    public boolean detectCollision(Object other) {
        double distance = this.position.distanceTo(other.position)* 2;
        double minDistance = (this.size / 2 + other.size / 2);
        Log.i(TAG, "detectCollision - distance: " + distance + " minDistance: " + minDistance);
        return distance <= minDistance;
    }


    public void handleCollision(Object other) {
        // Calculate the normal vector
        Vector3D normal = this.position.subtract(other.position).normalize();

        // Calculate the relative velocity
        Vector3D relativeVelocity = this.velocity.subtract(other.velocity);

        // Calculate the velocity along the normal
        double velAlongNormal = relativeVelocity.dot(normal);

        // If the spheres are moving away from each other, do nothing
        if (velAlongNormal > 0) return;

        // Calculate the impulse scalar
        double impulseScalar = -(1 + 1) * velAlongNormal; // 1 is the coefficient of restitution for elastic collision
        impulseScalar /= (1 / this.mass + 1 / other.mass);

        // Calculate the impulse vector
        Vector3D impulse = normal.scale(impulseScalar);

        // Apply the impulse to the velocities
        if (isBouncesOff()  && other.isBouncesOff()) {
            Log.i(TAG, "handleCollision: Both objects bounce off each other");
            // Both objects bounce off each other
            this.velocity = this.velocity.add(impulse.scale(1 / this.mass));
            other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
        } else if (isBouncesOff()  && !other.isBouncesOff()) {
            Log.i(TAG, "handleCollision: This object bounces off, but the other object does not");
            // This object bounces off, but the other object does not
            this.velocity = this.velocity.add(impulse.scale(1 / this.mass));
        } else if (!isBouncesOff() && other.isBouncesOff()) {
            Log.i(TAG, "handleCollision: This object does not bounce off, but the other object does");
            // This object does not bounce off, but the other object does
            other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
        }
    }


    public synchronized void applyGravity(Object other) {
        double G = 1;
        Vector3D distanceVector = other.position.subtract(this.position);
        double distance = distanceVector.magnitude();
        if (distance < 10) return; // Avoid close interactions

        double force = G * this.mass * other.mass / (distance * distance);
        Vector3D forceVector = distanceVector.normalize().scale(force);

        if (isAttractedByOther() && other.isAttractedByOther()) {
            Vector3D acceleration = forceVector.scale(1 / this.mass);
            this.velocity = this.velocity.add(acceleration);
            this.velocityTilt = this.velocityTilt.add(acceleration);
        }
        if (isAttractsOther() && other.isAttractedByOther()) {
            Vector3D accelerationOther = forceVector.scale(-1 / other.mass);
            other.velocity = other.velocity.add(accelerationOther);

        }
    }

    public synchronized void applyTilt(float[] tilt, float sensitivity) {
        if (isTiltEnabled) {
            // Adjust the velocity based on the tilt and sensitivity
            this.velocityTilt = this.velocityTilt.add(new Vector3D(tilt[0], -tilt[1],0 /* tilt[2]*/).scale(sensitivity));
        }
    }

    public abstract void draw(int program, float[] mvpMatrix, float[] modelMatrix,float[] projectionMatrix, GLES20 gl);

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public FloatBuffer getColorBuffer() {
        return colorBuffer;
    }

    public FloatBuffer getNormalBuffer() {
        return normalBuffer;
    }

    public ShortBuffer getIndexBuffer() {
        return indexBuffer;
    }

    public void setVertexBuffer(FloatBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    public void setColorBuffer(FloatBuffer colorBuffer) {
        this.colorBuffer = colorBuffer;
    }

    public void setNormalBuffer(FloatBuffer normalBuffer) {
        this.normalBuffer = normalBuffer;
    }

    public void setIndexBuffer(ShortBuffer indexBuffer) {
        this.indexBuffer = indexBuffer;
    }

    public int getNumIndices() {
        return numIndices;
    }

    public void setNumIndices(int numIndices) {
        this.numIndices = numIndices;
    }

    public synchronized void updatePosition() {
        // Update position
        position = position.add(velocity);
        position = position.add(velocityTilt);
    }


    public synchronized void speedUp() {
        this.velocity = this.velocity.scale(1.1); // Increase velocity by 10%
    }

    public synchronized void speedDown() {
        this.velocity = this.velocity.scale(1.1); // Decrease velocity by 10%
    }

    public synchronized void speedUpTilt() {
        this.velocityTilt = this.velocityTilt.scale(1.1); // Increase velocity by 10%
    }

    public synchronized void speedDownTilt() {
        this.velocityTilt = this.velocityTilt.scale(1.1); // Decrease velocity by 10%
    }

    // Getter and Setter methods
    public Vector3D getPosition() {
        return position;
    }

    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public Vector3D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3D velocity) {
        this.velocity = velocity;
    }

    public double getMass() {
        if (mass <= 0) {
            return 1;
        }
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColorTrail() {
        return colorTrail;
    }

    public void setColorTrail(int colorTrail) {
        this.colorTrail = colorTrail;
    }

    public double getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public ArrayList<Vector3D> getTrail() {
        return trail;
    }

    public void setTrail(ArrayList<Vector3D> trail) {
        this.trail = trail;
    }

    public int getTrailLength() {
        return trailLength;
    }

    public void setTrailLength(int trailLength) {
        this.trailLength = trailLength;
    }

    public float getTrailThickness() {
        return trailThickness;
    }

    public void setTrailThickness(float trailThickness) {
        this.trailThickness = trailThickness;
    }

    public boolean isTiltEnabled() {
        return isTiltEnabled;
    }

    public void setTiltEnabled(boolean tiltEnabled) {
        this.isTiltEnabled = tiltEnabled;
    }

    public boolean isFollowGravity() {
        return followGravity;
    }

    public void setFollowGravity() {
        this.followGravity = followGravity;
    }

    public boolean isAttractsOther() {
        return attractsOther;
    }

    public boolean isAttractedByOther() {
        return isAttractedByOther;
    }

    public void setAttractsOther(boolean attractsOther) {
        this.attractsOther = attractsOther;
    }

    public void setAttractedByOther(boolean isAttractedByOther) {
        this.isAttractedByOther = isAttractedByOther;
    }

    private boolean isBouncesOff() {
        return bouncesOff ;
    }

    private void setBouncesOff(boolean bouncesOff) {
        this.bouncesOff = bouncesOff;
    }

    public Vector3D getVelocityTilt() {
        return velocityTilt;
    }

    public void setVelocityTilt(Vector3D velocityTilt) {
        this.velocityTilt = velocityTilt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        setName(name);
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    public double getX() {
        return position.x;
    }

    public void setX(double x) {
        position.x = x;
    }

    public double getY() {
        return position.y;
    }

    public void setY(double y) {
        position.y = y;
    }

    public double getZ() {
        return position.z;
    }

    public void setZ(double z) {
        position.z = z;
    }

    public double getVx() {
        return velocity.x;
    }

    public void setVx(double vx) {
        velocity.x = vx;
    }

    public double getVy() {
        return velocity.y;
    }

    public void setVy(double vy) {
        velocity.y = vy;
    }

    public double getVz() {
        return velocity.z;
    }

    public void setVz(double vz) {
        velocity.z = vz;
    }

    public synchronized void reset() {
        setPosition(new Vector3D(initialPosition.x, initialPosition.y, initialPosition.z));
        setVelocity(new Vector3D(initialVelocity.x, initialVelocity.y, initialVelocity.z));
        setVelocityTilt(new Vector3D(initialVelocity.x, initialVelocity.y, initialVelocity.z));

    }
}
