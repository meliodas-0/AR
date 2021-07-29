package com.example.ar.Helper;

import android.content.Context;

import com.example.ar.MainActivity;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.TransformableNode;

public class JoystickHelper {
    Context context;

    public JoystickHelper(Context context) {
        this.context = context;
    }


    public void moveModel(int angle , int strength, Vector3 cameraPosition){
        MainActivity mainActivity = (MainActivity) context;
        TransformableNode node = mainActivity.modelHelper.getNodesSelected().peek();
        //TODO: This needs some calibration this is not perfect.
        double a = Math.toRadians(angle);
        double str = strength * 0.0001;
        Vector3 position = node.getLocalPosition();
        cameraPosition = Vector3.subtract(position, cameraPosition);

        /*
        Get the angle of the camera and move the object according to that angle this needs calibration the most.
        *The derivation of this formula is given in the readme file.
        * This -90° is due to the joystick 0° is → (right) so to move model forward 0° needs to be ↑(up).
        * */
        a += Math.acos(Math.sqrt(cameraPosition.x * cameraPosition.x / (cameraPosition.x * cameraPosition.x + cameraPosition.z * cameraPosition.z))) + Math.toRadians(-90);

        position.x += str * Math.cos(a);
        position.z -= str * Math.sin(a);
        node.setLocalPosition(position);
    }
}
