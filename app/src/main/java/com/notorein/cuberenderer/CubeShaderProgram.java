package com.notorein.cuberenderer;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.FloatBuffer;

public class CubeShaderProgram {
    private static final String VERTEX_SHADER =
            "uniform mat4 uModelMatrix;" +
                    "uniform mat4 uRotationMatrix;" +
                    "attribute vec4 aPosition;" +
                    "attribute vec4 aColor;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "   gl_Position = uRotationMatrix * uModelMatrix * aPosition;" +
                    "   vColor = aColor;" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "   gl_FragColor = vColor;" +
                    "}";

    private final int mProgramId;
    private int uModelMatrixLocation;
    private int uRotationMatrixLocation;
    private int aPositionLocation;
    private int aColorLocation;

    public CubeShaderProgram() {
        mProgramId = GLES20.glCreateProgram();

        int vertexShaderId = compileShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShaderId = compileShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

        GLES20.glAttachShader(mProgramId, vertexShaderId);
        GLES20.glAttachShader(mProgramId, fragmentShaderId);
        GLES20.glLinkProgram(mProgramId);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(mProgramId, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            String errorMessage = GLES20.glGetProgramInfoLog(mProgramId);
            GLES20.glDeleteProgram(mProgramId);
            throw new RuntimeException("Error linking program: " + errorMessage);
        }

        uModelMatrixLocation = GLES20.glGetUniformLocation(mProgramId, "uModelMatrix");
        uRotationMatrixLocation = GLES20.glGetUniformLocation(mProgramId, "uRotationMatrix");
        aPositionLocation = GLES20.glGetAttribLocation(mProgramId, "aPosition");
        aColorLocation = GLES20.glGetAttribLocation(mProgramId, "aColor");
    }


    private int compileShader(int type, String shaderCode) {
        int shaderId = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shaderId, shaderCode);
        GLES20.glCompileShader(shaderId);

        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            String errorMessage = GLES20.glGetShaderInfoLog(shaderId);
            Log.e("ShaderError", "Error compiling shader: " + errorMessage);  // Add this log
            GLES20.glDeleteShader(shaderId);
            throw new RuntimeException("Error compiling shader: " + errorMessage);
        }

        return shaderId;
    }



    public int getProgramId() {
        return mProgramId;
    }

    public void setModelMatrix(float[] modelMatrix) {
        GLES20.glUniformMatrix4fv(uModelMatrixLocation, 1, false, modelMatrix, 0);
    }

    public void setRotationMatrix(float[] rotationMatrix) {
        GLES20.glUniformMatrix4fv(uRotationMatrixLocation, 1, false, rotationMatrix, 0);
    }

    public void setPositionAttribute(FloatBuffer vertices) {
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertices);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
    }

    public void setColorAttribute(FloatBuffer colors) {
        GLES20.glVertexAttribPointer(aColorLocation, 4, GLES20.GL_FLOAT, false, 0, colors);
        GLES20.glEnableVertexAttribArray(aColorLocation);
    }


}
