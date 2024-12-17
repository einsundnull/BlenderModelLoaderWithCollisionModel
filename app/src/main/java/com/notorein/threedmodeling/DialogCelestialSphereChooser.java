package com.notorein.threedmodeling;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class DialogCelestialSphereChooser {

    private final ListViewAdapterSpheresChooser bodyAdapter;
    private Context context;
    private MainActivity activityMain;
    private ListView view;
    private RelativeLayout dialog_celestial_body_chooser_layout;

    private ImageView button_close_celestial_body_menu;

    public DialogCelestialSphereChooser(Context context, MainActivity activityMain) {
        this.context = context;
        this.activityMain = activityMain;

        dialog_celestial_body_chooser_layout = activityMain.findViewById(R.id.dialog_celestial_body_chooser_layout);
        view = dialog_celestial_body_chooser_layout.findViewById(R.id.dialog_celestial_body_chooser_list_view);
        button_close_celestial_body_menu = dialog_celestial_body_chooser_layout.findViewById(R.id.button_close_celestial_body_menu);

        button_close_celestial_body_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setVisible(false);
            }

        });


        bodyAdapter = new ListViewAdapterSpheresChooser(this.context, this.activityMain.getSpheres(), this.activityMain);
        view.setAdapter(bodyAdapter);
    }

    public void setVisible(boolean visible) {

        activityMain.setButtonsVisible(!visible);
        if(visible){
            dialog_celestial_body_chooser_layout.setVisibility(View.VISIBLE);
        } else {
            dialog_celestial_body_chooser_layout.setVisibility(View.INVISIBLE);
        }


    }

    public ObjectBlenderModel getSelectedBody() {
        return bodyAdapter.getSelectedSphere();
    }

    public boolean isVisible() {
        if(dialog_celestial_body_chooser_layout.getVisibility() == View.VISIBLE){
            return true;
        } else {
            return false;
        }

    }
}
