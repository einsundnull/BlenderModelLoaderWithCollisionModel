package com.notorein.threedmodeling;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.notorein.threedmodeling.ObjectLoaderBlenderModel.createFloatBuffer;
import static com.notorein.threedmodeling.ObjectLoaderBlenderModel.createShortBuffer;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.notorein.threedmodeling.utils.Vector3D;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class ObjectBlenderModel {
    private final Context context;
    private final String objFileName;
    private final ObjectLoaderTriangle objectLoaderTriangle;
    private final ObjectLoaderBlenderModel objectLoader;
    public String name;
    public FloatBuffer vertexBuffer;
    public FloatBuffer colorBuffer;
    public FloatBuffer normalBuffer;
    public ShortBuffer indexBuffer;
    public int numIndices;
    public FloatBuffer textureBuffer;
    public ArrayList<Vector3D> trail;
    public Vector3D initialPosition;
    public Vector3D initialVelocity;
    public Vector3D position;
    public Vector3D velocity;
    public double mass;
    public int color;
    public int colorTrail;
    public double size;
    public boolean isTiltEnabled;
    public boolean attractsOther;
    public boolean isAttractedByOther;
    public boolean bouncesOff;
    public boolean followGravity;
    public int positionIndex;
    public double gravityStrength = 1;
    public int colorInitial;

    // Rotation angles around x, y, and z axes
    public float rotationX = 0;
    public float rotationY = 0;
    public float rotationZ = 0;

    ConvexShape boundingVolume;
    public boolean useConstantGravity = true;
    public static boolean drawBoundingVolume = true;

    private static final float[] DEFAULT_COLOR = {
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f
    };

    private static final float[] COLLISION_COLOR = {
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f
    };
    public Vector3D localBoundingVolumeCenter;
    public Vector3D worldBoundingVolumeCenter;
    public Vector3D opticalCenter;

    public ObjectBlenderModel(Context context, int positionIndex, Vector3D position, Vector3D velocity, double mass, int color, double size, boolean isTiltEnabled, boolean followGravity, boolean attractsOther, boolean isAttractedByOther, boolean bouncesOff, String name, String objFileName) {
        this.context = context;
        this.positionIndex = positionIndex;
        this.position = position;
        this.velocity = velocity;
        this.initialPosition = position;
        this.initialVelocity = velocity;
        this.mass = mass;
        this.color = color;
        this.colorInitial = color;
        this.size = size;
        this.followGravity = followGravity;
        this.isTiltEnabled = isTiltEnabled;
        this.attractsOther = attractsOther;
        this.isAttractedByOther = isAttractedByOther;
        this.bouncesOff = bouncesOff;
        this.name = name;
        this.objFileName = objFileName;
        objectLoaderTriangle = new ObjectLoaderTriangle();
        objectLoader = new ObjectLoaderBlenderModel();
        objectLoader.loadObjModel(context, objFileName, color, objectLoaderTriangle);

        numIndices = objectLoader.getNumIndices();
        vertexBuffer = objectLoader.getVertexBuffer();
        normalBuffer = objectLoader.getNormalBuffer();
        indexBuffer = objectLoader.getIndexBuffer();
        colorBuffer = objectLoader.getColorBuffer();

        updateBoundingVolume();
    }

    public void updateColorBuffer(int color) {
        objectLoader.updateColorBuffer(color);
        colorBuffer = objectLoader.getColorBuffer();
    }

    public void updateColorBuffer() {
        objectLoader.updateColorBuffer(color);
        colorBuffer = objectLoader.getColorBuffer();
    }

    private void updateBoundingVolume() {
        List<Vector3D> vertices = new ArrayList<>();
        for (int i = 0; i < numIndices; i += 3) {
            Vector3D v0 = getVertex(indexBuffer.get(i));
            Vector3D v1 = getVertex(indexBuffer.get(i + 1));
            Vector3D v2 = getVertex(indexBuffer.get(i + 2));
            vertices.add(v0);
            vertices.add(v1);
            vertices.add(v2);
        }
        this.boundingVolume = new ConvexShape(vertices);
    }

    private Vector3D getVertex(int index) {
        int vertexIndex = index * 3;
        return new Vector3D(vertexBuffer.get(vertexIndex), vertexBuffer.get(vertexIndex + 1), vertexBuffer.get(vertexIndex + 2));
    }

    private Vector3D getNormal(int index) {
        int normalIndex = index * 3;
        if (normalIndex + 2 >= normalBuffer.capacity()) {
            throw new IndexOutOfBoundsException("Normal index out of bounds: " + normalIndex);
        }
        return new Vector3D(normalBuffer.get(normalIndex), normalBuffer.get(normalIndex + 1), normalBuffer.get(normalIndex + 2));
    }

    public void drawObject(int program, float[] mvpMatrix, float[] viewMatrix, float[] projectionMatrix) {
        int aPositionLocation = GLES20.glGetAttribLocation(program, "a_Position");
        int aNormalLocation = GLES20.glGetAttribLocation(program, "a_Normal");
        int aColorLocation = GLES20.glGetAttribLocation(program, "a_Color");
        int uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
        int uModelMatrixLocation = GLES20.glGetUniformLocation(program, "u_ModelMatrix");

        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, (float) position.x, (float) position.y, (float) position.z);
        Matrix.scaleM(modelMatrix, 0, (float) size, (float) size, (float) size);

        // Apply rotations
        Matrix.rotateM(modelMatrix, 0, rotationX, 1, 0, 0);
        Matrix.rotateM(modelMatrix, 0, rotationY, 0, 1, 0);
        Matrix.rotateM(modelMatrix, 0, rotationZ, 0, 0, 1);

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

        int aPositionLocation = GLES20.glGetAttribLocation(program, "a_Position");
        int aColorLocation = GLES20.glGetAttribLocation(program, "a_Color");
        int uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix");

        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glEnableVertexAttribArray(aColorLocation);

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

        float[] colors = color == Color.GREEN ? COLLISION_COLOR : DEFAULT_COLOR;

        FloatBuffer vertexBuffer = createFloatBuffer(vertices);
        FloatBuffer colorBuffer = createFloatBuffer(colors);

        short[] indices = new short[]{
                0, 1, 1, 2, 2, 3, 3, 0,
                4, 5, 5, 6, 6, 7, 7, 4,
                0, 4, 1, 5, 2, 6, 3, 7
        };

        ShortBuffer indexBuffer = createShortBuffer(indices);

        if (!drawBoundingVolume) return;
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(aColorLocation, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_LINES, indices.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aColorLocation);
    }

    public boolean detectCollision(ObjectBlenderModel other) {
        boolean collision = this.boundingVolume.intersects(other.boundingVolume);
        color = collision ? Color.BLUE : Color.GREEN;
        return collision;
    }

    public void handleCollision(ObjectBlenderModel other) {
        Vector3D normal = this.position.subtract(other.position).normalize();
        Vector3D relativeVelocity = this.velocity.subtract(other.velocity);
        double velAlongNormal = relativeVelocity.dot(normal);

        if (velAlongNormal > 0) return;

        double impulseScalar = -(1 + 1) * velAlongNormal;
        impulseScalar /= (1 / this.mass + 1 / other.mass);

        Vector3D impulse = normal.scale(impulseScalar);

        if (bouncesOff && other.bouncesOff) {
            this.velocity = this.velocity.add(impulse.scale(1 / this.mass));
            other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
        } else if (bouncesOff && !other.bouncesOff) {
            this.velocity = this.velocity.add(impulse.scale(1 / this.mass));
        } else if (!bouncesOff && other.bouncesOff) {
            other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
        }

        double overlap = (this.size + other.size) - this.position.subtract(other.position).magnitude();
        if (overlap > 0) {
            Vector3D separation = normal.scale(overlap / 2);
            this.position = this.position.add(separation);
            other.position = other.position.subtract(separation);
            this.updateBoundingVolume();
            other.updateBoundingVolume();
        }
    }

    public synchronized void updatePosition() {
        position = position.add(velocity);
        if (isTiltEnabled) {
            position = position.add(velocity);
        }
        updateBoundingVolume();
    }

    public synchronized void applyTilt(float[] tilt, float sensitivity) {
        if (isTiltEnabled) {
            velocity = velocity.add(new Vector3D(tilt[0], -tilt[1], 0).scale(sensitivity));
        }
        updatePosition();
    }

    public synchronized void applyGravity(ObjectBlenderModel other) {
        if (useConstantGravity) {
            if (followGravity)
                applyConstantGravity();
        } else {
            applyDynamicGravity(other);
        }
        updatePosition();
    }

    private void applyConstantGravity() {
        Vector3D constantForce = new Vector3D(0, -9.8, 0);
        Vector3D acceleration = constantForce.scale(1 / mass);
        velocity = velocity.add(acceleration);
    }

    private void applyDynamicGravity(ObjectBlenderModel other) {
        double G = gravityStrength;

        Vector3D distanceVector = other.position.subtract(position);
        double distance = distanceVector.magnitude();
        if (distance < 10) return;

        double force = G * mass * other.mass / (distance * distance);
        Vector3D forceVector = distanceVector.normalize().scale(force);

        if (isAttractedByOther && other.isAttractedByOther) {
            Vector3D acceleration = forceVector.scale(1 / mass);
            velocity = velocity.add(acceleration);
        }
        if (attractsOther && other.isAttractedByOther) {
            Vector3D accelerationOther = forceVector.scale(-1 / other.mass);
            other.velocity = other.velocity.add(accelerationOther);
        }
    }

    public synchronized void reset() {
        position = new Vector3D(initialPosition.x, initialPosition.y, initialPosition.z);
        velocity = new Vector3D(initialVelocity.x, initialVelocity.y, initialVelocity.z);
    }

    public void toggleConstantGravity() {
        useConstantGravity = !useConstantGravity;
    }
}