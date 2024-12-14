package com.notorein.planetarySystem3D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Color;
import android.opengl.GLES20;

public class ObjectPlane extends Object {

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;



    public ObjectPlane(int positionIndex, boolean isTiltEnabled, boolean followsGravity, boolean attractsOther, boolean isAttractedByOther,boolean bouncesOff, String name) {
        super(positionIndex, new Vector3D(0, 0, 0), new Vector3D(0, 0, 0), new Vector3D(0, 0, 0), 10, Color.GREEN, 0,  followsGravity, isTiltEnabled, attractsOther, isAttractedByOther,bouncesOff, name);
        generatePlaneGeometry();
    }

    private void generatePlaneGeometry() {
        float[] vertices = {
                -10000, 0, -10000,
                10000, 0, -10000,
                10000, 0,  10000,
                -10000, 0,  10000
        };

        short[] indices = {
                0, 1, 2,
                0, 2, 3
        };

        float[] colors = {
                0, 1, 0, 1,
                0, 1, 0, 1,
                0, 1, 0, 1,
                0, 1, 0, 1
        };

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        colorBuffer = cbb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);
    }

    @Override
    public void draw(int program, float[] mvpMatrix, float[] modelMatrix, float[] projectionMatrix,GLES20 gl) {
        int positionHandle = gl.glGetAttribLocation(program, "vPosition");
        gl.glEnableVertexAttribArray(positionHandle);
        gl.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        int colorHandle = gl.glGetAttribLocation(program, "aColor");
        gl.glEnableVertexAttribArray(colorHandle);
        gl.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        int mvpMatrixHandle = gl.glGetUniformLocation(program, "uMVPMatrix");
        gl.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        gl.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        gl.glDisableVertexAttribArray(positionHandle);
        gl.glDisableVertexAttribArray(colorHandle);
    }

//    @Override
//    public void draw(GL10 gl) {
//        gl.glPushMatrix();
//        gl.glTranslatef((float) getX(), (float) getY(), (float) getZ());
//
//        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
//        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
//
//        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
//        gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
//
//        gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_SHORT, indexBuffer);
//
//        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
//        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
//
//        gl.glPopMatrix();
//    }

}
