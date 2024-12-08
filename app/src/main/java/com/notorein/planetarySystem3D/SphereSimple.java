package com.notorein.planetarySystem3D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class SphereSimple {

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ByteBuffer indexBuffer;

    private float radius;
    private float posX, posY, posZ;
    private int stacks, slices;

    public SphereSimple(float radius, float posX, float posY, float posZ, int stacks, int slices) {
        this.radius = radius;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.stacks = stacks;
        this.slices = slices;

        float[] vertices = new float[(stacks + 1) * (slices + 1) * 3];
        float[] colors = new float[(stacks + 1) * (slices + 1) * 4];
        byte[] indices = new byte[stacks * slices * 6];

        int vertexIndex = 0;
        int colorIndex = 0;
        int indexIndex = 0;

        for (int i = 0; i <= stacks; i++) {
            float stackAngle = (float) (Math.PI * (i / (float) stacks) - Math.PI / 2);
            float xy = (float) (radius * Math.cos(stackAngle));
            float z = (float) (radius * Math.sin(stackAngle));

            for (int j = 0; j <= slices; j++) {
                float sliceAngle = (float) (2 * Math.PI * j / slices);
                float x = (float) (xy * Math.cos(sliceAngle));
                float y = (float) (xy * Math.sin(sliceAngle));

                vertices[vertexIndex++] = x;
                vertices[vertexIndex++] = y;
                vertices[vertexIndex++] = z;

                colors[colorIndex++] = (float) (x / radius + 0.5);
                colors[colorIndex++] = (float) (y / radius + 0.5);
                colors[colorIndex++] = (float) (z / radius + 0.5);
                colors[colorIndex++] = 1.0f;
            }
        }

        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int first = (i * (slices + 1)) + j;
                int second = first + slices + 1;

                indices[indexIndex++] = (byte) first;
                indices[indexIndex++] = (byte) second;
                indices[indexIndex++] = (byte) (first + 1);

                indices[indexIndex++] = (byte) second;
                indices[indexIndex++] = (byte) (second + 1);
                indices[indexIndex++] = (byte) (first + 1);
            }
        }

        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        colorBuffer = ByteBuffer.allocateDirect(colors.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length).order(ByteOrder.nativeOrder());
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

        gl.glDrawElements(GL10.GL_TRIANGLES, indexBuffer.capacity(), GL10.GL_UNSIGNED_BYTE, indexBuffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        gl.glPopMatrix();
    }
}
