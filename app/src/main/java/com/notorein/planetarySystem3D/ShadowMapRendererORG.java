package com.notorein.planetarySystem3D;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ShadowMapRendererORG implements GLSurfaceView.Renderer {
    private  final String VERTEX_SHADER_CODE =
            "#version 300 es\n" +
                    "precision highp float;\n" +
                    "layout(location = 0) in vec3 a_Position;\n" +
                    "layout(location = 1) in vec3 a_Normal;\n" +
                    "layout(location = 2) in vec4 a_Color;\n" +
                    "uniform mat4 u_MVPMatrix;\n" +
                    "uniform mat4 u_ModelMatrix;\n" +
                    "uniform vec3 u_LightPos;\n" +
                    "uniform mat4 u_LightSpaceMatrix;\n" +
                    "out vec3 v_Normal;\n" +
                    "out vec3 v_LightDir;\n" +
                    "out vec3 v_FragPos;\n" +
                    "out vec4 v_Color;\n" +
                    "out vec4 v_ShadowSpacePos;\n" +
                    "void main() {\n" +
                    "    vec4 worldPos = u_ModelMatrix * vec4(a_Position, 1.0);\n" +
                    "    gl_Position = u_MVPMatrix * vec4(a_Position, 1.0);\n" +
                    "    v_Normal = mat3(u_ModelMatrix) * a_Normal;\n" +
                    "    v_LightDir = u_LightPos - worldPos.xyz;\n" +
                    "    v_FragPos = worldPos.xyz;\n" +
                    "    v_Color = a_Color;\n" +
                    "    v_ShadowSpacePos = u_LightSpaceMatrix * vec4(v_FragPos, 1.0);\n" +
                    "}\n";

    private  final String SHADOW_FRAGMENT_SHADER_CODE =
            "#version 300 es\n" +
                    "precision highp float;\n" +
                    "in vec4 v_ShadowSpacePos;\n" + // Use the output variable from the vertex shaders
                    "uniform sampler2D u_ShadowMap;\n" +
                    "out float fragColor;\n" +
                    "void main() {\n" +
                    "    vec4 shadowSpacePos = v_ShadowSpacePos;\n" + // Use a temporary variable
                    "    shadowSpacePos.z += 0.0005; // Add depth bias\n" +
                    "    float shadow = texture(u_ShadowMap, shadowSpacePos.xy).r;\n" +
                    "    fragColor = shadow;\n" +
                    "}\n";

    private  final String SCENE_FRAGMENT_SHADER_CODE =
            "#version 300 es\n" +
                    "precision highp float;\n" +
                    "in vec3 v_LightDir;\n" +
                    "in vec3 v_FragPos;\n" +
                    "in vec3 v_Normal;\n" +
                    "in vec4 v_Color;\n" +
                    "in vec4 v_ShadowSpacePos;\n" + // Use the output variable from the vertex shaders
                    "uniform vec3 u_LightPos;\n" +
                    "uniform sampler2D u_ShadowMap;\n" +
                    "out vec4 fragColor;\n" +
                    "void main() {\n" +
                    "    vec3 lightDir = normalize(u_LightPos - v_FragPos);\n" +
                    "    float diff = max(dot(v_Normal, lightDir), 0.0);\n" +
                    "    vec4 color = vec4(diff, diff, diff, 1.0) * v_Color;\n" +
                    "    vec4 shadowSpacePos = v_ShadowSpacePos;\n" + // Use a temporary variable
                    "    shadowSpacePos.z += 0.0005; // Add depth bias\n" +
                    "    float shadow = texture(u_ShadowMap, shadowSpacePos.xy).r;\n" +
                    "    fragColor = color * shadow;\n" +
                    "}\n";

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

    private final ArrayList<ObjectBlenderModel> objects;
    private final MainActivity activityMain;
    private final Context context;
    private volatile float cameraPosX, cameraPosY, cameraPosZ, scaleFactor, cameraAngleX, cameraAngleY;

    private LightSource lightSource;

    private float cameraYaw, cameraPitch;
    private float eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ;

    public ShadowMapRendererORG(Context context, MainActivity activityMain, ArrayList<ObjectBlenderModel> objects, int screenWidth, int screenHeight) {
        this.objects = objects;
        this.context = context;
        this.activityMain = activityMain;
        this.width = screenWidth;
        this.height = screenHeight;
        // Initialize the light source
        this.lightSource = activityMain.getObjectLightSource();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        initShadowMap();
        shadowProgram = loadShaderProgram(VERTEX_SHADER_CODE, SHADOW_FRAGMENT_SHADER_CODE);
        sceneProgram = loadShaderProgram(VERTEX_SHADER_CODE, SCENE_FRAGMENT_SHADER_CODE);

        // Enable lighting and set light properties
        lightSource.enableLight(gl);

        // Set material properties
        lightSource.setMaterialProperties(gl);
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
        int aPositionLocation = GLES20.glGetAttribLocation(program, "a_Position");
        int aNormalLocation = GLES20.glGetAttribLocation(program, "a_Normal");
        int aColorLocation = GLES20.glGetAttribLocation(program, "a_Color");
        int uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
        int uModelMatrixLocation = GLES20.glGetUniformLocation(program, "u_ModelMatrix");

        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glEnableVertexAttribArray(aNormalLocation);
        GLES20.glEnableVertexAttribArray(aColorLocation);

        // Draw all objects
        synchronized (objects) {
            for (ObjectBlenderModel object : objects) {
                Log.i(TAG, "Drawing object: " + object.getName());
                object.drawObject(program, mvpMatrix, viewMatrix, projectionMatrix);

                if (ObjectBlenderModel.drawBoundingVolume) {
                    Log.i(TAG, "Drawing bounding volume for object: " + object.getName());
                    object.drawBoundingVolume(program, mvpMatrix, viewMatrix, projectionMatrix);
                }
            }
        }

        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aNormalLocation);
        GLES20.glDisableVertexAttribArray(aColorLocation);
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



    private void updateObjects() {
        synchronized (objects) {
            // Broad phase collision detection
            List<Pair<ObjectBlenderModel, ObjectBlenderModel>> potentialCollisions = broadPhaseCollisionDetection();

            // Narrow phase collision detection and response
            for (Pair<ObjectBlenderModel, ObjectBlenderModel> pair : potentialCollisions) {
                ObjectBlenderModel object = pair.first;
                ObjectBlenderModel other = pair.second;
                object.applyGravity(other);
                if (object.detectCollision(other)) {
                    object.handleCollision(other);
                }
            }

            // Update positions
            for (ObjectBlenderModel object : objects) {
                object.updatePosition();
            }
        }
    }





    private List<Pair<ObjectBlenderModel, ObjectBlenderModel>> broadPhaseCollisionDetection() {
        List<Pair<ObjectBlenderModel, ObjectBlenderModel>> potentialCollisions = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            for (int j = i + 1; j < objects.size(); j++) {
                ObjectBlenderModel object = objects.get(i);
                ObjectBlenderModel other = objects.get(j);
                if (object.boundingVolume.intersects(other.boundingVolume)) {
                    potentialCollisions.add(new Pair<>(object, other));
                }
            }
        }
        return potentialCollisions;
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
