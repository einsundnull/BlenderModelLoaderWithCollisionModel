package com.notorein.planetarySystem3D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.opengles.GL10;

public class Cube {

    private float[] vertices;
    private float[] colors;
    private byte[] indices;

    private ByteBuffer vertexBuffer;
    private ByteBuffer colorBuffer;
    private ByteBuffer indexBuffer;

    private float size;
    private float posX, posY, posZ;

    public Cube(float size, float posX, float posY, float posZ) {
        this.size = size;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;

        vertices = new float[]{
                -size, -size, -size,
                size, -size, -size,
                size, size, -size,
                -size, size, -size,
                -size, -size, size,
                size, -size, size,
                size, size, size,
                -size, size, size,
        };

        colors = new float[]{
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 0.5f, 0.0f, 1.0f,
                1.0f, 0.5f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,
        };

        indices = new byte[]{
                0, 1, 2, 0, 2, 3,
                3, 2, 6, 3, 6, 7,
                7, 6, 5, 7, 5, 4,
                4, 5, 1, 4, 1, 0,
                1, 5, 6, 1, 6, 2,
                4, 0, 3, 4, 3, 7
        };

        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        vertexBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer.asFloatBuffer().put(vertices);
        vertexBuffer.position(0);

        colorBuffer = ByteBuffer.allocateDirect(colors.length * 4);
        colorBuffer.order(ByteOrder.nativeOrder());
        colorBuffer.asFloatBuffer().put(colors);
        colorBuffer.position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length);
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    public void draw(GL10 gl) {
        gl.glPushMatrix();
        gl.glTranslatef(posX, posY, posZ);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);

        gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, indexBuffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        gl.glPopMatrix();
    }
}
