package com.example.ar.Helper;

import android.content.Context;
import android.widget.Toast;

import com.example.ar.Helper.Enums.DISTANCE_TO_FIND_BETWEEN;
import com.example.ar.MainActivity;
import com.example.ar.R;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Stack;
/**
 * Helper to manage the Distances. Hides the Android boilerplate code, and exposes simpler
 * methods.
 */
public class DistanceHelper {
    Context context;
    private DISTANCE_TO_FIND_BETWEEN distanceToFindBetween = DISTANCE_TO_FIND_BETWEEN.OBJECT_TO_OBJECT;

    public DistanceHelper(Context context) {
        this.context = context;
    }

    public DISTANCE_TO_FIND_BETWEEN getDistanceToFindBetween() {
        return distanceToFindBetween;
    }

    public void setDistanceToFindBetween(DISTANCE_TO_FIND_BETWEEN distanceToFindBetween) {
        this.distanceToFindBetween = distanceToFindBetween;
    }

    /*Used to measure distance between objects with respect to their centers*/
    private float measureObjectToObjectDistance(Vector3 startPose, Vector3 endPose) {
        float distance;
        float dx = startPose.x - endPose.x;
        float dy = startPose.y - endPose.y;
        float dz = startPose.z - endPose.z;

        distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance;
    }

    /*method to trigger the function by the measureButton*/
    public void checkDistance() {
        String result = "";
        MainActivity mainActivity = (MainActivity) context;
        Stack<TransformableNode> nodesSelected = mainActivity.modelHelper.getNodesSelected();
        Stack<Plane> planeStack = mainActivity.planeStack;
        switch (distanceToFindBetween) {
            case OBJECT_TO_OBJECT:
                if (nodesSelected.size() < 2) {
                    result = "Select two nodes";
                } else {
                    TransformableNode first = nodesSelected.pop();
                    float distance = measureObjectToObjectDistance(first.getWorldPosition(), nodesSelected.peek().getWorldPosition());
                    nodesSelected.push(first);
                    result = "distance between Objects is" + distance + "m";

                }
                break;
            case PLANE_TO_PLANE:
                if (planeStack.size() < 2) result = "Select Two Planes.";
                else {
                    Plane plane1 = planeStack.pop();
                    Plane plane2 = planeStack.peek();
                    planeStack.add(plane1);
                    result = "Distance between planes is : " + measurePlaneToPlaneDistance(plane1.getCenterPose(), plane2.getCenterPose()) + "m";
                }
                break;
            case PLANE_TO_OBJECT:
                if (planeStack.isEmpty() || !nodesSelected.isEmpty())
                    result = "Either plane or object is not selected.";
                else {
                    result = "Distance : " + measureObjectToPlaneDistance(nodesSelected.peek().getLocalPosition(), planeStack.peek().getCenterPose()) + "m";
                }
        }

        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

    /*Used to measure distance between object and plane with respect to their centers*/
    private float measureObjectToPlaneDistance(Vector3 objectPose, Pose planePose) {

        float distance;
        float dx = objectPose.x - planePose.tx();
        float dy = objectPose.y - planePose.ty();
        float dz = objectPose.z - planePose.tz();

        distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance;
    }

    /*Used to measure distance between planes with respect to their center pose*/
    private float measurePlaneToPlaneDistance(Pose plane1pose, Pose plane2Pose) {
        float distance;
        float dx = plane1pose.tx() - plane2Pose.tx();
        float dy = plane1pose.ty() - plane2Pose.ty();
        float dz = plane1pose.tz() - plane2Pose.tz();

        distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance;
    }

    /*Triggers change in the measuring targets*/
    public void changeMeasurementTargets() {

        MainActivity mainActivity = (MainActivity) context;

        switch (distanceToFindBetween) {
            case OBJECT_TO_OBJECT:
                mainActivity.measureButton.setText(R.string.object_to_object_distance);
                break;
            case PLANE_TO_OBJECT:
                mainActivity.measureButton.setText(R.string.object_to_plane_distance);
                break;
            case PLANE_TO_PLANE:
                mainActivity.measureButton.setText(R.string.plane_to_plane_distance);
                break;
        }

    }
}
