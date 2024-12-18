package com.notorein.threedmodeling;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class DialogListViewAdapterSpheresChooser extends ArrayAdapter<Object> {

    private  MainActivity activityMain;
    private Context context;
    private List<ObjectBlenderModel> objects;
    private ObjectBlenderModel selectedObject;
    private TextView sphereName;
    private ImageView findPlanet;
    private ImageView ivFollow;

    public DialogListViewAdapterSpheresChooser(Context context, List<ObjectBlenderModel> objects, MainActivity activityMain) {
        super(context, 0);
//        super(context,0, objects);
        this.context = context;
        this.objects = objects;
        this.activityMain = activityMain;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.list_item_body, parent, false);
        }

        selectedObject = objects.get(position);

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
//                UIClass.animateClick(ivFollow);
//                selectedObject = ObjectBlenderModel.get(position);
////                activityMain.getSphereSimulation().setAutoFollow(true);
//                activityMain.setIndexOfSelectedSphereToFollow(position);
////                activityMain.getSphereSimulation().searchFunction(position);
//                Toast.makeText(context, "Following " + selectedObject.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        return listItemView;
    }

    public ObjectBlenderModel getSelectedSphere() {
        return selectedObject;
    }
}
