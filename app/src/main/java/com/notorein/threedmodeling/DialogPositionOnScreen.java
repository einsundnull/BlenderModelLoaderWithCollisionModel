package com.notorein.threedmodeling;

//import static com.notorein.planetarysystem.ViewSimulation.setBodyPosition;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

public class DialogPositionOnScreen {

    private final Context context;
    private MainActivity activityMain;

    private ObjectBlenderModel selectedBody;

    private ImageView help_position_layout;
    private TextView tvPlanetName;
    private TextView tvCoordinatesX;
    private TextView tvCoordinatesY;
    private TextView tvCoordinatesZ;
    private TextView tvSize;
    private SeekBar sbSize;
    private Button btnSet;
    private Button btnCancel;
    private Button btnChoosePlanet;

    private float dX, dY;
    private ConstraintLayout dialog_set_body_position_layout;

    public DialogPositionOnScreen(@NonNull Context context, MainActivity activityMain) {
        this.context = context;
        this.activityMain = activityMain;
        dialog_set_body_position_layout = activityMain.findViewById(R.id.dialog_set_body_position_layout);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        help_position_layout = dialog_set_body_position_layout.findViewById(R.id.help_position_layout);
        tvPlanetName = dialog_set_body_position_layout.findViewById(R.id.tvPlanetName);
        tvCoordinatesX = dialog_set_body_position_layout.findViewById(R.id.tvCoordinatesX);
        tvCoordinatesY = dialog_set_body_position_layout.findViewById(R.id.tvCoordinatesY);
        tvCoordinatesZ = dialog_set_body_position_layout.findViewById(R.id.tvCoordinatesZ);
        tvSize = dialog_set_body_position_layout.findViewById(R.id.tvSize);
        sbSize = dialog_set_body_position_layout.findViewById(R.id.sbSize);
        btnSet = dialog_set_body_position_layout.findViewById(R.id.btnSet);
        btnCancel = dialog_set_body_position_layout.findViewById(R.id.btnCancel);
        btnChoosePlanet = dialog_set_body_position_layout.findViewById(R.id.btnChoosePlanet);

        setSelectedBodyValues();

        sbSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float size = progress / 100.0f;
                tvSize.setText("Size: " + size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });


        help_position_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIClass.animateClick(help_position_layout);
            }
        });

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedBody != null) {
                    float size = sbSize.getProgress() / 100.0f;
                    selectedBody.size = size;
                    Toast.makeText(context, "Body Size Set", Toast.LENGTH_SHORT).show();
                    activityMain.getDialogCelestialSphereChooser().setVisible(true);
                    activityMain.getDialogCelestialSphereValues().setVisible(true);
                    setVisible(false);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIClass.animateClick(btnCancel);
                activityMain.getDialogCelestialSphereChooser().setVisible(true);
                activityMain.getDialogCelestialSphereValues().setVisible(true);
                setVisible(false);
            }
        });

        btnChoosePlanet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIClass.animateClick(btnChoosePlanet);
                activityMain.getDialogCelestialSphereChooser().setVisible(true);
                setVisible(false);
            }
        });

        // Make the dialog movable
        dialog_set_body_position_layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        v.animate()
                                .x(event.getRawX() + dX)
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    public void setSelectedBodyValues() {
        if (selectedBody != null) {
            tvPlanetName.setText(selectedBody.name);
            tvCoordinatesX.setText("X: " + selectedBody.position.x);
            tvCoordinatesY.setText("Y: " + selectedBody.position.y);
            tvCoordinatesZ.setText("Z: " + selectedBody.position.z);
            tvSize.setText("Size: " + selectedBody.size);
            sbSize.setProgress((int) (selectedBody.size * 100)); // Assuming size is between 0 and 1
        }
    }

    public ObjectBlenderModel getSelectedBody() {
        return selectedBody;
    }

    public void setSelectedBody(ObjectBlenderModel selectedBody) {
        this.selectedBody = selectedBody;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            dialog_set_body_position_layout.setVisibility(View.VISIBLE);
        } else {
            dialog_set_body_position_layout.setVisibility(View.INVISIBLE);
        }
    }
}
