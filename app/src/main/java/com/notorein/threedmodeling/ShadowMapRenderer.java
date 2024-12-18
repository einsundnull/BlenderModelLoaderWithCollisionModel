package com.notorein.threedmodeling;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ShadowMapRenderer implements GLSurfaceView.Renderer {


    private final int height;
    private final int width;
    private int shadowMapFBO;
    private int shadowMapTexture;
    private int shadowProgram, sceneProgram;


    private final float[] lightProjectionMatrix = new float[16];
    private final float[] lightViewMatrix = new float[16];
    private final float[] lightSpaceMatrix = new float[16];

    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];

    private final List<ObjectBlenderModel> objects;
    private final MainActivity activityMain;
    private final Context context;
    private volatile float cameraPosX, cameraPosY, cameraPosZ, scaleFactor, cameraAngleX, cameraAngleY;

    private LightSource lightSource;

    private float cameraYaw, cameraPitch;
    private float eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ;

    public static String loadShader(Context context, String shaderFileName) {
        StringBuilder shaderSource = new StringBuilder();
        try {
            InputStream inputStream = context.getAssets().open(shaderFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return shaderSource.toString();
    }


    public ShadowMapRenderer(Context context, MainActivity activityMain, List<ObjectBlenderModel> objects, int screenWidth, int screenHeight) {
        this.objects = objects;
        this.context = context;
        this.activityMain = activityMain;
        this.width = screenWidth;
        this.height = screenHeight;
        this.lightSource = activityMain.getObjectLightSource();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        initShadowMap();

        String vertexShaderCode = loadShader(context, "shaders/vertex_shader.glsl");
        String shadowFragmentShaderCode = loadShader(context, "shaders/shadow_fragment_shader.glsl");
        String sceneFragmentShaderCode = loadShader(context, "shaders/scene_fragment_shader.glsl");

        shadowProgram = loadShaderProgram(vertexShaderCode, shadowFragmentShaderCode);
        sceneProgram = loadShaderProgram(vertexShaderCode, sceneFragmentShaderCode);

        // Enable lighting and set light properties
        activityMain.getObjectLightSource().enableLight(gl);

        // Set material properties
        activityMain.getObjectLightSource().setMaterialProperties(gl);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, this.width, this.height);

        // Set up the projection matrix
        float ratio = (float) width / height;
        Matrix.perspectiveM(projectionMatrix, 0, 45, ratio, 0.1f, 1000000.0f);

        // Apply camera transformations using matrix operations
        Matrix.setLookAtM(viewMatrix, 0, cameraPosX, cameraPosY, cameraPosZ, 0, 0, 0, 0, 1, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // Rotate the view matrix
        Matrix.rotateM(viewMatrix, 0, cameraAngleX, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(viewMatrix, 0, cameraAngleY, 0.0f, 1.0f, 0.0f);
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (activityMain) {
            // Apply the pitch and yaw adjustments
            Matrix.rotateM(viewMatrix, 0, cameraPitch, 1, 0, 0);
            Matrix.rotateM(viewMatrix, 0, cameraYaw, 0, 1, 0);
            cameraPosX = activityMain.getCameraPosX();
            cameraPosY = activityMain.getCameraPosY();
            cameraPosZ = activityMain.getCameraPosZ();
            scaleFactor = activityMain.getScaleFactor();
            cameraAngleX = activityMain.getCameraAngleX();
            cameraAngleY = activityMain.getCameraAngleY();

            // Apply camera transformations using matrix operations
            Matrix.setLookAtM(viewMatrix, 0, cameraPosX, cameraPosY, cameraPosZ, 0, 0, 0, 0, 1, 0);
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

            // Rotate the view matrix
            Matrix.rotateM(viewMatrix, 0, cameraAngleX, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(viewMatrix, 0, cameraAngleY, 0.0f, 1.0f, 0.0f);

            // Enable the light source
            lightSource.enableLight(gl);

            if (!activityMain.isPause()) {
                updateObjects();
            }
        }

        renderShadowMap();
        renderScene();
    }


    private void updateObjects() {
        synchronized (objects) {
            // Broad phase collision detection
            for (int i = 0; i < objects.size(); i++) {
                ObjectBlenderModel object = objects.get(i);
                for (int j = i + 1; j < objects.size(); j++) {
                    ObjectBlenderModel other = objects.get(j);
                    object.applyGravity(other);
                    if (object.boundingVolume.intersects(other.boundingVolume)) {
                        object.handleCollision(other);

                    }
                }
            }
            // Update positions for all objects
            for (ObjectBlenderModel object : objects) {
                object.updatePosition();
            }
        }
    }
//    private void updateObjects() {
//        synchronized (objects) {
//            // Broad phase collision detection
//            List<Pair<ObjectBlenderModel, ObjectBlenderModel>> potentialCollisions = broadPhaseCollisionDetection();
//            // Narrow phase collision detection and response
//            for (Pair<ObjectBlenderModel, ObjectBlenderModel> pair : potentialCollisions) {
//                ObjectBlenderModel object = pair.first;
//                ObjectBlenderModel other = pair.second;
//                object.applyGravity(other);
//                if (object.detectCollision(other)) {
//                    object.handleCollision(other);
//                }
//            }
//            // Update positions for all objects
//            for (ObjectBlenderModel object : objects) {
//                object.updatePosition();
//            }
//        }
//    }

    private List<Pair<ObjectBlenderModel, ObjectBlenderModel>> broadPhaseCollisionDetection() {
        List<Pair<ObjectBlenderModel, ObjectBlenderModel>> potentialCollisions = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            ObjectBlenderModel object = objects.get(i);
            for (int j = i + 1; j < objects.size(); j++) {
                ObjectBlenderModel other = objects.get(j);
                if (object.boundingVolume.intersects(other.boundingVolume)) {
//                    potentialCollisions.add(new Pair<>(object, other));
                    object.applyGravity(other);
                }
            }
        }
        return potentialCollisions;
    }

    private void initShadowMap() {
        int[] fbo = new int[1];
        int[] texture = new int[1];

        GLES20.glGenFramebuffers(1, fbo, 0);
        shadowMapFBO = fbo[0];

        GLES20.glGenTextures(1, texture, 0);
        shadowMapTexture = texture[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowMapTexture);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, width, height, 0,
                GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, shadowMapFBO);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_TEXTURE_2D, shadowMapTexture, 0);

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete");
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public void renderShadowMap() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, shadowMapFBO);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(shadowProgram);

        Matrix.setLookAtM(lightViewMatrix, 0, 0, 10, 0, 0, 0, 0, 0, 1, 0);
        Matrix.orthoM(lightProjectionMatrix, 0, -10, 10, -10, 10, 0.1f, 50f);
        Matrix.multiplyMM(lightSpaceMatrix, 0, lightProjectionMatrix, 0, lightViewMatrix, 0);

        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(shadowProgram, "u_LightSpaceMatrix"), 1, false, lightSpaceMatrix, 0);

        drawScene(shadowProgram, lightSpaceMatrix, viewMatrix, projectionMatrix);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public void renderScene() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(sceneProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowMapTexture);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(sceneProgram, "u_ShadowMap"), 0);

        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(sceneProgram, "u_LightSpaceMatrix"), 1, false, lightSpaceMatrix, 0);

        drawScene(sceneProgram, mvpMatrix, viewMatrix, projectionMatrix);
    }


    private void drawScene(int program, float[] mvpMatrix, float[] viewMatrix, float[] projectionMatrix) {
        // Bind attributes, uniforms, and draw geometry
        synchronized (objects) {
            for (ObjectBlenderModel object : objects) {
                // Get attribute and uniform locations from .glsl files in the main/assets/shaders folder
                int aPositionLocation = GLES20.glGetAttribLocation(program, "a_Position");
                int aNormalLocation = GLES20.glGetAttribLocation(program, "a_Normal");
                int aColorLocation = GLES20.glGetAttribLocation(program, "a_Color");
                int uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
                int uModelMatrixLocation = GLES20.glGetUniformLocation(program, "u_ModelMatrix");

                GLES20.glEnableVertexAttribArray(aPositionLocation);
                GLES20.glEnableVertexAttribArray(aNormalLocation);
                GLES20.glEnableVertexAttribArray(aColorLocation);

                // Draw all objects
                object.drawObject(program, mvpMatrix, viewMatrix, projectionMatrix);
                // Draw all bounding volumes
                if (ObjectBlenderModel.drawBoundingVolume) {
                    object.drawBoundingVolume(program, mvpMatrix, viewMatrix, projectionMatrix);
                }

                GLES20.glDisableVertexAttribArray(aPositionLocation);
                GLES20.glDisableVertexAttribArray(aNormalLocation);
                GLES20.glDisableVertexAttribArray(aColorLocation);
            }
        }
    }


    private int loadShaderProgram(String vertexShaderCode, String fragmentShaderCode) {
        int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            throw new RuntimeException("Error creating program: " + GLES20.glGetProgramInfoLog(program));
        }

        return program;
    }

    private int compileShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            throw new RuntimeException("Error compiling shaders: " + GLES20.glGetShaderInfoLog(shader));
        }

        return shader;
    }



    public synchronized void speedUpSimulation() {
        synchronized (objects) {
            for (ObjectBlenderModel objectSphere : objects) {
                objectSphere.speedUp();
            }
        }
    }

    public synchronized void speedDownSimulation() {
        synchronized (objects) {
            for (ObjectBlenderModel objectSphere : objects) {
                objectSphere.speedDown();
            }
        }
    }

    public synchronized void resetAnimation() {
        synchronized (objects) {
            for (ObjectBlenderModel object : objects) {
                object.reset();
            }
        }
    }
}
