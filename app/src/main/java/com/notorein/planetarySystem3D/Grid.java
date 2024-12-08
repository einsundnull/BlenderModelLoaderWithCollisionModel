package com.notorein.planetarySystem3D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Grid {

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private int numLines;
    private float lineThickness;
    private float lineSpacing;
    private int color;
    private float centerX, centerY, centerZ;

    public Grid(int numLines, float lineThickness, float lineSpacing, int color, float centerX, float centerY, float centerZ, float v) {
        this.numLines = numLines;
        this.lineThickness = lineThickness;
        this.lineSpacing = lineSpacing;
        this.color = color;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        generateGrid();
    }

    private void generateGrid() {
        int totalLines = numLines * 4; // 4 directions: +X, -X, +Y, -Y
        float[] vertices = new float[totalLines * 2 * 3]; // 2 vertices per line, 3 coordinates per vertex
        float[] colors = new float[totalLines * 2 * 4]; // 2 vertices per line, 4 color components per vertex

        int index = 0;

        // Generate lines along the X-axis
        for (int i = 0; i < numLines; i++) {
            float x = i * lineSpacing - (numLines - 1) * lineSpacing / 2 + centerX;
            vertices[index++] = x;
            vertices[index++] = centerY;
            vertices[index++] = centerZ;
            vertices[index++] = x;
            vertices[index++] = centerY + 1.0f;
            vertices[index++] = centerZ;

            vertices[index++] = x;
            vertices[index++] = centerY;
            vertices[index++] = centerZ;
            vertices[index++] = x;
            vertices[index++] = centerY - 1.0f;
            vertices[index++] = centerZ;
        }

        // Generate lines along the Y-axis
        for (int i = 0; i < numLines; i++) {
            float y = i * lineSpacing - (numLines - 1) * lineSpacing / 2 + centerY;
            vertices[index++] = centerX;
            vertices[index++] = y;
            vertices[index++] = centerZ;
            vertices[index++] = centerX + 1.0f;
            vertices[index++] = y;
            vertices[index++] = centerZ;

            vertices[index++] = centerX;
            vertices[index++] = y;
            vertices[index++] = centerZ;
            vertices[index++] = centerX - 1.0f;
            vertices[index++] = y;
            vertices[index++] = centerZ;
        }

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        for (int i = 0; i < totalLines * 2; i++) {
            colors[i * 4] = r;
            colors[i * 4 + 1] = g;
            colors[i * 4 + 2] = b;
            colors[i * 4 + 3] = a;
        }

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        colorBuffer = cbb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);
    }

    public void draw(GL10 gl) {
        gl.glPushMatrix();
        gl.glLineWidth(lineThickness);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);

        gl.glDrawArrays(GL10.GL_LINES, 0, numLines * 4 * 2);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        gl.glPopMatrix();
    }

    public int getNumLines() {
        return numLines;
    }

    public void setNumLines(int numLines) {
        this.numLines = numLines;
        generateGrid();
    }

    public float getLineThickness() {
        return lineThickness;
    }

    public void setLineThickness(float lineThickness) {
        this.lineThickness = lineThickness;
    }

    public float getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(float lineSpacing) {
        this.lineSpacing = lineSpacing;
        generateGrid();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        generateGrid();
    }

    public float getCenterX() {
        return centerX;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
        generateGrid();
    }

    public float getCenterY() {
        return centerY;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
        generateGrid();
    }

    public float getCenterZ() {
        return centerZ;
    }

    public void setCenterZ(float centerZ) {
        this.centerZ = centerZ;
        generateGrid();
    }
}
