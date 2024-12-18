package com.notorein.threedmodeling;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

public class ObjectLoaderBlenderModel {


    private List<ObjectModelTriangle> collisionModelTriangles;
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private FloatBuffer normalBuffer;
    private ShortBuffer indexBuffer;
    private int numIndices;
    public float[] vertexArray,normalArray;
    public short[] indexArray;

    public void loadObjModel(Context context, String objFileName, int color, ObjectLoaderTriangle objectLoaderTriangle) {
        collisionModelTriangles = objectLoaderTriangle.loadTrianglesFromOBJ(context, objFileName);
        List<Vector3D> verticesObjModel = objectLoaderTriangle.getVertices();
        List<Vector3D> normalsObjModel = objectLoaderTriangle.getNormals();


        vertexArray = new float[verticesObjModel.size() * 3];
     normalArray = new float[normalsObjModel.size() * 3];
        indexArray = new short[collisionModelTriangles.size() * 3];

        for (int i = 0; i < verticesObjModel.size(); i++) {
            Vector3D vertex = verticesObjModel.get(i);
            vertexArray[i * 3] = (float) vertex.x;
            vertexArray[i * 3 + 1] = (float) vertex.y;
            vertexArray[i * 3 + 2] = (float) vertex.z;
        }

        for (int i = 0; i < normalsObjModel.size(); i++) {
            Vector3D normal = normalsObjModel.get(i);
            normalArray[i * 3] = (float) normal.x;
            normalArray[i * 3 + 1] = (float) normal.y;
            normalArray[i * 3 + 2] = (float) normal.z;
        }

        for (int i = 0; i < collisionModelTriangles.size(); i++) {
            ObjectModelTriangle collisionModelTriangle = collisionModelTriangles.get(i);
            indexArray[i * 3] = (short) verticesObjModel.indexOf(collisionModelTriangle.getVertices()[0]);
            indexArray[i * 3 + 1] = (short) verticesObjModel.indexOf(collisionModelTriangle.getVertices()[1]);
            indexArray[i * 3 + 2] = (short) verticesObjModel.indexOf(collisionModelTriangle.getVertices()[2]);
        }

        numIndices = indexArray.length;

        vertexBuffer = createFloatBuffer(vertexArray);
        normalBuffer = createFloatBuffer(normalArray);
        indexBuffer = createShortBuffer(indexArray);

        updateColorBuffer(color);

    }

    public void updateColorBuffer(int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        float[] colors = new float[vertexArray.length / 3 * 4];
        for (int i = 0; i < colors.length; i += 4) {
            colors[i] = r;
            colors[i + 1] = g;
            colors[i + 2] = b;
            colors[i + 3] = a;
        }


        colorBuffer = createFloatBuffer(colors);
    }

    public static FloatBuffer createFloatBuffer(float[] array) {
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = bb.asFloatBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }

    public static ShortBuffer createShortBuffer(short[] array) {
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 2);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer buffer = bb.asShortBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }

    public int getNumIndices() {
        return numIndices;
    }

    public void setNumIndices(int numIndices) {
        this.numIndices = numIndices;
    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public void setVertexBuffer(FloatBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    public FloatBuffer getNormalBuffer() {
        return normalBuffer;
    }

    public void setNormalBuffer(FloatBuffer normalBuffer) {
        this.normalBuffer = normalBuffer;
    }

    public ShortBuffer getIndexBuffer() {
        return indexBuffer;
    }

    public void setIndexBuffer(ShortBuffer indexBuffer) {
        this.indexBuffer = indexBuffer;
    }

    public FloatBuffer getColorBuffer() {
        return colorBuffer;
    }

    public void setColorBuffer(FloatBuffer colorBuffer) {
        this.colorBuffer = colorBuffer;
    }

    public List<ObjectModelTriangle> getObjectTriangles() {
        return collisionModelTriangles;
    }

    public void setCollisionModelTriangles(List<ObjectModelTriangle> collisionModelTriangles) {
        this.collisionModelTriangles = collisionModelTriangles;
    }
}
