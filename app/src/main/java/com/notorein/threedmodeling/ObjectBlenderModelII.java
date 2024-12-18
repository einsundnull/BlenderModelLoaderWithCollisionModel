package com.notorein.threedmodeling;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

public class ObjectBlenderModelII {
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
//    public Vector3D velocityTilt;
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
    CollisionModelAABB boundingVolume;
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

//    public void logBoundingVolumeAndOpticalCenter() {
//        // Calculate the center of the bounding volume in local coordinates
//         localBoundingVolumeCenter = new Vector3D(
//                (boundingVolume.min.x + boundingVolume.max.x) / 2.0,
//                (boundingVolume.min.y + boundingVolume.max.y) / 2.0,
//                (boundingVolume.min.z + boundingVolume.max.z) / 2.0
//        );
//
//        // Convert the local center to world coordinates
////         worldBoundingVolumeCenter = localBoundingVolumeCenter.add(position);
//
//        // The position of the optical representation is the position of the object
////         opticalCenter = position;
//
//        // Log the positions
////        Log.i(TAG, "Bounding Volume Center (Local): " + localBoundingVolumeCenter);
////        Log.i(TAG, "Bounding Volume Center (World): " + name + " " + worldBoundingVolumeCenter);
////        Log.i(TAG, "Optical Representation Center: " + name + " " + opticalCenter);
////        Log.i(TAG, "Position: " + name + " " + position);
//    }


    public ObjectBlenderModelII(Context context, int positionIndex, Vector3D position, Vector3D velocity, Vector3D velocityTilt, double mass, int color, double size, boolean isTiltEnabled, boolean followGravity, boolean attractsOther, boolean isAttractedByOther, boolean bouncesOff, String name, String objFileName) {
        this.context = context;
        this.positionIndex = positionIndex;
        this.position = position;
        this.velocity = velocity;
        this.initialPosition = position;
        this.initialVelocity = velocity;
//        this.velocityTilt = velocityTilt;
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
//        logBoundingVolumeAndOpticalCenter();
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
        Vector3D min = new Vector3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3D max = new Vector3D(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

        for (int i = 0; i < numIndices; i += 3) {
            Vector3D v0 = getVertex(indexBuffer.get(i));
            Vector3D v1 = getVertex(indexBuffer.get(i + 1));
            Vector3D v2 = getVertex(indexBuffer.get(i + 2));

            min.x = Math.min(min.x, Math.min(Math.min(v0.x, v1.x), v2.x));
            min.y = Math.min(min.y, Math.min(Math.min(v0.y, v1.y), v2.y));
            min.z = Math.min(min.z, Math.min(Math.min(v0.z, v1.z), v2.z));
            max.x = Math.max(max.x, Math.max(Math.max(v0.x, v1.x), v2.x));
            max.y = Math.max(max.y, Math.max(Math.max(v0.y, v1.y), v2.y));
            max.z = Math.max(max.z, Math.max(Math.max(v0.z, v1.z), v2.z));
        }

        this.boundingVolume = new CollisionModelAABB(min, max);
    }

    private Vector3D getVertex(int index) {
        int vertexIndex = index * 3;
        return new Vector3D(vertexBuffer.get(vertexIndex), vertexBuffer.get(vertexIndex + 1), vertexBuffer.get(vertexIndex + 2));
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

        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(aNormalLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(aColorLocation, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(uModelMatrixLocation, 1, false, modelMatrix, 0);

        // This draws the actual object
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
    }

//    public void drawBoundingVolume(int program, float[] mvpMatrix, float[] viewMatrix, float[] projectionMatrix) {
//
//
//        int aPositionLocation = GLES20.glGetAttribLocation(program, "a_Position");
//        int aColorLocation = GLES20.glGetAttribLocation(program, "a_Color");
//        int uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
//
//        GLES20.glEnableVertexAttribArray(aPositionLocation);
//        GLES20.glEnableVertexAttribArray(aColorLocation);
//
//        float[] vertices = new float[]{
//                (float) boundingVolume.min.x, (float) boundingVolume.min.y, (float) boundingVolume.min.z,
//                (float) boundingVolume.max.x, (float) boundingVolume.min.y, (float) boundingVolume.min.z,
//                (float) boundingVolume.max.x, (float) boundingVolume.max.y, (float) boundingVolume.min.z,
//                (float) boundingVolume.min.x, (float) boundingVolume.max.y, (float) boundingVolume.min.z,
//                (float) boundingVolume.min.x, (float) boundingVolume.min.y, (float) boundingVolume.max.z,
//                (float) boundingVolume.max.x, (float) boundingVolume.min.y, (float) boundingVolume.max.z,
//                (float) boundingVolume.max.x, (float) boundingVolume.max.y, (float) boundingVolume.max.z,
//                (float) boundingVolume.min.x, (float) boundingVolume.max.y, (float) boundingVolume.max.z
//        };
//
//        float[] colors = color == Color.GREEN ? COLLISION_COLOR : DEFAULT_COLOR;
//
//        FloatBuffer vertexBuffer = createFloatBuffer(vertices);
//        FloatBuffer colorBuffer = createFloatBuffer(colors);
//
//        short[] indices = new short[]{
//                0, 1, 1, 2, 2, 3, 3, 0,
//                4, 5, 5, 6, 6, 7, 7, 4,
//                0, 4, 1, 5, 2, 6, 3, 7
//        };
//
//        ShortBuffer indexBuffer = createShortBuffer(indices);
//
//        if (!drawBoundingVolume) return;
//        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
//        GLES20.glVertexAttribPointer(aColorLocation, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
//
//        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
//
//        GLES20.glDrawElements(GLES20.GL_LINES, indices.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
//
//        GLES20.glDisableVertexAttribArray(aPositionLocation);
//        GLES20.glDisableVertexAttribArray(aColorLocation);
//    }

    public boolean detectCollision(ObjectBlenderModelII other) {
        boolean collision = this.boundingVolume.intersects(other.boundingVolume);
        color = collision ? Color.BLUE : Color.GREEN;
        return collision;
    }

//    void handleCollision(ObjectBlenderModel other) {
//        Vector3D normal = position.subtract(other.position).normalize();
//        Vector3D relativeVelocity = velocity.subtract(other.velocity);
//        double velAlongNormal = relativeVelocity.dot(normal);
//
////        if (velAlongNormal > 0) return;
//
//        double impulseScalar = -(1 + 1) * velAlongNormal;
//        impulseScalar /= (1 / mass + 1 / other.mass);
//
//
//        Vector3D impulse = normal.scale(impulseScalar);
//        color = Color.YELLOW;
//        velocity = velocity.add(impulse.scale(1 / mass));
//        other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
//        updatePosition();
//        other.updatePosition();
//    }
//public void handleCollision(ObjectBlenderModel other) {
//    // Calculate the normal vector
//
//    Vector3D normal = this.position.subtract(other.position).normalize();
//
//    // Calculate the relative velocity
//    Vector3D relativeVelocity = this.velocity.subtract(other.velocity);
//
//    // Calculate the velocity along the normal
//    double velAlongNormal = relativeVelocity.dot(normal);
//
//    // If the spheres are moving away from each other, do nothing
//    if (velAlongNormal > 0) return;
//
//    // Calculate the impulse scalar
//    double impulseScalar = -(1 + 1) * velAlongNormal; // 1 is the coefficient of restitution for elastic collision
//    impulseScalar /= (1 / this.mass + 1 / other.mass);
//
//    // Calculate the impulse vector
//    Vector3D impulse = normal.scale(impulseScalar);
//
//    // Apply the impulse to the velocities
//    if (bouncesOff && other.bouncesOff) {
//        // Both objects bounce off each other
//        this.velocity = this.velocity.add(impulse.scale(1 / this.mass));
//        other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
//    } else if (bouncesOff && !other.bouncesOff) {
//        // This object bounces off, but the other object does not
//        this.velocity = this.velocity.subtract(other.velocity).subtract(velocity);
//    } else if (!bouncesOff && other.bouncesOff) {
//        // This object does not bounce off, but the other object does
//        other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
//    }
//}

    public void handleCollision(ObjectBlenderModelII other) {
        // Calculate the normal vector
        Vector3D normal = this.position.subtract(other.position).normalize();

        // Calculate the relative velocity
        Vector3D relativeVelocity = this.velocity.subtract(other.velocity);

        // Calculate the velocity along the normal
        double velAlongNormal = relativeVelocity.dot(normal);

        // If the objects are moving away from each other, do nothing
        if (velAlongNormal > 0) return;

        // Calculate the impulse scalar
        double impulseScalar = -(1 + 1) * velAlongNormal; // 1 is the coefficient of restitution for elastic collision
        impulseScalar /= (1 / this.mass + 1 / other.mass);

        // Calculate the impulse vector
        Vector3D impulse = normal.scale(impulseScalar);

        // Apply the impulse to the velocities
        if (bouncesOff && other.bouncesOff) {
            // Both objects bounce off each other
            this.velocity = this.velocity.add(impulse.scale(1 / this.mass));
            other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
        } else if (bouncesOff && !other.bouncesOff) {
            // This object bounces off, but the other object does not
            this.velocity = this.velocity.add(impulse.scale(1 / this.mass));
        } else if (!bouncesOff && other.bouncesOff) {
            // This object does not bounce off, but the other object does
            other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
        }

        // Adjust positions to prevent clipping
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
//        logBoundingVolumeAndOpticalCenter(); // Log the centers after updating the position
    }

    public synchronized void applyTilt(float[] tilt, float sensitivity) {
        if (isTiltEnabled) {
            velocity = velocity.add(new Vector3D(tilt[0], -tilt[1], 0).scale(sensitivity));
        }
        updatePosition();
    }

    public synchronized void applyGravity(ObjectBlenderModelII other) {
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
//        velocity = velocity.add(acceleration);
    }

    private void applyDynamicGravity(ObjectBlenderModelII other) {
        double G = gravityStrength;

        Vector3D distanceVector = other.position.subtract(position);
        double distance = distanceVector.magnitude();
        if (distance < 10) return;

        double force = G * mass * other.mass / (distance * distance);
        Vector3D forceVector = distanceVector.normalize().scale(force);

        if (isAttractedByOther && other.isAttractedByOther) {
            Vector3D acceleration = forceVector.scale(1 / mass);
            velocity = velocity.add(acceleration);
//            velocityTilt = velocityTilt.add(acceleration);
        }
        if (attractsOther && other.isAttractedByOther) {
            Vector3D accelerationOther = forceVector.scale(-1 / other.mass);
            other.velocity = other.velocity.add(accelerationOther);
        }
    }


    public synchronized void reset() {
        position = new Vector3D(initialPosition.x, initialPosition.y, initialPosition.z);
        velocity = new Vector3D(initialVelocity.x, initialVelocity.y, initialVelocity.z);
//        velocityTilt = new Vector3D(initialVelocity.x, initialVelocity.y, initialVelocity.z);
    }

    public void toggleConstantGravity() {
        useConstantGravity = !useConstantGravity;
    }



}
