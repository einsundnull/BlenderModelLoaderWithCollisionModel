package com.notorein.planetarySystem3D;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class ObjectBlenderModelHEAVY {
    private static final int TRIANGLE_THRESHOLD = 10;
    private final Context context;
    private final String objFileName;
    private final CollisionModelModelLoader collisionModelModelLoader;
    private String name;
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private FloatBuffer normalBuffer;
    private ShortBuffer indexBuffer;
    private int numIndices;
    private CollisionModelTriangle collisionModelTriangle;
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
    CollisionModelBVHNode bvhRoot;
    private boolean useConstantGravity = true;
    private List<CollisionModelTriangle> collisionModelTriangles;

    public ObjectBlenderModelHEAVY(Context context, int positionIndex, Vector3D position, Vector3D velocity, Vector3D velocityTilt, double mass, int color, double size, boolean isTiltEnabled, boolean followGravity, boolean attractsOther, boolean isAttractedByOther, boolean bouncesOff, String name, String objFileName) {
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
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    private void loadObjModel(Context context, String objFileName) {
        String objFilePath = "models/" + objFileName + ".obj";
        this.collisionModelTriangles = collisionModelModelLoader.loadTrianglesFromOBJ(context, objFileName);

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
            collisionModelTriangle = collisionModelTriangles.get(i);
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

        this.bvhRoot = buildBVH(collisionModelTriangles);
    }

    private CollisionModelBVHNode buildBVH(List<CollisionModelTriangle> collisionModelTriangles) {
        if (collisionModelTriangles.size() <= TRIANGLE_THRESHOLD) {
            CollisionModelAABB boundingVolume = calculateBoundingVolume(collisionModelTriangles);
            return new CollisionModelBVHNode(boundingVolume, collisionModelTriangles);
        }

        List<CollisionModelTriangle> leftCollisionModelTriangles = new ArrayList<>();
        List<CollisionModelTriangle> rightCollisionModelTriangles = new ArrayList<>();
        Vector3D centroid = calculateCentroid(collisionModelTriangles);

        for (CollisionModelTriangle collisionModelTriangle : collisionModelTriangles) {
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

        return node;
    }

    private CollisionModelAABB calculateBoundingVolume(List<CollisionModelTriangle> collisionModelTriangles) {
        Vector3D min = new Vector3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3D max = new Vector3D(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

        for (CollisionModelTriangle collisionModelTriangle : collisionModelTriangles) {
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

    private Vector3D calculateCentroid(List<CollisionModelTriangle> collisionModelTriangles) {
        Vector3D centroid = new Vector3D(0, 0, 0);
        for (CollisionModelTriangle collisionModelTriangle : collisionModelTriangles) {
            centroid = centroid.add(collisionModelTriangle.getCentroid());
        }
        centroid = centroid.scale(1.0f / collisionModelTriangles.size());
        return centroid;
    }

    public boolean detectCollision(ObjectBlenderModelHEAVY other) {
        return this.bvhRoot.detectCollision(other.bvhRoot);
    }

    public void handleCollision(ObjectBlenderModelHEAVY other) {
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

    public synchronized void applyGravity(ObjectBlenderModelHEAVY other) {
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

    private void applyDynamicGravity(ObjectBlenderModelHEAVY other) {
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

    public void draw(int program, float[] mvpMatrix, float[] viewMatrix, float[] projectionMatrix, GLES20 gl) {
        int aPositionLocation = gl.glGetAttribLocation(program, "a_Position");
        int aNormalLocation = gl.glGetAttribLocation(program, "a_Normal");
        int aColorLocation = gl.glGetAttribLocation(program, "a_Color");
        int uMVPMatrixLocation = gl.glGetUniformLocation(program, "u_MVPMatrix");
        int uModelMatrixLocation = gl.glGetUniformLocation(program, "u_ModelMatrix");

        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, (float) getPosition().x, (float) getPosition().y, (float) getPosition().z);

        Matrix.scaleM(modelMatrix, 0, (float) getSize(), (float) getSize(), (float) getSize());

        gl.glVertexAttribPointer(aPositionLocation, 3, gl.GL_FLOAT, false, 0, vertexBuffer);
        gl.glVertexAttribPointer(aNormalLocation, 3, gl.GL_FLOAT, false, 0, normalBuffer);
        gl.glVertexAttribPointer(aColorLocation, 4, gl.GL_FLOAT, false, 0, colorBuffer);

        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0);
        gl.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
        gl.glUniformMatrix4fv(uModelMatrixLocation, 1, false, modelMatrix, 0);

        gl.glDrawElements(gl.GL_TRIANGLES, numIndices, gl.GL_UNSIGNED_SHORT, indexBuffer);
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
        // Update triangle data and rebuild BVH tree
        updateTriangles();
        this.bvhRoot = buildBVH(collisionModelTriangles);

        // Log the closest edges
        logClosestEdges();
    }

    private void updateTriangles() {
        // Update the vertices of the collisionModelTriangles based on the object's transformation matrix
        for (CollisionModelTriangle collisionModelTriangle : collisionModelTriangles) {
            Vector3D[] vertices = collisionModelTriangle.getVertices();
            for (int i = 0; i < vertices.length; i++) {
                vertices[i] = vertices[i].scale(size).add(position);
            }
            collisionModelTriangle.setVertices(vertices);
        }
    }

    private void logClosestEdges() {
        if (collisionModelTriangles.isEmpty()) {
            return;
        }

        // Find the closest edges
        double minDistance = Double.MAX_VALUE;
        Vector3D closestEdgeStart = null;
        Vector3D closestEdgeEnd = null;

        for (CollisionModelTriangle triangle : collisionModelTriangles) {
            Vector3D[] vertices = triangle.getVertices();
            for (int i = 0; i < vertices.length; i++) {
                Vector3D start = vertices[i];
                Vector3D end = vertices[(i + 1) % vertices.length];
                double distance = start.distanceTo(end);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestEdgeStart = start;
                    closestEdgeEnd = end;
                }
            }
        }
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
