package com.notorein.threedmodeling;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static com.notorein.threedmodeling.ObjectLoaderBlenderModel.createFloatBuffer;
import static com.notorein.threedmodeling.ObjectLoaderBlenderModel.createShortBuffer;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class ObjectBlenderModel {
    private final Context context;
    private final String objFileName;
    private final ObjectLoaderTriangle objectLoaderTriangle;
    private final ObjectLoaderBlenderModel objectLoader;
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
    private List<ObjectModelTriangle> objectTriangles;

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
        objectLoaderTriangle = new ObjectLoaderTriangle();
        objectLoader = new ObjectLoaderBlenderModel();
        objectLoader.loadObjModel(context, objFileName, color, objectLoaderTriangle);
        objectTriangles = objectLoader.getObjectTriangles();
        numIndices = objectLoader.getNumIndices();
        vertexBuffer = objectLoader.getVertexBuffer();
        normalBuffer = objectLoader.getNormalBuffer();
        indexBuffer = objectLoader.getIndexBuffer();
        colorBuffer = objectLoader.getColorBuffer();

        updateBoundingVolume();
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

        // Calculate the center of the bounding volume
        Vector3D center = new Vector3D(
                (min.x + max.x) / 2,
                (min.y + max.y) / 2,
                (min.z + max.z) / 2
        );

        // Log the center coordinates
        Log.i(TAG, "updateBoundingVolume: " + getName() + " Bounding Volume Center: (" + center.x + ", " + center.y + ", " + center.z + ")");
//        Log.i(TAG, "updateBoundingVolume: " + getName() + " Bounding Volume: " + boundingVolume);
    }



    public int getPositionIndex() {
        return positionIndex;
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

    public void enableBoundingVolumeDrawing() {
        drawBoundingVolume = true;
    }

    public void disableBoundingVolumeDrawing() {
        drawBoundingVolume = false;
    }

    public boolean detectCollision(ObjectBlenderModel other) {
        boolean collision = this.boundingVolume.intersects(other.boundingVolume);
//        Log.i(TAG, "detectCollision: " + getName() + " with " + other.getName() + " collision: " + collision);
        color = collision ? Color.BLUE : Color.GREEN;
        return collision;
    }

    void handleCollision(ObjectBlenderModel other) {
        Vector3D normal = position.subtract(other.position).normalize();
        Vector3D relativeVelocity = velocity.subtract(other.velocity);
        double velAlongNormal = relativeVelocity.dot(normal);

//        if (velAlongNormal > 0) return;

        double impulseScalar = -(1 + 1) * velAlongNormal;
//        impulseScalar /= (1 / mass + 1 / other.mass);


        Vector3D impulse = normal.scale(impulseScalar);
        color = Color.YELLOW;
        velocity = velocity.add(impulse.scale(1 / mass));
        other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
        updatePosition();
//        Log.i(TAG, "handleCollision: " + getName() + " with " + other.getName() + " impulse: " + impulse);
    }



//    void handleCollision(ObjectBlenderModel other) {
//        Vector3D normal = position.subtract(other.position).normalize();
//        Vector3D relativeVelocity = velocity.subtract(other.velocity);
//        double velAlongNormal = relativeVelocity.dot(normal);
//
//        if (velAlongNormal > 0) return;
//
//        double impulseScalar = -(1 + 1) * velAlongNormal;
//        impulseScalar /= (1 / mass + 1 / other.mass);
//
//        Vector3D impulse = normal.scale(impulseScalar);
//
////        if (bouncesOff && other.bouncesOff) {
//            velocity = velocity.add(impulse.scale(1 / mass));
//            other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
////        } else if (bouncesOff && !other.bouncesOff) {
////            velocity = velocity.add(impulse.scale(1 / mass));
////        } else if (!bouncesOff && other.bouncesOff) {
////            other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
////        }
//
//        // Update positions and velocities
//        updatePosition();
//        other.updatePosition();
//    }

    public synchronized void updatePosition() {
        position = position.add(velocity);
        if (isTiltEnabled) {
            position = position.add(velocityTilt);
        }
        updateBoundingVolume();
//        Log.i(TAG, "updatePosition: " + getName() + " Position: " + position + " Velocity: " + velocity + " Bounding Volume: " + boundingVolume);
    }
    public synchronized void applyTilt(float[] tilt, float sensitivity) {
        if (isTiltEnabled) {
            velocityTilt = velocityTilt.add(new Vector3D(tilt[0], -tilt[1], 0).scale(sensitivity));
        }
        updatePosition();
    }

    public synchronized void applyGravity(ObjectBlenderModel other) {
        if (useConstantGravity) {
            if (followGravity) applyConstantGravity();
        } else {
            applyDynamicGravity(other);
        }
        updatePosition();
    }

    private void applyConstantGravity() {
        Vector3D constantForce = new Vector3D(0, -9.8, 0);
        Vector3D acceleration = constantForce.scale(1 / mass);
        velocity = velocity.add(acceleration);
        velocityTilt = velocityTilt.add(acceleration);
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
            velocityTilt = velocityTilt.add(acceleration);
        }
        if (attractsOther && other.isAttractedByOther) {
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



    public double getGravityStrength() {
        return gravityStrength;
    }

    public void setGravityStrength(double gravityStrength) {
        this.gravityStrength = gravityStrength;
    }

    public synchronized void speedUp() {
        velocity = velocity.scale(1.1);
    }

    public synchronized void speedDown() {
        velocity = velocity.scale(0.9);
    }

    public synchronized void speedUpTilt() {
        velocityTilt = velocityTilt.scale(1.1);
    }

    public synchronized void speedDownTilt() {
        velocityTilt = velocityTilt.scale(0.9);
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
        return mass <= 0 ? 1 : mass;
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

    public boolean isBouncesOff() {
        return bouncesOff;
    }

    public void setBouncesOff(boolean bouncesOff) {
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
