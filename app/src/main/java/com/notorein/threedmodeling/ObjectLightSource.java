package com.notorein.threedmodeling;

import javax.microedition.khronos.opengles.GL10;

public class ObjectLightSource {
    private float[] lightAmbient;
    private float[] lightDiffuse;
    private float[] lightSpecular;
    private float[] lightPosition;

    public ObjectLightSource(float[] lightAmbient, float[] lightDiffuse, float[] lightSpecular, float[] lightPosition) {
        this.lightAmbient = lightAmbient;
        this.lightDiffuse = lightDiffuse;
        this.lightSpecular = lightSpecular;
        this.lightPosition = lightPosition;
    }

    public void enableLight(GL10 gl) {
        gl.glEnable(GL10.GL_LIGHTING);
        gl.glEnable(GL10.GL_LIGHT0);

        // Enable smooth shading
        gl.glShadeModel(GL10.GL_SMOOTH);

        // Adjusted light properties for a more diffuse effect
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, new float[]{0.8f, 0.8f, 0.8f, 1.0f}, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, new float[]{0.5f, 0.5f, 0.5f, 1.0f}, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);

        // Enable additional light sources if needed
        // gl.glEnable(GL10.GL_LIGHT1);
        // gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);
        // gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, new float[]{0.8f, 0.8f, 0.8f, 1.0f}, 0);
        // gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_SPECULAR, new float[]{0.5f, 0.5f, 0.5f, 1.0f}, 0);
        // gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, new float[]{1.0f, 1.0f, 1.0f, 0.0f}, 0);
    }

    public void setMaterialProperties(GL10 gl) {
        float[] materialAmbient = {0.2f, 0.2f, 0.2f, 1.0f}; // Softer ambient light
        float[] materialDiffuse = {0.8f, 0.8f, 0.8f, 1.0f}; // Softer diffuse light
        float[] materialSpecular = {0.5f, 0.5f, 0.5f, 1.0f}; // Softer specular light
        float materialShininess = 30.0f; // Less shiny

        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, materialAmbient, 0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, materialDiffuse, 0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, materialSpecular, 0);
        gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, materialShininess);
    }
}