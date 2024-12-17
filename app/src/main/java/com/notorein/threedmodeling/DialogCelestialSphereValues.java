package com.notorein.threedmodeling;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DialogCelestialSphereValues {

    private final Context context;
    //    private List<Body> celestialBodies;
    private double gravityStrength;
    public ObjectBlenderModel selectedObjectSphere;

    private AlertDialog menuDialog;
    private AlertDialog addBodyDialog;
    private AlertDialog removeBodyDialog;
    private AlertDialog setGravityStrengthDialog;
    private AlertDialog setBodyPropertiesDialog;

    private MainActivity activityMain;
    FrameLayout dialog_body_values_layout;

    // Define the fields as class-level variables
    private EditText etMass;
    private EditText etSize;
    private EditText etTrailLength;
    private EditText etVx;
    private EditText etVy;
    private EditText etVz;
    private EditText etX;
    private EditText etY;
    private EditText etZ;
    private View colorView;
    private View trailColorView;
    private ImageView setLocation;
    private Button buttonCancel;
    private Button buttonOk;
    private TextView tvName;

    public DialogCelestialSphereValues(Context context, MainActivity activityMain) {
        this.context = context;
        this.activityMain = activityMain;
        dialog_body_values_layout = this.activityMain.findViewById(R.id.dialog_body_values_layout);
        initViews();
        initSphereValueMenu(selectedObjectSphere);
    }

    public void setActivityMain(MainActivity activityMain) {
        this.activityMain = activityMain;
    }

    private void initViews() {
        tvName = dialog_body_values_layout.findViewById(R.id.tvName);
        etMass = dialog_body_values_layout.findViewById(R.id.etMass);
        etSize = dialog_body_values_layout.findViewById(R.id.etSize);
        etTrailLength = dialog_body_values_layout.findViewById(R.id.etTrailLength);
        etVx = dialog_body_values_layout.findViewById(R.id.etVx);
        etVy = dialog_body_values_layout.findViewById(R.id.etVy);
        etVz = dialog_body_values_layout.findViewById(R.id.etVz);
        etX = dialog_body_values_layout.findViewById(R.id.etX);
        etY = dialog_body_values_layout.findViewById(R.id.etY);
        etZ = dialog_body_values_layout.findViewById(R.id.etZ);
        colorView = dialog_body_values_layout.findViewById(R.id.colorView);
        trailColorView = dialog_body_values_layout.findViewById(R.id.trailColorView);
        setLocation = dialog_body_values_layout.findViewById(R.id.button_set_location);
        buttonCancel = dialog_body_values_layout.findViewById(R.id.button_cancel_menu_values);
        buttonOk = dialog_body_values_layout.findViewById(R.id.button_ok_menu_values);
    }

    private void initNewBodyValueMenu() {
        setLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIClass.animateClick(setLocation);
                activityMain.getDialogCelestialSphereChooser().setVisible(false);
                setVisible(false);
                activityMain.getDialogPositionOnScreen().setSelectedBody(selectedObjectSphere);
                activityMain.getDialogPositionOnScreen().setSelectedBodyValues();
                activityMain.getDialogPositionOnScreen().setVisible(true);
            }
        });

        colorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPickerDialog(colorView);
            }
        });

        trailColorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPickerDialog(trailColorView);
            }
        });
    }

    private void initializeRemoveBodyDialog() {
        if (activityMain.getSpheres().isEmpty()) {
            Toast.makeText(context, "No celestial bodies to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Remove Celestial Body");
        final CharSequence[] items = new CharSequence[activityMain.getSpheres().size()];
        for (int i = 0; i < activityMain.getSpheres().size(); i++) {
            items[i] = "Body " + (i + 1);
        }
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                activityMain.getSpheres().remove(which);
                Toast.makeText(context, "Celestial Body Removed", Toast.LENGTH_SHORT).show();
            }
        });
        removeBodyDialog = builder.create();
    }

    private void initializeSetGravityStrengthDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set Gravity Strength");

        final EditText input = new EditText(context);
        builder.setView(input);

        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    gravityStrength = Double.parseDouble(input.getText().toString());
                    Toast.makeText(context, "Gravity Strength Set", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        setGravityStrengthDialog = builder.create();
    }

    public void initSphereValueMenu(ObjectBlenderModel selectedObjectSphere) {
        if (this.selectedObjectSphere == null) {
//            Toast.makeText(context, "No celestial body selected", Toast.LENGTH_SHORT).show();
            return;
        }

        setSelectedBodyValues();

        setLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIClass.animateClick(setLocation);
                activityMain.getDialogCelestialSphereChooser().setVisible(false);
                setVisible(false);

                activityMain.getDialogPositionOnScreen().setSelectedBody(DialogCelestialSphereValues.this.selectedObjectSphere);
                activityMain.getDialogPositionOnScreen().setSelectedBodyValues();
                activityMain.getDialogPositionOnScreen().setVisible(true);
            }
        });

        colorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPickerDialog(colorView);
            }
        });

        trailColorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPickerDialog(trailColorView);
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
//                    double mass = Float.parseFloat(etMass.getText().toString());
//                    float size = Float.parseFloat(etSize.getText().toString());
//                    int trailLength = Integer.parseInt(etTrailLength.getText().toString());
//                    double vx = Float.parseFloat(etVx.getText().toString());
//                    double vy = Float.parseFloat(etVy.getText().toString());
//                    double vz = Float.parseFloat(etVz.getText().toString());
//                    double x = Double.parseDouble(etX.getText().toString());
//                    double y = Double.parseDouble(etY.getText().toString());
//                    double z = Double.parseDouble(etZ.getText().toString());
//                    int color = ((ColorDrawable) colorView.getBackground()).getColor();
//                    int trailColor = ((ColorDrawable) trailColorView.getBackground()).getColor();
//
//                    Object updatedBody = new Object(selectedObjectSphere.getPositionIndex(),false,true, x, y, z, vx, vy, vz, mass, color, trailColor, size, trailLength, 0.5f, selectedObjectSphere.getName());
////                    updatedBody.setTrail(new ArrayList<>(trailLength));
//                    int index = activityMain.getSpheres().indexOf(selectedObjectSphere);
//                    if (index != -1) {
//                        activityMain.getSpheres().set(index, updatedBody);
//                    }
//                    FileClassBodyValues.saveSpheres(context, activityMain.getSpheres());
//                    activityMain.getDialogCelestialSphereChooser().setVisible(true);
//                    activityMain.getDialogCelestialSphereValues().setVisible(true);
//                    activityMain.setButtonsVisible(true);
//                    setVisible(false);
//                    Toast.makeText(context, "Body Properties Set", Toast.LENGTH_SHORT).show();
//                } catch (NumberFormatException e) {
//                    Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityMain.getDialogCelestialSphereChooser().setVisible(true);
                activityMain.getDialogCelestialSphereValues().setVisible(true);
                activityMain.setButtonsVisible(true);
                setVisible(false);
            }
        });
    }

    public void setSelectedBodyValues() {
        tvName.setText(selectedObjectSphere.getName());
        etMass.setText(String.valueOf(selectedObjectSphere.getMass()));
        etSize.setText(String.valueOf(selectedObjectSphere.getSize()));
//        etTrailLength.setText(String.valueOf(selectedObjectSphere.getTrailLength()));
        etVx.setText(String.valueOf(selectedObjectSphere.getVx()));
        etVy.setText(String.valueOf(selectedObjectSphere.getVy()));
        etVz.setText(String.valueOf(selectedObjectSphere.getVz())); // Assuming vz is not used in the Body class
        etX.setText(String.valueOf(selectedObjectSphere.getX()));
        etY.setText(String.valueOf(selectedObjectSphere.getY()));
        etZ.setText(String.valueOf(selectedObjectSphere.getZ()));// Assuming z is not used in the Body class
        colorView.setBackgroundColor(selectedObjectSphere.getColor());
        trailColorView.setBackgroundColor(selectedObjectSphere.getColorTrail());
    }

    private void showColorPickerDialog(final View colorView) {
        DialogColorPicker colorPickerDialog = new DialogColorPicker();
        colorPickerDialog.setOnColorSelectedListener(new DialogColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                colorView.setBackgroundColor(color);
            }
        });
        colorPickerDialog.show(((MainActivity) context).getSupportFragmentManager(), "colorPicker");
    }

    public void setSelectedSphere(ObjectBlenderModel selectedObjectSphere) {
        this.selectedObjectSphere = selectedObjectSphere;
    }

    public ObjectBlenderModel getSelectedSphere() {
        return this.selectedObjectSphere;
    }

    public void setVisible(boolean visible) {
        activityMain.setButtonsVisible(!visible);
        if (visible) {
            dialog_body_values_layout.setVisibility(View.VISIBLE);
        } else {
            dialog_body_values_layout.setVisibility(View.INVISIBLE);
        }
    }

    public boolean isVisible() {
        if (dialog_body_values_layout.getVisibility() == View.VISIBLE) {
            return true;
        } else {
            return false;
        }
    }
}
