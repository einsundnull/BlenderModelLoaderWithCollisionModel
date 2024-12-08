package com.notorein.planetarySystem3D;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ActivityMain extends AppCompatActivity {
    private Context context;
    private ListView listView;
    private ListViewAdapterSpheresChooser spheresAdapter;

    private ArrayList<SphereII> sphereIIS;
    private ImageView menuButton, buttonRemove, buttonAdd, buttonReset, buttonSpeedUp, buttonPlay, buttonSpeedDown;

    private float trailThickness = 5.5f;
    private FileClassBodyValues bodyStorage;

    private DialogCelestialSphereChooser dialogCelestialBodyChooser;
    private LinearLayout dialogCelestialBodyChooserLayout;

    private DialogCelestialSphereValues dialogCelestialBodyValues;
    private DialogPositionOnScreen dialogPositionOnScreen;


    private TextView tvPositionXOnScreen;
    private TextView tvPositionYOnScreen;
    private TextView tvPositionZOnScreen;

    private int screenWidth;
    private int screenHeight;
    private ImageView button_follow_unfollow;
    private GLSurfaceView glSurfaceView;
    private SphereSimulation sphereSimulation;

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

//        bodyStorage = new FileClassBodyValues(this);
//        sphereIIS = bodyStorage.loadSpheres(this);
//        initDialogs();
        initViews();
        initOnClickListeners();

        listView.setAdapter(spheresAdapter);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        setCoordinatesText(0, 0, 0);

        glSurfaceView = findViewById(R.id.glSurfaceView);
        sphereSimulation = new SphereSimulation(this);
        sphereSimulation.setActivityMain(this);
        glSurfaceView.setRenderer(sphereSimulation);

//        initializeSpheres();
        sphereSimulation.setSpheres(sphereIIS);
        buttonPlay.setImageLevel(sphereSimulation.getPause() ? 1 : 0);
    }

    public ActivityMain getActivityMain() {
        return this;
    }

    private void initViews() {
//        sphereSimulation = findViewById(R.id.simulationView);
        sphereSimulation.setActivityMain(this);

        listView = findViewById(R.id.dialog_celestial_body_chooser_list_view);
//        spheresAdapter = new ListViewAdapterSpheresChooser(this, spheres, this);
        menuButton = findViewById(R.id.button_menu);
        buttonAdd = findViewById(R.id.button_add);
        buttonRemove = findViewById(R.id.button_remove);
        buttonReset = findViewById(R.id.button_reset);
        buttonSpeedUp = findViewById(R.id.button_speed_up);
        buttonPlay = findViewById(R.id.button_play);
        buttonSpeedDown = findViewById(R.id.button_speed_down);
        button_follow_unfollow = findViewById(R.id.button_follow_unfollow);

        tvPositionXOnScreen = findViewById(R.id.tv_position_x_on_screen);
        tvPositionYOnScreen = findViewById(R.id.tv_position_y_on_screen);
        tvPositionZOnScreen = findViewById(R.id.tv_position_z_on_screen);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (glSurfaceView != null) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (glSurfaceView != null) {
            glSurfaceView.onResume();
        }
    }

    private void initOnClickListeners() {
        menuButton.setOnClickListener(v -> {
            if (!sphereSimulation.getPause()) {
                sphereSimulation.togglePause();
                buttonPlay.setImageLevel(sphereSimulation.getPause() ? 1 : 0);
            }
            UIClass.animateClick(menuButton);
            getDialogCelestialSphereChooser().setVisible(true);
        });

        buttonAdd.setOnClickListener(v -> {
            UIClass.animateClick(buttonAdd);
            sphereSimulation.togglePause();
            sphereSimulation.togglePause();
            buttonPlay.setImageLevel(sphereSimulation.getPause() ? 1 : 0);
            dialogCelestialBodyChooser.setVisible(true);
        });

        buttonRemove.setOnClickListener(v -> {
            UIClass.animateClick(buttonRemove);
            sphereSimulation.togglePause();
            sphereSimulation.togglePause();
            buttonPlay.setImageLevel(sphereSimulation.getPause() ? 1 : 0);
            dialogCelestialBodyChooser.setVisible(true);
        });

        buttonReset.setOnClickListener(v -> {
            UIClass.animateClick(buttonReset);
            resetSimulation();
        });

        buttonSpeedUp.setOnClickListener(v -> {
            UIClass.animateClick(buttonSpeedUp);
            sphereSimulation.speedUpSimulation();
            sphereSimulation.speedUpSimulation();
        });

        buttonPlay.setOnClickListener(v -> {
            UIClass.animateClick(buttonPlay);
            boolean pause = sphereSimulation.togglePause();
            sphereSimulation.togglePause();
            buttonPlay.setImageLevel(pause ? 1 : 0);
        });

        buttonSpeedDown.setOnClickListener(v -> {
            UIClass.animateClick(buttonSpeedDown);
            sphereSimulation.speedDownSimulation();
            sphereSimulation.speedDownSimulation();
        });

        button_follow_unfollow.setOnClickListener(v -> {
            UIClass.animateClick(button_follow_unfollow);
            sphereSimulation.setAutoFollow(!sphereSimulation.isAutoFollow());
            sphereSimulation.setAutoFollow(!sphereSimulation.isAutoFollow());
            Toast.makeText(context, "Auto Follow: " + sphereIIS.get(sphereSimulation.getIndexOfSelectedSphereToFollow()).getName() + " " + sphereSimulation.isAutoFollow(), Toast.LENGTH_SHORT).show();
        });
    }

    public void resetSimulation() {

//        initializeSpheres();
        sphereSimulation.invalidate();
        sphereSimulation.setSpheres(sphereIIS);
    }

//    private void initDialogs() {
//        dialogCelestialBodyChooser = new DialogCelestialSphereChooser(this, this);
//        dialogCelestialBodyValues = new DialogCelestialValues(this, this);
//        dialogPositionOnScreen = new DialogPositionOnScreen(this, this);
//    }

    public DialogCelestialSphereValues getDialogCelestialSphereValues() {
        return dialogCelestialBodyValues;
    }

    public void setDialogCelestialBodyValues(DialogCelestialSphereValues dialogCelestialBodyValues) {
        this.dialogCelestialBodyValues = dialogCelestialBodyValues;
    }

    public DialogPositionOnScreen getDialogPositionOnScreen() {
        return dialogPositionOnScreen;
    }

    public void setDialogPositionOnScreen(DialogPositionOnScreen dialogPositionOnScreen) {
        this.dialogPositionOnScreen = dialogPositionOnScreen;
    }

    public DialogCelestialSphereChooser getDialogCelestialSphereChooser() {
        return dialogCelestialBodyChooser;
    }

    public void setDialogCelestialBodyChooser(DialogCelestialSphereChooser dialogCelestialBodyChooser) {
        this.dialogCelestialBodyChooser = dialogCelestialBodyChooser;
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


//    public ArrayList<SphereII> initializeSpheres() {
//        sphereIIS = new ArrayList<>();
//        sphereIIS.add(new SphereII(0, ConfigCelestialBody.Sun.X, ConfigCelestialBody.Sun.Y, ConfigCelestialBody.Sun.Z, ConfigCelestialBody.Sun.VX, ConfigCelestialBody.Sun.VY, ConfigCelestialBody.Sun.VZ, ConfigCelestialBody.Sun.MASS, ConfigCelestialBody.Sun.COLOR, ConfigCelestialBody.Sun.TRAIL_COLOR, ConfigCelestialBody.Sun.SIZE, ConfigCelestialBody.Sun.TRAIL_LENGTH, ConfigCelestialBody.Sun.TRAIL_THICKNESS, ConfigCelestialBody.Sun.NAME));
//        sphereIIS.add(new SphereII(1, ConfigCelestialBody.Mercury.X, ConfigCelestialBody.Mercury.Y, ConfigCelestialBody.Mercury.Z, ConfigCelestialBody.Mercury.VX, ConfigCelestialBody.Mercury.VY, ConfigCelestialBody.Mercury.VZ, ConfigCelestialBody.Mercury.MASS, ConfigCelestialBody.Mercury.COLOR, ConfigCelestialBody.Mercury.TRAIL_COLOR, ConfigCelestialBody.Mercury.SIZE, ConfigCelestialBody.Mercury.TRAIL_LENGTH, ConfigCelestialBody.Mercury.TRAIL_THICKNESS, ConfigCelestialBody.Mercury.NAME));
//        sphereIIS.add(new SphereII(2, ConfigCelestialBody.Venus.X, ConfigCelestialBody.Venus.Y, ConfigCelestialBody.Venus.Z, ConfigCelestialBody.Venus.VX, ConfigCelestialBody.Venus.VY, ConfigCelestialBody.Venus.VZ, ConfigCelestialBody.Venus.MASS, ConfigCelestialBody.Venus.COLOR, ConfigCelestialBody.Venus.TRAIL_COLOR, ConfigCelestialBody.Venus.SIZE, ConfigCelestialBody.Venus.TRAIL_LENGTH, ConfigCelestialBody.Venus.TRAIL_THICKNESS, ConfigCelestialBody.Venus.NAME));
//        sphereIIS.add(new SphereII(3, ConfigCelestialBody.Earth.X, ConfigCelestialBody.Earth.Y, ConfigCelestialBody.Earth.Z, ConfigCelestialBody.Earth.VX, ConfigCelestialBody.Earth.VY, ConfigCelestialBody.Earth.VZ, ConfigCelestialBody.Earth.MASS, ConfigCelestialBody.Earth.COLOR, ConfigCelestialBody.Earth.TRAIL_COLOR, ConfigCelestialBody.Earth.SIZE, ConfigCelestialBody.Earth.TRAIL_LENGTH, ConfigCelestialBody.Earth.TRAIL_THICKNESS, ConfigCelestialBody.Earth.NAME));
//        sphereIIS.add(new SphereII(4, ConfigCelestialBody.Moon.X, ConfigCelestialBody.Moon.Y, ConfigCelestialBody.Moon.Z, ConfigCelestialBody.Moon.VX, ConfigCelestialBody.Moon.VY, ConfigCelestialBody.Moon.VZ, ConfigCelestialBody.Moon.MASS, ConfigCelestialBody.Moon.COLOR, ConfigCelestialBody.Moon.TRAIL_COLOR, ConfigCelestialBody.Moon.SIZE, ConfigCelestialBody.Moon.TRAIL_LENGTH, ConfigCelestialBody.Moon.TRAIL_THICKNESS, ConfigCelestialBody.Moon.NAME));
//        sphereIIS.add(new SphereII(5, ConfigCelestialBody.Mars.X, ConfigCelestialBody.Mars.Y, ConfigCelestialBody.Mars.Z, ConfigCelestialBody.Mars.VX, ConfigCelestialBody.Mars.VY, ConfigCelestialBody.Mars.VZ, ConfigCelestialBody.Mars.MASS, ConfigCelestialBody.Mars.COLOR, ConfigCelestialBody.Mars.TRAIL_COLOR, ConfigCelestialBody.Mars.SIZE, ConfigCelestialBody.Mars.TRAIL_LENGTH, ConfigCelestialBody.Mars.TRAIL_THICKNESS, ConfigCelestialBody.Mars.NAME));
//        sphereIIS.add(new SphereII(6, ConfigCelestialBody.Jupiter.X, ConfigCelestialBody.Jupiter.Y, ConfigCelestialBody.Jupiter.Z, ConfigCelestialBody.Jupiter.VX, ConfigCelestialBody.Jupiter.VY, ConfigCelestialBody.Jupiter.VZ, ConfigCelestialBody.Jupiter.MASS, ConfigCelestialBody.Jupiter.COLOR, ConfigCelestialBody.Jupiter.TRAIL_COLOR, ConfigCelestialBody.Jupiter.SIZE, ConfigCelestialBody.Jupiter.TRAIL_LENGTH, ConfigCelestialBody.Jupiter.TRAIL_THICKNESS, ConfigCelestialBody.Jupiter.NAME));
//        sphereIIS.add(new SphereII(7, ConfigCelestialBody.Saturn.X, ConfigCelestialBody.Saturn.Y, ConfigCelestialBody.Saturn.Z, ConfigCelestialBody.Saturn.VX, ConfigCelestialBody.Saturn.VY, ConfigCelestialBody.Saturn.VZ, ConfigCelestialBody.Saturn.MASS, ConfigCelestialBody.Saturn.COLOR, ConfigCelestialBody.Saturn.TRAIL_COLOR, ConfigCelestialBody.Saturn.SIZE, ConfigCelestialBody.Saturn.TRAIL_LENGTH, ConfigCelestialBody.Saturn.TRAIL_THICKNESS, ConfigCelestialBody.Saturn.NAME));
//        return sphereIIS;
//    }

    public void setCoordinatesText(double x, double y, double z) {
        tvPositionXOnScreen.setText("x: " + x + screenWidth / 2);
        tvPositionYOnScreen.setText("y: " + y + screenHeight / 2);
        tvPositionZOnScreen.setText("z: " + z);
    }

    public SphereSimulation getSphereSimulation() {
        return sphereSimulation;
    }

    public ArrayList<SphereII> getSpheres() {
        return sphereIIS;
    }

    public void setSpheres(ArrayList<SphereII> sphereIIS) {
        this.sphereIIS = sphereIIS;
    }
}
