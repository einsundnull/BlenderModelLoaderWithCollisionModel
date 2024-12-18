package com.notorein.threedmodeling;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private GLSurfaceView glSurfaceView;
    private ShadowMapRenderer shadowMapRenderer;
    private GestureDetectorCompat gestureDetector;

    private Context context;
    private ListView dialog_celestial_body_chooser_list_view;
    private DialogListViewAdapterSpheresChooser spheresAdapter;
    private ImageView menuButton, buttonRemove, buttonAdd, buttonReset, buttonSpeedUp, buttonPlay, buttonSpeedDown, button_follow_unfollow, buttonToggleTilt, buttonUseConstantGravity;
    private DialogCelestialSphereChooser dialogCelestialSphereChooser;
    private DialogCelestialSphereValues dialogCelestialSphereValues;
    private DialogPositionOnScreen dialogPositionOnScreen;
    private TextView tvPositionXOnScreen, tvPositionYOnScreen, tvPositionZOnScreen;
    private int screenWidth, screenHeight;
    private float cameraSensitivity = 1f; // Reduced sensitivity
    private float cameraAngleX = 0, cameraAngleY = 0;
    private float cameraPosX = 0, cameraPosY = 0, cameraPosZ = -10.0f;
    private float scaleFactor = 100.0f;
    private boolean isScaling;
    public static boolean pause = true;
    private ScaleGestureDetector scaleGestureDetector;
    private List<ObjectBlenderModel> objects;
    private int indexOfSelectedSphereToFollow = 3;


    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean setTilt = false;
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private float tiltSensitivity = 1f; // Default tilt sensitivity
    private ObjectLightSource objectLightSource;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        this.context = this;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        // Set the layout
        setContentView(R.layout.activity_main);

        // Initialize components
        initObjects();
        initDialogs();
        initViews();
        initLightSource();
        initGLSurfaceView();
        initOnClickListeners();
        setCoordinatesText(0, 0, 0);
        buttonPlay.setImageLevel(pause ? 1 : 0);

        spheresAdapter = new DialogListViewAdapterSpheresChooser(context, objects, this);
        dialog_celestial_body_chooser_list_view.setAdapter(spheresAdapter);

        // Initialize the ScaleGestureDetector
        setSliderAndDetector();
    }


    private void initLightSource() {
        float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
        float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] lightPosition = {0.0f, 0.0f, 5.0f, 1.0f};
        this.objectLightSource = new ObjectLightSource(lightAmbient, lightDiffuse, lightSpecular, lightPosition);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initGLSurfaceView() {
        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                if (e2.getPointerCount() == 1) {
                    // Handle single-finger scroll events for strafing and turning
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        // Horizontal movement: turn the camera left/right
                        cameraAngleY -= (distanceX * cameraSensitivity); // Yaw
                    } else {
                        // Vertical movement: strafe up/down
                        cameraAngleX += (distanceY * cameraSensitivity);
                    }
                }
//                else if (e2.getPointerCount() == 2) {
//                    if(!isScaling) {
//                        // Handle two-finger scroll events for pitch and yaw
//                        cameraAngleX -= (distanceY * cameraSensitivity); // Yaw
//                        cameraAngleY -= (distanceX * cameraSensitivity); // Pitch
//                    }
//                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                ObjectBlenderModel.drawBoundingVolume = !ObjectBlenderModel.drawBoundingVolume;
                return true;
            }
        });

        glSurfaceView = findViewById(R.id.glSurfaceView);

        // Important rendering configurations
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.setPreserveEGLContextOnPause(true);

        // Create renderer
        shadowMapRenderer = new ShadowMapRenderer(context, this, objects, screenWidth, screenHeight);
        glSurfaceView.setRenderer(shadowMapRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        // Gesture detector setup

        // Touch event handling for GLSurfaceView
        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                scaleGestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }



    @SuppressLint("ClickableViewAccessibility")
    private void setSliderAndDetector() {
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private float previousSpanY;

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float currentSpanY = detector.getCurrentSpanY();
                float deltaY = currentSpanY - previousSpanY;

                if (Math.abs(deltaY) > Math.abs(detector.getCurrentSpanX() - detector.getPreviousSpanX())) {
                    // Vertical movement detected
                    if (deltaY > 0) {
                        // Both fingers moving down
                        cameraPosY -= (deltaY * cameraSensitivity);
                    } else {
                        // Both fingers moving up
                        cameraPosY += (Math.abs(deltaY) * cameraSensitivity);
                    }
                }

                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.0001f, Math.min(scaleFactor, 5000.0f));
                cameraPosZ = -10.0f * scaleFactor;
                isScaling = true;

                previousSpanY = currentSpanY;
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                previousSpanY = detector.getCurrentSpanY();
                isScaling = true;
                return super.onScaleBegin(detector);
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isScaling = false;
                super.onScaleEnd(detector);
            }
        });

        // Initialize the SensorManager and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }




    private void initViews() {
        dialog_celestial_body_chooser_list_view = findViewById(R.id.dialog_celestial_body_chooser_list_view);
        button_follow_unfollow = findViewById(R.id.button_follow_unfollow);
        menuButton = findViewById(R.id.button_menu);
        buttonAdd = findViewById(R.id.button_add);
        buttonRemove = findViewById(R.id.button_remove);
        buttonReset = findViewById(R.id.button_reset);
        buttonSpeedUp = findViewById(R.id.button_speed_up);
        buttonPlay = findViewById(R.id.button_play);
        buttonSpeedDown = findViewById(R.id.button_speed_down);
        buttonToggleTilt = findViewById(R.id.button_toggle_tilt);
        buttonUseConstantGravity = findViewById(R.id.button_use_constant_gravity);
        tvPositionXOnScreen = findViewById(R.id.tv_position_x_on_screen);
        tvPositionYOnScreen = findViewById(R.id.tv_position_y_on_screen);
        tvPositionZOnScreen = findViewById(R.id.tv_position_z_on_screen);
    }

    private void initOnClickListeners() {
        menuButton.setOnClickListener(v -> {
            if (!pause) {
                togglePause();
                buttonPlay.setImageLevel(pause ? 1 : 0);
            }
            UIClass.animateClick(menuButton);
            getDialogCelestialSphereChooser().setVisible(true);
            findViewById(R.id.seekBar_camera_yaw).setVisibility(View.INVISIBLE);
            findViewById(R.id.slider_camera_pitch).setVisibility(View.INVISIBLE);
        });

        buttonAdd.setOnClickListener(v -> {
            UIClass.animateClick(buttonAdd);
            togglePause();
            buttonPlay.setImageLevel(pause ? 1 : 0);
            dialogCelestialSphereChooser.setVisible(true);
        });

        buttonRemove.setOnClickListener(v -> {
            UIClass.animateClick(buttonRemove);
            togglePause();
            buttonPlay.setImageLevel(pause ? 1 : 0);
            dialogCelestialSphereChooser.setVisible(true);
        });

        buttonReset.setOnClickListener(v -> {
            UIClass.animateClick(buttonReset);
            shadowMapRenderer.resetAnimation();
        });

        buttonSpeedUp.setOnClickListener(v -> {
            UIClass.animateClick(buttonSpeedUp);
//            shadowMapRenderer.speedUpSimulation();
        });

        buttonSpeedDown.setOnClickListener(v -> {
            UIClass.animateClick(buttonSpeedDown);
//            shadowMapRenderer.speedDownSimulation();
        });

        buttonPlay.setOnClickListener(v -> {
            UIClass.animateClick(buttonPlay);
            togglePause();
            buttonPlay.setImageLevel(pause ? 1 : 0);
        });

        button_follow_unfollow.setOnClickListener(v -> {
            UIClass.animateClick(button_follow_unfollow);
        });

        buttonToggleTilt.setOnClickListener(v -> {
            UIClass.animateClick(buttonToggleTilt);
            boolean isTiltEnabled = !setTilt;
            toggleTilt(isTiltEnabled);
            // Update UI to reflect the current state
        });

        buttonUseConstantGravity.setOnClickListener(v -> {
            UIClass.animateClick(buttonUseConstantGravity);
            for (ObjectBlenderModel object : objects) {
                object.toggleConstantGravity();
            }
        });
    }

    public List<ObjectBlenderModel> initObjects() {
        objects = new ArrayList<>();
        objects.add(new ObjectBlenderModel(context, 1, new Vector3D(0, -400, 800), new Vector3D(0, 0, 0), new Vector3D(0, 0, 0), 1000, Color.BLUE, 100, true, false, true, true, true, "Plane", "cyl"));
        objects.add(new ObjectBlenderModel(context, 3, new Vector3D(-600, 0, 800), new Vector3D(0, 0, 0), new Vector3D(0, 0, 0), ObjectInitData.Sun.MASS, Color.MAGENTA, 100, false, false, true, true, true, "CHECK", "cyl"));
        objects.add(new ObjectBlenderModel(context, 2, new Vector3D(600, 0, 800), new Vector3D(0, 0, 0), new Vector3D(0, 0, 0), ObjectInitData.Sun.MASS, Color.RED, 100, false, false, true, true, true, "CUP", "cyl"));
        objects.add(new ObjectBlenderModel(context, 3, new Vector3D(-600, 800, 800), new Vector3D(0, 0, 0), new Vector3D(0, 0, 0), ObjectInitData.Sun.MASS, Color.MAGENTA, 100, false, false, true, true, true, "CHECK", "cyl"));
        objects.add(new ObjectBlenderModel(context, 2, new Vector3D(600, 0, -800), new Vector3D(0, 0, 0), new Vector3D(0, 0, 0), ObjectInitData.Sun.MASS, Color.RED, 100, false, false, true, true, true, "CUP", "cyl"));

        return objects;
    }


    private void initDialogs() {
        dialogCelestialSphereChooser = new DialogCelestialSphereChooser(this, this);
        dialogCelestialSphereValues = new DialogCelestialSphereValues(this, this);
        dialogPositionOnScreen = new DialogPositionOnScreen(this, this);
    }

    public void setCoordinatesText(double x, double y, double z) {
        tvPositionXOnScreen.setText("x: " + x + screenWidth / 2);
        tvPositionYOnScreen.setText("y: " + y + screenHeight / 2);
        tvPositionZOnScreen.setText("z: " + z);
    }

    public void setButtonsVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;

        findViewById(R.id.button_menu).setVisibility(visibility);
        findViewById(R.id.button_add).setVisibility(visibility);
        findViewById(R.id.button_remove).setVisibility(visibility);
        findViewById(R.id.button_reset).setVisibility(visibility);
        findViewById(R.id.button_speed_up).setVisibility(visibility);
        findViewById(R.id.button_play).setVisibility(visibility);
        findViewById(R.id.button_speed_down).setVisibility(visibility);
    }


    public DialogCelestialSphereValues getDialogCelestialSphereValues() {
        return dialogCelestialSphereValues;
    }

    public void setDialogCelestialSphereValues(DialogCelestialSphereValues dialogCelestialSphereValues) {
        this.dialogCelestialSphereValues = dialogCelestialSphereValues;
    }

    public DialogPositionOnScreen getDialogPositionOnScreen() {
        return dialogPositionOnScreen;
    }

    public void setDialogPositionOnScreen(DialogPositionOnScreen dialogPositionOnScreen) {
        this.dialogPositionOnScreen = dialogPositionOnScreen;
    }

    public DialogCelestialSphereChooser getDialogCelestialSphereChooser() {
        return dialogCelestialSphereChooser;
    }

    public void setDialogCelestialSphereChooser(DialogCelestialSphereChooser dialogCelestialSphereChooser) {
        this.dialogCelestialSphereChooser = dialogCelestialSphereChooser;
    }

    public float getCameraAngleX() {
        return cameraAngleX;
    }

    public void setCameraAngleX(float cameraAngleX) {
        this.cameraAngleX = cameraAngleX;
    }

    public float getCameraAngleY() {
        return cameraAngleY;
    }

    public void setCameraAngleY(float cameraAngleY) {
        this.cameraAngleY = cameraAngleY;
    }

    public float getCameraPosX() {
        return cameraPosX;
    }

    public void setCameraPosX(float cameraPosX) {
        this.cameraPosX = cameraPosX;
    }

    public float getCameraPosY() {
        return cameraPosY;
    }

    public void setCameraPosY(float cameraPosY) {
        this.cameraPosY = cameraPosY;
    }

    public float getCameraPosZ() {
        return cameraPosZ;
    }

    public void setCameraPosZ(float cameraPosZ) {
        this.cameraPosZ = cameraPosZ;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public boolean togglePause() {
        pause = !pause;
        return pause;
    }

    public List<ObjectBlenderModel> getSpheres() {
        return objects;
    }

    public void setIndexOfSelectedSphereToFollow(int indexOfSelectedSphereToFollow) {
        this.indexOfSelectedSphereToFollow = indexOfSelectedSphereToFollow;
    }

    @Override
    public void onBackPressed() {
        if (getDialogCelestialSphereChooser().isVisible()) {
            getDialogCelestialSphereChooser().setVisible(false);
            findViewById(R.id.slider_camera_pitch).setVisibility(View.VISIBLE);
            findViewById(R.id.seekBar_camera_yaw).setVisibility(View.VISIBLE);
        } else if (getDialogCelestialSphereValues().isVisible()) {
            getDialogCelestialSphereChooser().setVisible(false);
            getDialogCelestialSphereValues().setVisible(false);
            findViewById(R.id.slider_camera_pitch).setVisibility(View.VISIBLE);
            findViewById(R.id.seekBar_camera_yaw).setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    public int getIndexOfSelectedSphereToFollow() {
        return indexOfSelectedSphereToFollow;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Apply low-pass filter to isolate gravity
            final float alpha = 0.8f;
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            // Remove the gravity contribution with the high-pass filter.
            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            if (setTilt) {
                updateSpheresBasedOnTilt(linear_acceleration);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    private void updateSpheresBasedOnTilt(float[] tilt) {
        for (ObjectBlenderModel object : objects) {
            object.applyTilt(tilt, tiltSensitivity);
        }
    }

    public void toggleTilt(boolean enable) {
        setTilt = enable;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public ObjectLightSource getObjectLightSource() {
        return objectLightSource;
    }

    public void setObjectLightSource(ObjectLightSource objectLightSource) {
        this.objectLightSource = objectLightSource;
    }

}