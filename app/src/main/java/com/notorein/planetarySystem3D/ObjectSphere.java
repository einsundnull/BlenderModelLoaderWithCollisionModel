package com.notorein.planetarySystem3D;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ObjectSphere extends Object {

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private FloatBuffer normalBuffer;
    private ShortBuffer indexBuffer;
    private int numIndices;

    private int stacks;
    private int slices;

    public ObjectSphere(Context context, int positionIndex, double posX, double posY, double posZ, double vx, double vy, double vz, double mass, int color, float size, boolean isTiltEnabled, boolean followsGravity, boolean attractsOther, boolean isAttractedByOther, boolean bouncesOff, String name, String objectFileName) {
        super(context, positionIndex, new Vector3D(posX, posY, posZ), new Vector3D(vx, vy, vz), new Vector3D(vx, vy, vz), mass, color, size,  isTiltEnabled, followsGravity, attractsOther, isAttractedByOther, bouncesOff, name ,objectFileName);

        this.stacks = 50;
        this.slices = 50;

        generateSphereGeometry(size, stacks, slices);
    }

    private void generateSphereGeometry(float size, int stacks, int slices) {
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
            float xy = (float) (size * Math.cos(stackAngle));
            float z = (float) (size * Math.sin(stackAngle));

            for (int j = 0; j <= slices; j++) {
                float sliceAngle = j * 2 * (float) Math.PI / slices;
                float x = (float) (xy * Math.cos(sliceAngle));
                float y = (float) (xy * Math.sin(sliceAngle));

                vertices[vertexIndex++] = x;
                vertices[vertexIndex++] = y;
                vertices[vertexIndex++] = z;

                normals[normalIndex++] = x / size;
                normals[normalIndex++] = y / size;
                normals[normalIndex++] = z / size;

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
        FloatBuffer textureBuffer = tbb.asFloatBuffer();
        textureBuffer.put(textures);
        textureBuffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);

        // Extract RGBA components from the color integer
        float r = ((getColor() >> 16) & 0xFF) / 255.0f;
        float g = ((getColor() >> 8) & 0xFF) / 255.0f;
        float b = (getColor() & 0xFF) / 255.0f;
        float a = ((getColor() >> 24) & 0xFF) / 255.0f;

        float[] colors = new float[(stacks + 1) * (slices + 1) * 4];
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

    @Override
    public void draw(int program, float[] mvpMatrix, float[] viewMatrix, float[] projectionMatrix, GLES20 gl) {
        int aPositionLocation = gl.glGetAttribLocation(program, "a_Position");
        int aNormalLocation = gl.glGetAttribLocation(program, "a_Normal");
        int aColorLocation = gl.glGetAttribLocation(program, "a_Color");
        int uMVPMatrixLocation = gl.glGetUniformLocation(program, "u_MVPMatrix");
        int uModelMatrixLocation = gl.glGetUniformLocation(program, "u_ModelMatrix");

        // Set the model matrix
        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, (float) getPosition().x, (float) getPosition().y, (float) getPosition().z);

        // Bind the vertex buffer
        gl.glVertexAttribPointer(aPositionLocation, 3, gl.GL_FLOAT, false, 0, getVertexBuffer());
        gl.glVertexAttribPointer(aNormalLocation, 3, gl.GL_FLOAT, false, 0, getNormalBuffer());
        gl.glVertexAttribPointer(aColorLocation, 4, gl.GL_FLOAT, false, 0, getColorBuffer());

        // Set the MVP matrix
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0);
        gl.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
        gl.glUniformMatrix4fv(uModelMatrixLocation, 1, false, modelMatrix, 0);

        // Draw the sphere
        gl.glDrawElements(gl.GL_TRIANGLES, getNumIndices(), gl.GL_UNSIGNED_SHORT, getIndexBuffer());
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
}
