package com.notorein.cuberenderer;



import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CubeRenderer implements GLSurfaceView.Renderer {

    private final Cube mCube;
    private final float[] mRotationMatrix = new float[16];

    public CubeRenderer() {
        mCube = new Cube();
        Matrix.setIdentityM(mRotationMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Rotate the cube every frame
        Matrix.rotateM(mRotationMatrix, 0, 1f, 1f, 1f, 0f); // Rotate along the X, Y, and Z axes
        mCube.draw(mRotationMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 3.0f;
        final float far = 7.0f;

        Matrix.frustumM(mRotationMatrix, 0, left, right, bottom, top, near, far);
    }


}

