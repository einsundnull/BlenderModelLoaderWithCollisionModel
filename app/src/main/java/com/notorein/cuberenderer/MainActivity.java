package com.notorein.cuberenderer;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends Activity {
    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setRenderer(new CubeRenderer());
        setContentView(glSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    class CubeRenderer implements Renderer {
        private float rotation;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();
            gl.glTranslatef(0.0f, 0.0f, -5.0f);
            gl.glRotatef(rotation, 1.0f, 1.0f, 1.0f);
            drawCube(gl);
            rotation += 1.0f;
        }

        private void drawCube(GL10 gl) {
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

            float[] vertices = {
                    -1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f,
                    1.0f,  1.0f, -1.0f,
                    -1.0f,  1.0f, -1.0f,
                    -1.0f, -1.0f,  1.0f,
                    1.0f, -1.0f,  1.0f,
                    1.0f,  1.0f,  1.0f,
                    -1.0f,  1.0f,  1.0f,
            };

            float[] colors = {
                    0.0f,  1.0f,  0.0f,  1.0f,
                    0.0f,  1.0f,  0.0f,  1.0f,
                    1.0f,  0.5f,  0.0f,  1.0f,
                    1.0f,  0.5f,  0.0f,  1.0f,
                    1.0f,  0.0f,  0.0f,  1.0f,
                    1.0f,  0.0f,  0.0f,  1.0f,
                    0.0f,  0.0f,  1.0f,  1.0f,
                    1.0f,  0.0f,  1.0f,  1.0f,
            };

            ByteBuffer vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
            vertexBuffer.order(ByteOrder.nativeOrder());
            vertexBuffer.asFloatBuffer().put(vertices);
            vertexBuffer.position(0);

            ByteBuffer colorBuffer = ByteBuffer.allocateDirect(colors.length * 4);
            colorBuffer.order(ByteOrder.nativeOrder());
            colorBuffer.asFloatBuffer().put(colors);
            colorBuffer.position(0);

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
            gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);

            byte[] indices = {
                    0, 1, 2, 0, 2, 3,
                    3, 2, 6, 3, 6, 7,
                    7, 6, 5, 7, 5, 4,
                    4, 5, 1, 4, 1, 0,
                    1, 5, 6, 1, 6, 2,
                    4, 0, 3, 4, 3, 7
            };

            ByteBuffer indexBuffer = ByteBuffer.allocateDirect(indices.length);
            indexBuffer.put(indices);
            indexBuffer.position(0);

            gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, indexBuffer);

            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        }
    }
}