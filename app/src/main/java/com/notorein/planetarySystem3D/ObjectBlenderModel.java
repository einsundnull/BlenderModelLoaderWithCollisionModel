package com.notorein.planetarySystem3D;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class ObjectBlenderModel {
    private final Context context;
    private final String objFileName;
    private final CollisionModelModelLoader collisionModelModelLoader;
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
    private boolean isTiltEnabled;
    private boolean attractsOther;
    private boolean isAttractedByOther;
    private boolean bouncesOff;
    private boolean followGravity;
    private int positionIndex;
    private double gravityStrength = 10;
    CollisionModelAABB boundingVolume;
    private boolean useConstantGravity = true;
    public static boolean drawBoundingVolume = true; // Flag to control bounding volume drawing

    float[] colors = new float[]{
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f
    };

    float[] colorsIntersect = new float[]{
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f
    };

    public ObjectBlenderModel(Context context, int positionIndex, Vector3D position, Vector3D velocity, Vector3D velocityTilt, double mass, int color, double size, boolean isTiltEnabled, boolean followGravity, boolean attractsOther, boolean isAttractedByOther, boolean bouncesOff, String name, String objFileName) {
        this.context = context;
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
        this.objFileName = objFileName;
        collisionModelModelLoader = new CollisionModelModelLoader();
        loadObjModel(context, objFileName);
        updateBoundingVolume();
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    private void loadObjModel(Context context, String objFileName) {
        String objFilePath = "models/" + objFileName + ".obj";
        List<CollisionModelTriangle> collisionModelTriangles = collisionModelModelLoader.loadTrianglesFromOBJ(context, objFileName);

        List<Vector3D> verticesObjModel = collisionModelModelLoader.getVertices();
        List<Vector3D> normalsObjModel = collisionModelModelLoader.getNormals();
        List<Vector2D> texCoordsObjModel = collisionModelModelLoader.getTexCoords();

        float[] vertexArray = new float[verticesObjModel.size() * 3];
        for (int i = 0; i < verticesObjModel.size(); i++) {
            Vector3D vertex = verticesObjModel.get(i);
            vertexArray[i * 3] = (float) vertex.x;
            vertexArray[i * 3 + 1] = (float) vertex.y;
            vertexArray[i * 3 + 2] = (float) vertex.z;
        }

        float[] normalArray = new float[normalsObjModel.size() * 3];
        for (int i = 0; i < normalsObjModel.size(); i++) {
            Vector3D normal = normalsObjModel.get(i);
            normalArray[i * 3] = (float) normal.x;
            normalArray[i * 3 + 1] = (float) normal.y;
            normalArray[i * 3 + 2] = (float) normal.z;
        }

        short[] indexArray = new short[collisionModelTriangles.size() * 3];
        for (int i = 0; i < collisionModelTriangles.size(); i++) {
            CollisionModelTriangle collisionModelTriangle = collisionModelTriangles.get(i);
            indexArray[i * 3] = (short) verticesObjModel.indexOf(collisionModelTriangle.getVertices()[0]);
            indexArray[i * 3 + 1] = (short) verticesObjModel.indexOf(collisionModelTriangle.getVertices()[1]);
            indexArray[i * 3 + 2] = (short) verticesObjModel.indexOf(collisionModelTriangle.getVertices()[2]);
        }

        numIndices = indexArray.length;

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexArray.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertexArray);
        vertexBuffer.position(0);

        ByteBuffer nbb = ByteBuffer.allocateDirect(normalArray.length * 4);
        nbb.order(ByteOrder.nativeOrder());
        normalBuffer = nbb.asFloatBuffer();
        normalBuffer.put(normalArray);
        normalBuffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(indexArray.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indexArray);
        indexBuffer.position(0);

        float r = ((getColor() >> 16) & 0xFF) / 255.0f;
        float g = ((getColor() >> 8) & 0xFF) / 255.0f;
        float b = (getColor() & 0xFF) / 255.0f;
        float a = ((getColor() >> 24) & 0xFF) / 255.0f;

        float[] colors = new float[vertexArray.length / 3 * 4];
        for (int i = 0; i < colors.length; i += 4) {
            colors[i] = r;
            colors[i + 1] = g;
            colors[i + 2] = b;
            colors[i + 3] = a;
        }

        colorBuffer = ByteBuffer.allocateDirect(colors.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);
    }

    private void updateBoundingVolume() {
        Vector3D min = new Vector3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3D max = new Vector3D(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

        for (int i = 0; i < numIndices; i += 3) {
            Vector3D v0 = new Vector3D(vertexBuffer.get(indexBuffer.get(i) * 3), vertexBuffer.get(indexBuffer.get(i) * 3 + 1), vertexBuffer.get(indexBuffer.get(i) * 3 + 2));
            Vector3D v1 = new Vector3D(vertexBuffer.get(indexBuffer.get(i + 1) * 3), vertexBuffer.get(indexBuffer.get(i + 1) * 3 + 1), vertexBuffer.get(indexBuffer.get(i + 1) * 3 + 2));
            Vector3D v2 = new Vector3D(vertexBuffer.get(indexBuffer.get(i + 2) * 3), vertexBuffer.get(indexBuffer.get(i + 2) * 3 + 1), vertexBuffer.get(indexBuffer.get(i + 2) * 3 + 2));

            min.x = Math.min(min.x, Math.min(Math.min(v0.x, v1.x), v2.x));
            min.y = Math.min(min.y, Math.min(Math.min(v0.y, v1.y), v2.y));
            min.z = Math.min(min.z, Math.min(Math.min(v0.z, v1.z), v2.z));
            max.x = Math.max(max.x, Math.max(Math.max(v0.x, v1.x), v2.x));
            max.y = Math.max(max.y, Math.max(Math.max(v0.y, v1.y), v2.y));
            max.z = Math.max(max.z, Math.max(Math.max(v0.z, v1.z), v2.z));
        }

        this.boundingVolume = new CollisionModelAABB(min, max);
        Log.i(TAG, "updateBoundingVolume: " + this.getName() + " Bounding Volume: " + boundingVolume);
    }

    public boolean detectCollision(ObjectBlenderModel other) {
        boolean collision = this.boundingVolume.intersects(other.boundingVolume);
        Log.i(TAG, "detectCollision: " + this.getName() + " with " + other.getName() + " collision: " + collision);
        if (collision) {
            color = Color.GREEN; // Change color to green
            Log.i(TAG, "detectCollision: Collision detected intersection");
        } else {
            color = Color.RED; // Change color to red
        }
        return collision;
    }

    public void drawObject(int program, float[] mvpMatrix, float[] viewMatrix, float[] projectionMatrix) {
        int aPositionLocation = GLES20.glGetAttribLocation(program, "a_Position");
        int aNormalLocation = GLES20.glGetAttribLocation(program, "a_Normal");
        int aColorLocation = GLES20.glGetAttribLocation(program, "a_Color");
        int uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
        int uModelMatrixLocation = GLES20.glGetUniformLocation(program, "u_ModelMatrix");

        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, (float) getPosition().x, (float) getPosition().y, (float) getPosition().z);
        Matrix.scaleM(modelMatrix, 0, (float) getSize(), (float) getSize(), (float) getSize());

        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(aNormalLocation, 3, GLES20.GL_FLOAT, false, 0, normalBuffer);
        GLES20.glVertexAttribPointer(aColorLocation, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(uModelMatrixLocation, 1, false, modelMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
    }

    public void drawBoundingVolume(int program, float[] mvpMatrix, float[] viewMatrix, float[] projectionMatrix) {
        if (!drawBoundingVolume) return;

        // Bind attributes, uniforms, and draw geometry
        int aPositionLocation = GLES20.glGetAttribLocation(program, "a_Position");
        int aColorLocation = GLES20.glGetAttribLocation(program, "a_Color");
        int uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix");

        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glEnableVertexAttribArray(aColorLocation);

        // Define the vertices of the bounding box
        float[] vertices = new float[]{
                (float) boundingVolume.min.x, (float) boundingVolume.min.y, (float) boundingVolume.min.z,
                (float) boundingVolume.max.x, (float) boundingVolume.min.y, (float) boundingVolume.min.z,
                (float) boundingVolume.max.x, (float) boundingVolume.max.y, (float) boundingVolume.min.z,
                (float) boundingVolume.min.x, (float) boundingVolume.max.y, (float) boundingVolume.min.z,
                (float) boundingVolume.min.x, (float) boundingVolume.min.y, (float) boundingVolume.max.z,
                (float) boundingVolume.max.x, (float) boundingVolume.min.y, (float) boundingVolume.max.z,
                (float) boundingVolume.max.x, (float) boundingVolume.max.y, (float) boundingVolume.max.z,
                (float) boundingVolume.min.x, (float) boundingVolume.max.y, (float) boundingVolume.max.z
        };

        // Define the colors of the bounding box (e.g., red color)
        float[] colors = color == Color.GREEN ? colorsIntersect : this.colors;

        // Create buffers for the vertices and colors
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices).position(0);

        FloatBuffer colorBuffer = ByteBuffer.allocateDirect(colors.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.put(colors).position(0);

        // Define the indices for the bounding box
        short[] indices = new short[]{
                0, 1, 1, 2, 2, 3, 3, 0,
                4, 5, 5, 6, 6, 7, 7, 4,
                0, 4, 1, 5, 2, 6, 3, 7
        };

        ShortBuffer indexBuffer = ByteBuffer.allocateDirect(indices.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        indexBuffer.put(indices).position(0);

        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(aColorLocation, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_LINES, indices.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aColorLocation);
    }

    public void enableBoundingVolumeDrawing() {
        drawBoundingVolume = true;
    }

    public void disableBoundingVolumeDrawing() {
        drawBoundingVolume = false;
    }

    public void handleCollision(ObjectBlenderModel other) {
        Vector3D normal = this.position.subtract(other.position).normalize();
        Vector3D relativeVelocity = this.velocity.subtract(other.velocity);
        double velAlongNormal = relativeVelocity.dot(normal);
        Log.i(TAG, "handleCollision: velAlongNormal " + velAlongNormal);
        if (velAlongNormal > 0) return;

        double impulseScalar = -(1 + 1) * velAlongNormal;
        impulseScalar /= (1 / this.mass + 1 / other.mass);

        Vector3D impulse = normal.scale(impulseScalar);
        Log.i(TAG, "handleCollision: " + other.getName());
        if (isBouncesOff() && other.isBouncesOff()) {
            this.velocity = this.velocity.add(impulse.scale(1 / this.mass));
            other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
            Log.i(TAG, "handleCollision: I " + other.getName());
        } else if (isBouncesOff() && !other.isBouncesOff()) {
            this.velocity = this.velocity.add(impulse.scale(1 / this.mass));
            Log.i(TAG, "handleCollision: II " + other.getName());
        } else if (!isBouncesOff() && other.isBouncesOff()) {
            Log.i(TAG, "handleCollision: III " + other.getName());
            other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
        }

        // Update positions and velocities
        this.updatePosition();
        other.updatePosition();
    }

    public synchronized void applyGravity(ObjectBlenderModel other) {
        if (useConstantGravity) {
            if (isFollowGravity())
                applyConstantGravity();
        } else {
            applyDynamicGravity(other);
        }
    }

    private void applyConstantGravity() {
        Vector3D constantForce = new Vector3D(0, -9.8, 0);
        Vector3D acceleration = constantForce.scale(1 / this.mass);
        this.velocity = this.velocity.add(acceleration);
        this.velocityTilt = this.velocityTilt.add(acceleration);
    }

    private void applyDynamicGravity(ObjectBlenderModel other) {
        double G = gravityStrength;
        Vector3D distanceVector = other.position.subtract(this.position);
        double distance = distanceVector.magnitude();
        if (distance < 10) return;

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

    public boolean isUseConstantGravity() {
        return useConstantGravity;
    }
    public void setUseConstantGravity(boolean useConstantGravity) {
        this.useConstantGravity = useConstantGravity;
    }

    public synchronized void applyTilt(float[] tilt, float sensitivity) {
        if (isTiltEnabled()) {
            this.velocityTilt = this.velocityTilt.add(new Vector3D(tilt[0], -tilt[1], 0).scale(sensitivity));
        }
    }

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

    public int getNumIndices() {
        return numIndices;
    }

    public synchronized void updatePosition() {
        position = position.add(velocity);
        if (isTiltEnabled()) {
            position = position.add(velocityTilt);
        }
        // Update the bounding volume
        updateBoundingVolume();
        Log.i(TAG, "updatePosition: " + this.getName() + " Position: " + position + " Velocity: " + velocity + " Bounding Volume: " + boundingVolume);
    }

    public double getGravityStrength() {
        return gravityStrength;
    }

    public void setGravityStrength(double gravityStrength) {
        this.gravityStrength = gravityStrength;
    }

    public synchronized void speedUp() {
        this.velocity = this.velocity.scale(1.1);
    }

    public synchronized void speedDown() {
        this.velocity = this.velocity.scale(1.1);
    }

    public synchronized void speedUpTilt() {
        this.velocityTilt = this.velocityTilt.scale(1.1);
    }

    public synchronized void speedDownTilt() {
        this.velocityTilt = this.velocityTilt.scale(1.1);
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
        return bouncesOff;
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
        this.name = name;
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

    public void toggleConstantGravity() {
        useConstantGravity = !useConstantGravity;
    }
}
