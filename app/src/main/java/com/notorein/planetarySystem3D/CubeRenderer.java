package com.notorein.planetarySystem3D;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class CubeRenderer implements GLSurfaceView.Renderer {
    private final ArrayList<Sphere> spheres;
    private final MainActivity activityMain;
    private final Context context;
    private float cameraPosX, cameraPosY, cameraPosZ, scaleFactor, cameraAngleX, cameraAngleY;
private Grid gridXY,  gridXZ,  gridYZ;
    public CubeRenderer(Context context, MainActivity activityMain, ArrayList<Sphere> spheres , Grid gridXY, Grid gridXZ, Grid gridYZ) {
        this.spheres = spheres;
        this.context = context;
        this.activityMain = activityMain;
        this.gridXY = gridXY;
        this.gridXZ = gridXZ;
        this.gridYZ = gridYZ;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float) width / height, 0.01f, 100000.0f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();



        cameraPosX = activityMain.getCameraPosX();
        cameraPosY = activityMain.getCameraPosY();
        cameraPosZ = activityMain.getCameraPosZ();
        scaleFactor = activityMain.getScaleFactor();
        cameraAngleX = activityMain.getCameraAngleX();
        cameraAngleY = activityMain.getCameraAngleY();

        gl.glTranslatef(cameraPosX, cameraPosY, cameraPosZ * scaleFactor);
        gl.glRotatef(cameraAngleX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(cameraAngleY, 0.0f, 1.0f, 0.0f);

        if (!activityMain.isPause())
            updateSpheres();

        for (Sphere sphere : spheres) {
            sphere.draw(gl);
        }
//        gridXY.draw(gl);
//        gridXZ.draw(gl);
//        gridYZ.draw(gl);
    }

    private void updateSpheres() {

        for (Sphere sphere : spheres) {
            for (Sphere other : spheres) {
                if (sphere != other) {
                    sphere.applyGravity(other);
                }
            }
            sphere.updatePosition();
        }
    }


}
