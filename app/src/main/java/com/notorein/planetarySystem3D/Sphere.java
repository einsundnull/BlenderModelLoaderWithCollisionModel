package com.notorein.planetarySystem3D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

public class Sphere {

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ByteBuffer indexBuffer;

    private float radius;
    private double posX, posY, posZ;
    private float vX, vY, vZ;
    private int stacks;

    private float trailThickness;
    private double trailLength;
    private String name;
    private Vector3D position;
    private Vector3D velocity;
    private double mass;
    private int color;
    private int colorTrail;
    private float size;
    private ArrayList<Vector3D> trail;
    private int positionIndex;

    private FloatBuffer normalBuffer;
    private FloatBuffer textureBuffer;
    private ShortBuffer indexBufferII;
    private int numIndices;

    private Sphere selectedSphere;

    private float vx, vy, vz;

    public Sphere(int positionIndex, double posX, double posY, double posZ, double vx, double vy, double vz, double mass, int color, int colorTrail, float radius, double trailLength, float trailThickness, String name) {

        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.stacks = 10;
        int slices = 10;
        this.positionIndex = positionIndex;
        this.position = new Vector3D(this.posX, this.posY , this.posZ);
        this.velocity = new Vector3D(vx, vy, vz);
        this.mass = mass;
        this.color = color;
        this.colorTrail = colorTrail;
        this.radius = radius;
        this.trailLength = trailLength;
        this.trailThickness = trailThickness;
        this.name = name;
        this.trail = new ArrayList<>(1500);

        float[] vertices = new float[(stacks + 1) * (slices + 1) * 3];
        float[] colors = new float[(stacks + 1) * (slices + 1) * 4];
        byte[] indices = new byte[stacks * slices * 6];

        int vertexIndex = 0;
        int colorIndex = 0;
        int indexIndex = 0;

        // Extract RGBA components from the color integer
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        for (int i = 0; i <= stacks; i++) {
            float stackAngle = (float) (Math.PI * (i / (float) stacks) - Math.PI / 2);
            float xy = (float) (radius * Math.cos(stackAngle));
            float zi = (float) (radius * Math.sin(stackAngle));

            for (int j = 0; j <= slices; j++) {
                float sliceAngle = (float) (2 * Math.PI * j / slices);
                float xi = (float) (xy * Math.cos(sliceAngle));
                float yi = (float) (xy * Math.sin(sliceAngle));

                vertices[vertexIndex++] = xi;
                vertices[vertexIndex++] = yi;
                vertices[vertexIndex++] = zi;

                colors[colorIndex++] = r;
                colors[colorIndex++] = g;
                colors[colorIndex++] = b;
                colors[colorIndex++] = a;
            }
        }

        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int first = (i * (slices + 1)) + j;
                int second = first + slices + 1;

                indices[indexIndex++] = (byte) first;
                indices[indexIndex++] = (byte) second;
                indices[indexIndex++] = (byte) (first + 1);

                indices[indexIndex++] = (byte) second;
                indices[indexIndex++] = (byte) (second + 1);
                indices[indexIndex++] = (byte) (first + 1);
            }
        }

        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        colorBuffer = ByteBuffer.allocateDirect(colors.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length).order(ByteOrder.nativeOrder());
        indexBuffer.put(indices);
        indexBuffer.position(0);

        generateSphereGeometry(radius, stacks, slices);
    }

    private void generateSphereGeometry(float radius, int stacks, int slices) {
        float[] vertices = new float[(stacks + 1) * (slices + 1) * 3];
        float[] normals = new float[(stacks + 1) * (slices + 1) * 3];
        float[] textures = new float[(stacks + 1) * (slices + 1) * 2];
        short[] indices = new short[stacks * slices * 6];

        int vertexIndex = 0;
        int normalIndex = 0;
        int textureIndex = 0;
        int indexIndex = 0;

        for (int i = 0; i <= stacks; i++) {
            float stackAngle = (float) (Math.PI / 2 - i * Math.PI / stacks);
            float xy = (float) (radius * Math.cos(stackAngle));
            float z = (float) (radius * Math.sin(stackAngle));

            for (int j = 0; j <= slices; j++) {
                float sliceAngle = j * 2 * (float) Math.PI / slices;
                float x = (float) (xy * Math.cos(sliceAngle));
                float y = (float) (xy * Math.sin(sliceAngle));

                vertices[vertexIndex++] = x;
                vertices[vertexIndex++] = y;
                vertices[vertexIndex++] = z;

                normals[normalIndex++] = x / radius;
                normals[normalIndex++] = y / radius;
                normals[normalIndex++] = z / radius;

                textures[textureIndex++] = (float) j / slices;
                textures[textureIndex++] = (float) i / stacks;
            }
        }

        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int first = (i * (slices + 1)) + j;
                int second = first + slices + 1;

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
        normalBuffer = vbb.asFloatBuffer();
        normalBuffer.put(vertices);
        normalBuffer.position(0);

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
        indexBufferII = ibb.asShortBuffer();
        indexBufferII.put(indices);
        indexBufferII.position(0);
    }

    public void draw(GL10 gl) {
        gl.glPushMatrix();
        gl.glTranslatef((float) posX, (float) posY, (float) posZ);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);

        gl.glDrawElements(GL10.GL_TRIANGLES, indexBuffer.capacity(), GL10.GL_UNSIGNED_BYTE, indexBuffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        gl.glPopMatrix();
    }

    public synchronized void applyGravity(Sphere other) {
        double G = 0.0001;
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
//        Log.d("Sphere", "Applying gravity: " + this.name + " to " + other.name);
//        Log.d("Sphere", "Position: " + this.position + ", Velocity: " + this.velocity);
//        Log.d("Sphere", "Other Position: " + other.position + ", Other Velocity: " + other.velocity);
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
        posX= position.x;
        posY = position.y;
       posZ = position.z;

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

    public double getX() {
        return posX;
    }

    public void setX(int x) {
        this.posX = x;
    }

    public double getY() {
        return posY;
    }

    public void setY(int y) {
        this.posY = y;
    }

    public double getZ() {
        return posZ;
    }

    public void setZ(int z) {
        this.posZ = z;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(float vx) {
        this.vx = vx;
    }

    public double getVz() {
        return vz;
    }

    public void setVz(float vz) {
        this.vz = vz;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(float vy) {
        this.vy = vy;
    }

    // Inner class to represent a position
    public static class Position {
        private double x, y, z;

        public Position(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
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
