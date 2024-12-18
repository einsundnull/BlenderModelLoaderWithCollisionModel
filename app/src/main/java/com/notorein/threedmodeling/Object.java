package com.notorein.threedmodeling;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class Object {
    private static final int TRIANGLE_THRESHOLD = 10;
    private final Context context;
    private final String objFileName;
    private ObjectTriangleLoader objectTriangleLoader;
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

    private double gravityStrength = 100;
    CollisionModelBVHNode bvhRoot;

    private boolean useConstantGravity = true;

    public Object(Context context,int positionIndex, Vector3D position, Vector3D velocity, Vector3D velocityTilt, double mass, int color, double size, boolean isTiltEnabled, boolean followGravity, boolean attractsOther, boolean isAttractedByOther, boolean bouncesOff, String name,  String objFileName) {

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
        objectTriangleLoader = new ObjectTriangleLoader();
        this.bvhRoot = buildBVH(objectTriangleLoader.loadTrianglesFromOBJ(context, objFileName));
    }

    CollisionModelBVHNode buildBVH(List<ObjectModelTriangle> collisionModelTriangles) {
        Log.i(TAG, name + "   Building BVH with " + collisionModelTriangles.size() + " collisionModelTriangles");
        if (collisionModelTriangles.size() <= TRIANGLE_THRESHOLD) {
            CollisionModelAABB boundingVolume = calculateBoundingVolume(collisionModelTriangles);
            Log.i(TAG, name + "   Creating leaf node with " + collisionModelTriangles.size() + " collisionModelTriangles");
            return new CollisionModelBVHNode(boundingVolume, collisionModelTriangles);
        }

        List<ObjectModelTriangle> leftCollisionModelTriangles = new ArrayList<>();
        List<ObjectModelTriangle> rightCollisionModelTriangles = new ArrayList<>();
        Vector3D centroid = calculateCentroid(collisionModelTriangles);

        for (ObjectModelTriangle collisionModelTriangle : collisionModelTriangles) {
            if (collisionModelTriangle.getCentroid().x < centroid.x) {
                leftCollisionModelTriangles.add(collisionModelTriangle);
            } else {
                rightCollisionModelTriangles.add(collisionModelTriangle);
            }
        }

        CollisionModelBVHNode leftChild = buildBVH(leftCollisionModelTriangles);
        CollisionModelBVHNode rightChild = buildBVH(rightCollisionModelTriangles);

        CollisionModelAABB boundingVolume = calculateBoundingVolume(collisionModelTriangles);
        CollisionModelBVHNode node = new CollisionModelBVHNode(boundingVolume, collisionModelTriangles);
        node.addChild(leftChild);
        node.addChild(rightChild);

        Log.i(TAG, "Created BVH node with " + collisionModelTriangles.size() + " collisionModelTriangles and " + leftCollisionModelTriangles.size() + " and " + rightCollisionModelTriangles.size() + " children");
        return node;
    }

    private CollisionModelAABB calculateBoundingVolume(List<ObjectModelTriangle> collisionModelTriangles) {
        Vector3D min = new Vector3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3D max = new Vector3D(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

        for (ObjectModelTriangle collisionModelTriangle : collisionModelTriangles) {
            for (Vector3D vertex : collisionModelTriangle.getVertices()) {
                min.x = Math.min(min.x, vertex.x);
                min.y = Math.min(min.y, vertex.y);
                min.z = Math.min(min.z, vertex.z);
                max.x = Math.max(max.x, vertex.x);
                max.y = Math.max(max.y, vertex.y);
                max.z = Math.max(max.z, vertex.z);
            }
        }

        return new CollisionModelAABB(min, max);
    }

    private Vector3D calculateCentroid(List<ObjectModelTriangle> collisionModelTriangles) {
        Vector3D centroid = new Vector3D(0, 0, 0);
        for (ObjectModelTriangle collisionModelTriangle : collisionModelTriangles) {
            centroid = centroid.add(collisionModelTriangle.getCentroid());
        }
        centroid = centroid.scale(1.0f / collisionModelTriangles.size());
        return centroid;
    }

    public boolean detectCollision(Object other) {
        return this.bvhRoot.detectCollision(other.bvhRoot);
    }

    public void handleCollision(Object other) {
        Vector3D normal = this.position.subtract(other.position).normalize();
        Vector3D relativeVelocity = this.velocity.subtract(other.velocity);
        double velAlongNormal = relativeVelocity.dot(normal);

        if (velAlongNormal > 0) return;

        double impulseScalar = -(1 + 1) * velAlongNormal;
        impulseScalar /= (1 / this.mass + 1 / other.mass);

        Vector3D impulse = normal.scale(impulseScalar);

        if (isBouncesOff() && other.isBouncesOff()) {
            this.velocity = this.velocity.add(impulse.scale(1 / this.mass));
            other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
        } else if (isBouncesOff() && !other.isBouncesOff()) {
            this.velocity = this.velocity.add(impulse.scale(1 / this.mass));
        } else if (!isBouncesOff() && other.isBouncesOff()) {
            other.velocity = other.velocity.subtract(impulse.scale(1 / other.mass));
        }
    }

    public synchronized void applyGravity(Object other) {
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

    private void applyDynamicGravity(Object other) {
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

    public abstract void draw(int program, float[] mvpMatrix, float[] modelMatrix, float[] projectionMatrix, GLES20 gl);

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
        position = position.add(velocity);
        if (isTiltEnabled()) {
            position = position.add(velocityTilt);
        }
        // Update triangle data and rebuild BVH tree
//        updateTriangles();
        this.bvhRoot = buildBVH( updateTriangles());
    }

    private   List<ObjectModelTriangle> updateTriangles() {
        // Update the vertices of the collisionModelTriangles based on the object's transformation matrix
        List<ObjectModelTriangle> collisionModelTriangles = objectTriangleLoader.loadTrianglesFromOBJ(context, objFileName);
        for (ObjectModelTriangle collisionModelTriangle : collisionModelTriangles) {
            Vector3D[] vertices = collisionModelTriangle.getVertices();
            for (int i = 0; i < vertices.length; i++) {
                vertices[i] = vertices[i].add(position);
            }
            collisionModelTriangle.setVertices(vertices);
        }
        return collisionModelTriangles;
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

    public void toggleConstantGravity() {
        useConstantGravity = !useConstantGravity;
    }
}
