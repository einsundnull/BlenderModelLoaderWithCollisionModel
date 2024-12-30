package com.notorein.threedmodeling;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Pair;

import com.notorein.threedmodeling.utils.Vector3D;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    private ObjectLightSource objectLightSource;

    private float cameraYaw, cameraPitch;

    private float strafeX = 0;
    private float strafeY = 0;
    private float strafeZ = 0;
    private static final float STRAFE_SPEED = 0.1f;
    private List<Pair<ObjectBlenderModel, ObjectBlenderModel>> potentialCollisions;
    private final ExecutorService shaderLoaderExecutor;
    private final ExecutorService collisionDetectionExecutor;

    public ShadowMapRenderer(Context context, MainActivity activityMain, List<ObjectBlenderModel> objects, int screenWidth, int screenHeight) {
        this.objects = objects;
        this.context = context;
        this.activityMain = activityMain;
        this.width = screenWidth;
        this.height = screenHeight;
        this.objectLightSource = activityMain.getObjectLightSource();
        this.shaderLoaderExecutor = Executors.newSingleThreadExecutor();
        this.collisionDetectionExecutor = Executors.newFixedThreadPool(4);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        initShadowMap();

        Future<String> vertexShaderFuture = shaderLoaderExecutor.submit(() -> loadShader(context, "shaders/vertex_shader.glsl"));
        Future<String> shadowFragmentShaderFuture = shaderLoaderExecutor.submit(() -> loadShader(context, "shaders/shadow_fragment_shader.glsl"));
        Future<String> sceneFragmentShaderFuture = shaderLoaderExecutor.submit(() -> loadShader(context, "shaders/scene_fragment_shader.glsl"));

        try {
            String vertexShaderCode = vertexShaderFuture.get();
            String shadowFragmentShaderCode = shadowFragmentShaderFuture.get();
            String sceneFragmentShaderCode = sceneFragmentShaderFuture.get();

            shadowProgram = loadShaderProgram(vertexShaderCode, shadowFragmentShaderCode);
            sceneProgram = loadShaderProgram(vertexShaderCode, sceneFragmentShaderCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        activityMain.getObjectLightSource().enableLight(gl);
        activityMain.getObjectLightSource().setMaterialProperties(gl);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, this.width, this.height);

        float ratio = (float) width / height;
        Matrix.perspectiveM(projectionMatrix, 0, 45, ratio, 0.1f, 1000000.0f);

        Matrix.setLookAtM(viewMatrix, 0, cameraPosX, cameraPosY, cameraPosZ, 0, 0, 0, 0, 1, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Matrix.rotateM(viewMatrix, 0, cameraAngleX, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(viewMatrix, 0, cameraAngleY, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (activityMain) {
            Matrix.rotateM(viewMatrix, 0, cameraPitch, 1, 0, 0);
            Matrix.rotateM(viewMatrix, 0, cameraYaw, 0, 1, 0);
            cameraPosX = activityMain.getCameraPosX();
            cameraPosY = activityMain.getCameraPosY();
            cameraPosZ = activityMain.getCameraPosZ();

            cameraPosX += strafeX * STRAFE_SPEED;
            cameraPosY += strafeY * STRAFE_SPEED;
            cameraPosZ += strafeZ * STRAFE_SPEED;

            scaleFactor = activityMain.getScaleFactor();
            cameraAngleX = activityMain.getCameraAngleX();
            cameraAngleY = activityMain.getCameraAngleY();

            Matrix.setLookAtM(viewMatrix, 0, cameraPosX, cameraPosY, cameraPosZ, 0, 0, 0, 0, 1, 0);
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

            Matrix.rotateM(viewMatrix, 0, cameraAngleX, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(viewMatrix, 0, cameraAngleY, 0.0f, 1.0f, 0.0f);

            objectLightSource.enableLight(gl);

            if (!activityMain.isPause()) {
                updateObjects();
            }
        }

        renderShadowMap();
        renderScene();
    }

    public void setStrafeDirection(float x, float y, float z) {
        strafeX = x;
        strafeY = y;
        strafeZ = z;
    }

    private void updateObjects() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (ObjectBlenderModel object : objects) {
            executor.submit(() -> {
                object.updatePosition();
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (objects) {
            potentialCollisions = broadPhaseCollisionDetection();

            synchronized (potentialCollisions) {
                for (Pair<ObjectBlenderModel, ObjectBlenderModel> pair : potentialCollisions) {
                    ObjectBlenderModel object = pair.first;
                    ObjectBlenderModel other = pair.second;

                    double distance = object.position.subtract(other.position).magnitude();
                    double size = object.size + other.size;

                    if (distance <= size * 1.2) {
                        synchronized (object) {
                            synchronized (other) {
                                object.color = Color.GREEN;
                                object.updateColorBuffer(object.color);
                                other.color = Color.GREEN;
                                other.updateColorBuffer(other.color);
                                object.handleCollision(other);
                            }
                        }
                    } else {
                        synchronized (object) {
                            synchronized (other) {
                                object.color = object.colorInitial;
                                object.updateColorBuffer(object.colorInitial);
                                other.color = other.colorInitial;
                                other.updateColorBuffer(other.colorInitial);
                            }
                        }
                    }
                }
            }
        }
    }

    private List<Pair<ObjectBlenderModel, ObjectBlenderModel>> broadPhaseCollisionDetection() {
        List<Pair<ObjectBlenderModel, ObjectBlenderModel>> potentialCollisions = new ArrayList<>();
        synchronized (objects) {
            List<Future<Void>> futures = new ArrayList<>();
            for (ObjectBlenderModel object : objects) {
                futures.add(collisionDetectionExecutor.submit(() -> {
                    for (ObjectBlenderModel other : objects) {
                        if (object != other) {
                            object.applyGravity(other);
                            Vector3D worldBoundingVolumeCenter1 = object.boundingVolume.min.add(object.boundingVolume.max).scale(0.5).add(object.position);
                            Vector3D worldBoundingVolumeCenter2 = other.boundingVolume.min.add(other.boundingVolume.max).scale(0.5).add(other.position);

                            double distance = worldBoundingVolumeCenter1.subtract(worldBoundingVolumeCenter2).magnitude();

                            if (distance < (object.size + other.size) * 1.5) {
                                synchronized (potentialCollisions) {
                                    potentialCollisions.add(new Pair<>(object, other));
                                }
                            }
                        }
                    }
                    return null;
                }));
            }

            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return potentialCollisions;
        }
    }

    private void initShadowMap() {
        int[] fbo = new int[1];
        int[] texture = new int[1];

        GLES20.glGenFramebuffers(1, fbo, 0);
        shadowMapFBO = fbo[0];

        GLES20.glGenTextures(1, texture, 0);
        shadowMapTexture = texture[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowMapTexture);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, width, height, 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, shadowMapFBO);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, shadowMapTexture, 0);

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
        synchronized (objects) {
            for (ObjectBlenderModel object : objects) {
                if (isInFrustum(object.position, (float) object.size)) {
                    int aPositionLocation = GLES20.glGetAttribLocation(program, "a_Position");
                    int aNormalLocation = GLES20.glGetAttribLocation(program, "a_Normal");
                    int aColorLocation = GLES20.glGetAttribLocation(program, "a_Color");
                    int uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
                    int uModelMatrixLocation = GLES20.glGetUniformLocation(program, "u_ModelMatrix");

                    GLES20.glEnableVertexAttribArray(aPositionLocation);
                    GLES20.glEnableVertexAttribArray(aNormalLocation);
                    GLES20.glEnableVertexAttribArray(aColorLocation);

                    object.drawObject(program, mvpMatrix, viewMatrix, projectionMatrix);
                    if(object.drawBoundingVolume) {
                        object.drawBoundingVolume(program, mvpMatrix, viewMatrix, projectionMatrix);

                    }
                    GLES20.glDisableVertexAttribArray(aPositionLocation);
                    GLES20.glDisableVertexAttribArray(aNormalLocation);
                    GLES20.glDisableVertexAttribArray(aColorLocation);
                }
            }
        }
    }

    private boolean isInFrustum(Vector3D position, float radius) {
        // Extract the frustum planes from the view-projection matrix
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Plane[] frustumPlanes = new Plane[6];
        frustumPlanes[0] = extractPlane(mvpMatrix, 3, 0, 0); // Left
        frustumPlanes[1] = extractPlane(mvpMatrix, 3, 1, 1); // Right
        frustumPlanes[2] = extractPlane(mvpMatrix, 3, 2, 2); // Bottom
        frustumPlanes[3] = extractPlane(mvpMatrix, 3, 3, 3); // Top
        frustumPlanes[4] = extractPlane(mvpMatrix, 2, 3, 2); // Near
        frustumPlanes[5] = extractPlane(mvpMatrix, 3, 2, 3); // Far

        // Check if the object's bounding sphere is within the frustum
        for (Plane plane : frustumPlanes) {
            if (plane.getDistanceToPoint(position) < -radius) {
                return false; // Object is outside the frustum
            }
        }
        return true; // Object is inside the frustum
    }

    private Plane extractPlane(float[] mvpMatrix, int row, int col1, int col2) {
        Vector3D normal = new Vector3D(
                mvpMatrix[col1] - mvpMatrix[row],
                mvpMatrix[col1 + 4] - mvpMatrix[row + 4],
                mvpMatrix[col1 + 8] - mvpMatrix[row + 8]
        );
        float distance = -(mvpMatrix[col2] - mvpMatrix[row + 12]);
        return new Plane(normal, distance);
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

    public synchronized void resetAnimation() {
        synchronized (objects) {
            for (ObjectBlenderModel object : objects) {
                object.reset();
            }
        }
    }

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
}
