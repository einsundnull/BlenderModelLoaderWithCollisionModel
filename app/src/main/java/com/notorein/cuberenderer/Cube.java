package com.notorein.cuberenderer;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Cube {

    private final CubeShaderProgram mShaderProgram;
    private final float[] mModelMatrix = new float[16];
    private final FloatBuffer mVertexBuffer;
    private final FloatBuffer mColorBuffer;

    private static final float[] VERTICES = {
            // Front face
            -1.0f, -1.0f,  1.0f,
            1.0f, -1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,

            // Back face
            -1.0f, -1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            1.0f,  1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,

            // Left face
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f, -1.0f,

            // Right face
            1.0f, -1.0f, -1.0f,
            1.0f,  1.0f, -1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f, -1.0f,  1.0f,

            // Top face
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f, -1.0f,

            // Bottom face
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f
    };

    private static final float[] COLORS = {
            // Front face
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,

            // Back face
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,

            // Left face
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,

            // Right face
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,

            // Top face
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,

            // Bottom face
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f
    };

    public Cube() {
        mShaderProgram = new CubeShaderProgram();
        Matrix.setIdentityM(mModelMatrix, 0);

        mVertexBuffer = ByteBuffer.allocateDirect(VERTICES.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTICES);
        mVertexBuffer.position(0);

        mColorBuffer = ByteBuffer.allocateDirect(COLORS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(COLORS);
        mColorBuffer.position(0);
    }

    public void draw(float[] rotationMatrix) {
        GLES20.glUseProgram(mShaderProgram.getProgramId());
        mShaderProgram.setModelMatrix(mModelMatrix);
        mShaderProgram.setRotationMatrix(rotationMatrix);
        mShaderProgram.setPositionAttribute(mVertexBuffer);
        mShaderProgram.setColorAttribute(mColorBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTICES.length / 3);
    }

}
