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

public class ListViewAdapterSpheresChooser extends ArrayAdapter<Object> {

    private  MainActivity activityMain;
    private Context context;
    private ArrayList<Object> Objects;
    private Object selectedObject;
    private TextView sphereName;
    private ImageView findPlanet;
    private ImageView ivFollow;

    public ListViewAdapterSpheresChooser(Context context, ArrayList<Object> Objects, MainActivity activityMain) {
        super(context,0, Objects);
        this.context = context;
        this.Objects = Objects;
        this.activityMain = activityMain;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.list_item_body, parent, false);
        }

        selectedObject = Objects.get(position);

        sphereName = listItemView.findViewById(R.id.tvBodyName);
        sphereName.setText(selectedObject.getName());

        findPlanet = listItemView.findViewById(R.id.findPlanet);
        ivFollow = listItemView.findViewById(R.id.ivFollow);

        sphereName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the menu for the selected sphere
//                selectedObject = Objects.get(position);
//                selectedObject.setPosition(position);
//                activityMain.getSpheres().get(position).setPosition(position);
                Toast.makeText(context, "Set " + selectedObject.getName() + " properties", Toast.LENGTH_SHORT).show();
                activityMain.getDialogCelestialSphereValues().setSelectedSphere(selectedObject);
                activityMain.getDialogCelestialSphereValues().initSphereValueMenu(selectedObject); // Sets the values of the selected sphere to the EditText fields in DialogCelestialSphereValues

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
                selectedObject = Objects.get(position);
//                activityMain.getSphereSimulation().setAutoFollow(true);
                activityMain.setIndexOfSelectedSphereToFollow(position);
//                activityMain.getSphereSimulation().searchFunction(position);
                Toast.makeText(context, "Following " + selectedObject.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        return listItemView;
    }

    public Object getSelectedSphere() {
        return selectedObject;
    }
}
