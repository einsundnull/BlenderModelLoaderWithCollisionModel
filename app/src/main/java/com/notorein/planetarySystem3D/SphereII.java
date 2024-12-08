package com.notorein.planetarySystem3D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

public class SphereII {
    private float trailThickness;
    private double trailLength;
    private String name;
    private Vector3D position = Vector3D.zero();
    private Vector3D velocity = Vector3D.zero();
    private double mass;
    private int color;
    private int colorTrail;
    private float size;
    private ArrayList<Vector3D> trail = new ArrayList<>(1500);
    private int positionIndex;

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;
    private FloatBuffer textureBuffer;
    private ShortBuffer indexBuffer;
    private int numIndices;

    private SphereII selectedSphereII;

    public SphereII(int positionIndex, double x, double y, double z, double vx, double vy, double vz, double mass, int color, int colorTrail, float size, double trailLength, float trailThickness, String name) {
        this.positionIndex = positionIndex;
        this.position = new Vector3D(x, y, z);
        this.velocity = new Vector3D(vx, vy, vz);
        this.mass = mass;
        this.color = color;
        this.colorTrail = colorTrail;
        this.size = size;
        this.trailLength = trailLength;
        this.trailThickness = trailThickness;
        this.name = name;

        generateSphereGeometry(size);
    }

    private void generateSphereGeometry(float radius) {
        int numStacks = 24;
        int numSlices = 24;
        float[] vertices = new float[(numStacks + 1) * (numSlices + 1) * 3];
        float[] normals = new float[(numStacks + 1) * (numSlices + 1) * 3];
        float[] textures = new float[(numStacks + 1) * (numSlices + 1) * 2];
        short[] indices = new short[numStacks * numSlices * 6];

        int vertexIndex = 0;
        int normalIndex = 0;
        int textureIndex = 0;
        int indexIndex = 0;

        for (int i = 0; i <= numStacks; i++) {
            float stackAngle = (float) (Math.PI / 2 - i * Math.PI / numStacks);
            float xy = (float) (radius * Math.cos(stackAngle));
            float z = (float) (radius * Math.sin(stackAngle));

            for (int j = 0; j <= numSlices; j++) {
                float sliceAngle = j * 2 * (float) Math.PI / numSlices;
                float x = (float) (xy * Math.cos(sliceAngle));
                float y = (float) (xy * Math.sin(sliceAngle));

                vertices[vertexIndex++] = x;
                vertices[vertexIndex++] = y;
                vertices[vertexIndex++] = z;

                normals[normalIndex++] = x / radius;
                normals[normalIndex++] = y / radius;
                normals[normalIndex++] = z / radius;

                textures[textureIndex++] = (float) j / numSlices;
                textures[textureIndex++] = (float) i / numStacks;
            }
        }

        for (int i = 0; i < numStacks; i++) {
            for (int j = 0; j < numSlices; j++) {
                int first = (i * (numSlices + 1)) + j;
                int second = first + numSlices + 1;

                indices[indexIndex++] = (short) first;
                indices[indexIndex++] = (short) (first + 1);
                indices[indexIndex++] = (short) second;

                indices[indexIndex++] = (short) (first + 1);
                indices[indexIndex++] = (short) (second + 1);
                indices[indexIndex++] = (short) second;
            }
        }

        numIndices = indexIndex;

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer nbb = ByteBuffer.allocateDirect(normals.length * 4);
        nbb.order(ByteOrder.nativeOrder());
        normalBuffer = nbb.asFloatBuffer();
        normalBuffer.put(normals);
        normalBuffer.position(0);

        ByteBuffer tbb = ByteBuffer.allocateDirect(textures.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        textureBuffer = tbb.asFloatBuffer();
        textureBuffer.put(textures);
        textureBuffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    public void draw(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

        gl.glDrawElements(GL10.GL_TRIANGLES, numIndices, GL10.GL_UNSIGNED_SHORT, indexBuffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    public synchronized void applyGravity(SphereII other) {
        double G = 0.1;
        Vector3D distanceVector = other.position.subtract(this.position);
        double distance = distanceVector.magnitude();
        if (distance < 10) return; // Avoid close interactions

        double force = G * this.mass * other.mass / (distance * distance);
        Vector3D forceVector = distanceVector.normalize().scale(force);

        Vector3D acceleration = forceVector.scale(1 / this.mass);
        this.velocity = this.velocity.add(acceleration);

        Vector3D accelerationOther = forceVector.scale(-1 / other.mass);
        other.velocity = other.velocity.add(accelerationOther);

        // Log the positions and velocities
        Log.d("Sphere", "Applying gravity: " + this.name + " to " + other.name);
        Log.d("Sphere", "Position: " + this.position + ", Velocity: " + this.velocity);
        Log.d("Sphere", "Other Position: " + other.position + ", Other Velocity: " + other.velocity);
    }

    public synchronized void updatePosition() {
        synchronized (trail) {
            // Save the current position before updating
            trail.add(new Vector3D(position.x, position.y, position.z));
            if (trail.size() > trailLength) { // Maintain the size of the trail
                trail.remove(0); // Remove the oldest position
            }
        }

        // Update position
        position = position.add(velocity);

        // Log the new position
        Log.d("Sphere", "Updating position: " + this.name);
        Log.d("Sphere", "New Position: " + this.position);
    }

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX() {
        return position.x;
    }

    public void setX(double x) {
        this.position.x = x;
    }

    public double getY() {
        return position.y;
    }

    public void setY(double y) {
        this.position.y = y;
    }

    public void setZ(double z) {
        this.position.z = z;
    }

    public double getZ() {
        return position.z;
    }

    public double getVx() {
        return velocity.x;
    }

    public void setVx(double vx) {
        this.velocity.x = vx;
    }

    public double getVz() {
        return velocity.z;
    }

    public void setVz(double vz) {
        this.velocity.z = vz;
    }

    public double getVy() {
        return velocity.y;
    }

    public void setVy(double vy) {
        this.velocity.y = vy;
    }

    public double getMass() {
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

    public void setColorTrail(int color) {
        this.colorTrail = color;
    }

    public float getSize() {
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
        return trail.size();
    }

    public float getTrailThickness() {
        return trailThickness;
    }

    public boolean shouldBlur() {
        // Example condition: blur if the size is below a certain threshold
        return size < 5.0f;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPosition(int position) {
        this.positionIndex = position;
    }

    // Inner class to represent a position
    public static class Position {
        private double x, y , z;

        public Position(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z= z;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getZ() {
            return z;
        }

        public void setZ(double z) {
            this.z = z;
        }
    }
}