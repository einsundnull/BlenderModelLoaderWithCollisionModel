package com.notorein.planetarySystem3D;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private Context context;
    private ListView dialog_celestial_body_chooser_list_view;
    private ListViewAdapterSpheresChooser spheresAdapter;
    private ImageView menuButton, buttonRemove, buttonAdd, buttonReset, buttonSpeedUp, buttonPlay, buttonSpeedDown;
    private float trailThickness = 5.5f;
    private FileClassBodyValues bodyStorage;
    private DialogCelestialSphereChooser dialogCelestialSphereChooser;
    private LinearLayout dialogCelestialBodyChooserLayout;
    private DialogCelestialSphereValues dialogCelestialSphereValues;
    private DialogPositionOnScreen dialogPositionOnScreen;
    private TextView tvPositionXOnScreen, tvPositionYOnScreen, tvPositionZOnScreen;
    private int screenWidth, screenHeight;
    private ImageView button_follow_unfollow;
    private float cameraSensitivity = 0.1f; // Reduced sensitivity
    private float touchX, touchY;
    private float cameraAngleX = 0, cameraAngleY = 0;
    private float cameraPosX = 0, cameraPosY = 0, cameraPosZ = -10.0f;
    private float scaleFactor = 100.0f;
    private boolean isScaling;
    private boolean pause;
    private ScaleGestureDetector scaleGestureDetector;
    private ArrayList<Sphere> spheres;
    private CubeRenderer renderer;
    private int indexOfSelectedSphereToFollow = 3;
    private Slider sliderCameraPitch, sliderCameraYaw;

    private Grid gridXY, gridXZ, gridYZ;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        this.context = this;
        glSurfaceView = findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        bodyStorage = new FileClassBodyValues(this);
        spheres = bodyStorage.loadSpheres(context);

        initSpheres();
        initGrids();
        initDialogs();
        initViews();
        initOnClickListeners();
        setCoordinatesText(0, 0, 0);
        spheresAdapter = new ListViewAdapterSpheresChooser(context, spheres, this);
        dialog_celestial_body_chooser_list_view.setAdapter(spheresAdapter);

        renderer = new CubeRenderer(context, this, spheres, gridXY, gridXZ, gridYZ);
        glSurfaceView.setRenderer(renderer);

        // Initialize the ScaleGestureDetector
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.0001f, Math.min(scaleFactor, 5000.0f));
                isScaling = true;
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                isScaling = true;
                return super.onScaleBegin(detector);
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isScaling = false;
                super.onScaleEnd(detector);
            }
        });

        // Set the OnTouchListener for the GLSurfaceView
        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            private float initialY1, initialY2;
            private boolean isTwoFingerMove = false;
            private float previousPitch = 0; // Store the previous pitch value

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        touchX = event.getX();
                        touchY = event.getY();
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (event.getPointerCount() == 2) {
                            initialY1 = event.getY(0);
                            initialY2 = event.getY(1);
                            isTwoFingerMove = true;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isTwoFingerMove && event.getPointerCount() == 2) {
                            float currentY1 = event.getY(0);
                            float currentY2 = event.getY(1);
                            float dy1 = currentY1 - initialY1;
                            float dy2 = currentY2 - initialY2;
                            float averageDy = (dy1 + dy2) / 2;

                            // Adjust the camera pitch based on the average vertical movement
                            cameraAngleX = previousPitch + averageDy * cameraSensitivity * 10;

                            initialY1 = currentY1;
                            initialY2 = currentY2;
                        } else if (!isScaling) {
                            float dx = ((event.getX() - touchX) * cameraSensitivity);
                            float dy = ((event.getY() - touchY) * cameraSensitivity);
                            cameraPosX += dx;
                            cameraPosY -= dy;
                            touchX = event.getX();
                            touchY = event.getY();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        isScaling = false;
                        isTwoFingerMove = false;
                        previousPitch = cameraAngleX; // Update the previous pitch value
                        break;
                }
                return true;
            }
        });

        // Initialize the Slider for pitch and set the listener
        sliderCameraPitch = findViewById(R.id.slider_camera_pitch);
        sliderCameraPitch.setValueFrom(0);
        sliderCameraPitch.setValueTo(360);
        sliderCameraPitch.setValue(180); // Set the initial value to the middle
        sliderCameraPitch.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                if (fromUser) {
                    // Convert the value to a pitch value
                    cameraAngleX = value - 180; // Convert to range -180 to 180
                }
            }
        });

        // Initialize the Slider for yaw and set the listener
        sliderCameraYaw = findViewById(R.id.seekBar_camera_yaw);
        sliderCameraYaw.setValueFrom(0);
        sliderCameraYaw.setValueTo(360);
        sliderCameraYaw.setValue(180); // Set the initial value to the middle
        sliderCameraYaw.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                if (fromUser) {
                    // Convert the value to a yaw value
                    cameraAngleY = value - 180; // Convert to range -180 to 180
                }
            }
        });
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
//            resetSimulation();
        });

        buttonSpeedUp.setOnClickListener(v -> {
            UIClass.animateClick(buttonSpeedUp);
//            sphereSimulation.speedUpSimulation();
//            sphereSimulation.speedUpSimulation();
        });

        buttonPlay.setOnClickListener(v -> {
            UIClass.animateClick(buttonPlay);
            togglePause();
            buttonPlay.setImageLevel(pause ? 1 : 0);
        });

        buttonSpeedDown.setOnClickListener(v -> {
            UIClass.animateClick(buttonSpeedDown);
//            sphereSimulation.speedDownSimulation();
//            sphereSimulation.speedDownSimulation();
        });

        button_follow_unfollow.setOnClickListener(v -> {
            UIClass.animateClick(button_follow_unfollow);
//            sphereSimulation.setAutoFollow(!sphereSimulation.isAutoFollow());
//            sphereSimulation.setAutoFollow(!sphereSimulation.isAutoFollow());
//            Toast.makeText(context, "Auto Follow: " + spheres.get(sphereSimulation.getIndexOfSelectedSphereToFollow()).getName() + " " + sphereSimulation.isAutoFollow(), Toast.LENGTH_SHORT).show();
        });
    }

    public ArrayList<Sphere> initSpheres() {
        spheres = new ArrayList<>();
        spheres.add(new Sphere(0, ConfigCelestialSphere.Sun.X, ConfigCelestialSphere.Sun.Y, ConfigCelestialSphere.Sun.Z, ConfigCelestialSphere.Sun.VX, ConfigCelestialSphere.Sun.VY, ConfigCelestialSphere.Sun.VZ, ConfigCelestialSphere.Sun.MASS, ConfigCelestialSphere.Sun.COLOR, ConfigCelestialSphere.Sun.TRAIL_COLOR, ConfigCelestialSphere.Sun.SIZE, ConfigCelestialSphere.Sun.TRAIL_LENGTH, ConfigCelestialSphere.Sun.TRAIL_THICKNESS, ConfigCelestialSphere.Sun.NAME));
        spheres.add(new Sphere(1, ConfigCelestialSphere.Mercury.X, ConfigCelestialSphere.Mercury.Y, ConfigCelestialSphere.Mercury.Z, ConfigCelestialSphere.Mercury.VX, ConfigCelestialSphere.Mercury.VY, ConfigCelestialSphere.Mercury.VZ, ConfigCelestialSphere.Mercury.MASS, ConfigCelestialSphere.Mercury.COLOR, ConfigCelestialSphere.Mercury.TRAIL_COLOR, ConfigCelestialSphere.Mercury.SIZE, ConfigCelestialSphere.Mercury.TRAIL_LENGTH, ConfigCelestialSphere.Mercury.TRAIL_THICKNESS, ConfigCelestialSphere.Mercury.NAME));
        spheres.add(new Sphere(2, ConfigCelestialSphere.Venus.X, ConfigCelestialSphere.Venus.Y, ConfigCelestialSphere.Venus.Z, ConfigCelestialSphere.Venus.VX, ConfigCelestialSphere.Venus.VY, ConfigCelestialSphere.Venus.VZ, ConfigCelestialSphere.Venus.MASS, ConfigCelestialSphere.Venus.COLOR, ConfigCelestialSphere.Venus.TRAIL_COLOR, ConfigCelestialSphere.Venus.SIZE, ConfigCelestialSphere.Venus.TRAIL_LENGTH, ConfigCelestialSphere.Venus.TRAIL_THICKNESS, ConfigCelestialSphere.Venus.NAME));
        spheres.add(new Sphere(3, ConfigCelestialSphere.Earth.X, ConfigCelestialSphere.Earth.Y, ConfigCelestialSphere.Earth.Z, ConfigCelestialSphere.Earth.VX, ConfigCelestialSphere.Earth.VY, ConfigCelestialSphere.Earth.VZ, ConfigCelestialSphere.Earth.MASS, ConfigCelestialSphere.Earth.COLOR, ConfigCelestialSphere.Earth.TRAIL_COLOR, ConfigCelestialSphere.Earth.SIZE, ConfigCelestialSphere.Earth.TRAIL_LENGTH, ConfigCelestialSphere.Earth.TRAIL_THICKNESS, ConfigCelestialSphere.Earth.NAME));
        spheres.add(new Sphere(4, ConfigCelestialSphere.Moon.X, ConfigCelestialSphere.Moon.Y, ConfigCelestialSphere.Moon.Z, ConfigCelestialSphere.Moon.VX, ConfigCelestialSphere.Moon.VY, ConfigCelestialSphere.Moon.VZ, ConfigCelestialSphere.Moon.MASS, ConfigCelestialSphere.Moon.COLOR, ConfigCelestialSphere.Moon.TRAIL_COLOR, ConfigCelestialSphere.Moon.SIZE, ConfigCelestialSphere.Moon.TRAIL_LENGTH, ConfigCelestialSphere.Moon.TRAIL_THICKNESS, ConfigCelestialSphere.Moon.NAME));
        spheres.add(new Sphere(5, ConfigCelestialSphere.Mars.X, ConfigCelestialSphere.Mars.Y, ConfigCelestialSphere.Mars.Z, ConfigCelestialSphere.Mars.VX, ConfigCelestialSphere.Mars.VY, ConfigCelestialSphere.Mars.VZ, ConfigCelestialSphere.Mars.MASS, ConfigCelestialSphere.Mars.COLOR, ConfigCelestialSphere.Mars.TRAIL_COLOR, ConfigCelestialSphere.Mars.SIZE, ConfigCelestialSphere.Mars.TRAIL_LENGTH, ConfigCelestialSphere.Mars.TRAIL_THICKNESS, ConfigCelestialSphere.Mars.NAME));
        spheres.add(new Sphere(6, ConfigCelestialSphere.Jupiter.X, ConfigCelestialSphere.Jupiter.Y, ConfigCelestialSphere.Jupiter.Z, ConfigCelestialSphere.Jupiter.VX, ConfigCelestialSphere.Jupiter.VY, ConfigCelestialSphere.Jupiter.VZ, ConfigCelestialSphere.Jupiter.MASS, ConfigCelestialSphere.Jupiter.COLOR, ConfigCelestialSphere.Jupiter.TRAIL_COLOR, ConfigCelestialSphere.Jupiter.SIZE, ConfigCelestialSphere.Jupiter.TRAIL_LENGTH, ConfigCelestialSphere.Jupiter.TRAIL_THICKNESS, ConfigCelestialSphere.Jupiter.NAME));
        spheres.add(new Sphere(7, ConfigCelestialSphere.Saturn.X, ConfigCelestialSphere.Saturn.Y, ConfigCelestialSphere.Saturn.Z, ConfigCelestialSphere.Saturn.VX, ConfigCelestialSphere.Saturn.VY, ConfigCelestialSphere.Saturn.VZ, ConfigCelestialSphere.Saturn.MASS, ConfigCelestialSphere.Saturn.COLOR, ConfigCelestialSphere.Saturn.TRAIL_COLOR, ConfigCelestialSphere.Saturn.SIZE, ConfigCelestialSphere.Saturn.TRAIL_LENGTH, ConfigCelestialSphere.Saturn.TRAIL_THICKNESS, ConfigCelestialSphere.Saturn.NAME));
        return spheres;
    }

    private void initGrids() {
        gridXY = new Grid(10, 200.0f, 300.1f, Color.WHITE, 0.0f, 0.0f, 0.0f, 30.0f); // XY-Ebene
        gridXZ = new Grid(10, 200.0f, 300.1f, Color.WHITE, 0.0f, 0.0f, 0.0f, 30.0f); // XZ-Ebene
        gridYZ = new Grid(10, 200.0f, 300.1f, Color.WHITE, 0.0f, 0.0f, 0.0f, 30.0f); // YZ-Ebene
    }


    private void initDialogs() {
        dialogCelestialSphereChooser = new DialogCelestialSphereChooser(context, this);
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

    public ArrayList<Sphere> getSpheres() {
        return spheres;
    }

    public void setIndexOfSelectedSphereToFollow(int indexOfSelectedSphereToFollow) {
        this.indexOfSelectedSphereToFollow = indexOfSelectedSphereToFollow;
    }

    public int getIndexOfSelectedSphereToFollow() {
        return indexOfSelectedSphereToFollow;
    }
}
