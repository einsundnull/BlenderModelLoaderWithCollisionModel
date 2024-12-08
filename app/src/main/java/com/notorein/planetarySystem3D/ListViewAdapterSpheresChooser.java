package com.notorein.planetarySystem3D;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ListViewAdapterSpheresChooser extends ArrayAdapter<Sphere> {

    private  MainActivity activityMain;
    private Context context;
    private ArrayList<Sphere> spheres;
    private Sphere selectedSphere;
    private TextView sphereName;
    private ImageView findPlanet;
    private ImageView ivFollow;

    public ListViewAdapterSpheresChooser(Context context, ArrayList<Sphere> spheres, MainActivity activityMain) {
        super(context,0, spheres);
        this.context = context;
        this.spheres = spheres;
        this.activityMain = activityMain;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.list_item_body, parent, false);
        }

        selectedSphere = spheres.get(position);

        sphereName = listItemView.findViewById(R.id.tvBodyName);
        sphereName.setText(selectedSphere.getName());

        findPlanet = listItemView.findViewById(R.id.findPlanet);
        ivFollow = listItemView.findViewById(R.id.ivFollow);

        sphereName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the menu for the selected sphere
                selectedSphere = spheres.get(position);
                selectedSphere.setPosition(position);
                activityMain.getSpheres().get(position).setPosition(position);
                Toast.makeText(context, "Set " + selectedSphere.getName() + " properties", Toast.LENGTH_SHORT).show();
                activityMain.getDialogCelestialSphereValues().setSelectedSphere(selectedSphere);
                activityMain.getDialogCelestialSphereValues().initSphereValueMenu(selectedSphere); // Sets the values of the selected sphere to the EditText fields in DialogCelestialsphereValues

                activityMain.getDialogCelestialSphereValues().setActivityMain(activityMain);
                activityMain.getDialogCelestialSphereChooser().setVisible(false);
                activityMain.getDialogCelestialSphereValues().setVisible(true);
            }
        });

        findPlanet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Center the view on the selected sphere
                UIClass.animateClick(findPlanet);
                activityMain.setIndexOfSelectedSphereToFollow(position);
//                activityMain.searchFunction(position);
                Toast.makeText(context, "Centering on " + activityMain.getSpheres().get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });

        ivFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Follow the selected sphere
                UIClass.animateClick(ivFollow);
                selectedSphere = spheres.get(position);
//                activityMain.getSphereSimulation().setAutoFollow(true);
                activityMain.setIndexOfSelectedSphereToFollow(position);
//                activityMain.getSphereSimulation().searchFunction(position);
                Toast.makeText(context, "Following " + selectedSphere.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        return listItemView;
    }

    public Sphere getSelectedSphere() {
        return selectedSphere;
    }
}
