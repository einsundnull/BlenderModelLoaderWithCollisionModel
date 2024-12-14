package com.notorein.planetarySystem3D;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ObjectCube extends Object {
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private FloatBuffer normalBuffer;
    private ShortBuffer indexBuffer;
    private int numIndices;


    public ObjectCube(int positionIndex, double x, double y, double z, double vx, double vy, double vz, double vxt, double vyt, double vzt, int mass, int color, int size, boolean followGravity, boolean isTiltEnabled, boolean attractsOther, boolean isAttractedByOther, boolean bouncesOff, String name) {
        super(positionIndex, new Vector3D(x, y, z), new Vector3D(vx, vy, vz), new Vector3D(vxt, vyt, vzt), mass, color, size, true, true, true, true, true, name);


        float[] vertices = {
                // Front face
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                // Back face
                -1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                // Top face
                -1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                // Bottom face
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                // Right face
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                // Left face
                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, -1.0f,
        };

        float[] colors = {
                // Green color for all vertices
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
        };

        float[] normals = {
                // Front face
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                // Back face
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                // Top face
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                // Bottom face
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                // Right face
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                // Left face
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
        };

        short[] indices = {
                0, 1, 2, 0, 2, 3, // Front face
                4, 5, 6, 4, 6, 7, // Back face
                8, 9, 10, 8, 10, 11, // Top face
                12, 13, 14, 12, 14, 15, // Bottom face
                16, 17, 18, 16, 18, 19, // Right face
                20, 21, 22, 20, 22, 23  // Left face
        };

        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices).position(0);

        colorBuffer = ByteBuffer.allocateDirect(colors.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.put(colors).position(0);

        normalBuffer = ByteBuffer.allocateDirect(normals.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalBuffer.put(normals).position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        indexBuffer.put(indices).position(0);

        numIndices = indices.length;
    }

    @Override
    public void draw(int program, float[] mvpMatrix, float[] modelMatrix, float[] projectionMatrix, GLES20 gl) {
        int aPositionLocation = gl.glGetAttribLocation(program, "a_Position");
        int aNormalLocation = gl.glGetAttribLocation(program, "a_Normal");
        int aColorLocation = gl.glGetAttribLocation(program, "a_Color");
        int uMVPMatrixLocation = gl.glGetUniformLocation(program, "u_MVPMatrix");
        int uModelMatrixLocation = gl.glGetUniformLocation(program, "u_ModelMatrix");

        // Set the model matrix
        Matrix.setIdentityM(modelMatrix, 0);

        Matrix.translateM(modelMatrix, 0, (float) getX(), (float) getY(), (float) getZ());

        // Bind the vertex buffer
        gl.glVertexAttribPointer(aPositionLocation, 3, gl.GL_FLOAT, false, 0, getVertexBuffer());
        gl.glVertexAttribPointer(aNormalLocation, 3, gl.GL_FLOAT, false, 0, getNormalBuffer());
        gl.glVertexAttribPointer(aColorLocation, 4, gl.GL_FLOAT, false, 0, getColorBuffer());

        // Set the MVP matrix
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0);
        gl.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
        gl.glUniformMatrix4fv(uModelMatrixLocation, 1, false, modelMatrix, 0);

        // Draw the cube
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


    public void draw(int program, float[] mvpMatrix, float[] modelMatrix, GLES20 gl) {
        int aPositionLocation = gl.glGetAttribLocation(program, "a_Position");
        int aNormalLocation = gl.glGetAttribLocation(program, "a_Normal");
        int aColorLocation = gl.glGetAttribLocation(program, "a_Color");
        int uMVPMatrixLocation = gl.glGetUniformLocation(program, "u_MVPMatrix");
        int uModelMatrixLocation = gl.glGetUniformLocation(program, "u_ModelMatrix");

        // Set the model matrix
        Matrix.setIdentityM(modelMatrix, 0);

        Matrix.translateM(modelMatrix, 0, (float) getX(), (float) getY(), (float) getZ());

        // Bind the vertex buffer
        gl.glVertexAttribPointer(aPositionLocation, 3, gl.GL_FLOAT, false, 0, getVertexBuffer());
        gl.glVertexAttribPointer(aNormalLocation, 3, gl.GL_FLOAT, false, 0, getNormalBuffer());
        gl.glVertexAttribPointer(aColorLocation, 4, gl.GL_FLOAT, false, 0, getColorBuffer());

        // Set the MVP matrix
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0);
        gl.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
        gl.glUniformMatrix4fv(uModelMatrixLocation, 1, false, modelMatrix, 0);

        // Draw the cube
        gl.glDrawElements(gl.GL_TRIANGLES, getNumIndices(), gl.GL_UNSIGNED_SHORT, getIndexBuffer());
    }
}
