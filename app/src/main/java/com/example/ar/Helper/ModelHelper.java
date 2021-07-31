package com.example.ar.Helper;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.ar.MainActivity;
import com.example.ar.R;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Sun;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.List;
import java.util.Objects;
import java.util.Stack;

/**
 * Helper to manage the Model.
 */
public class ModelHelper {
    private final List<Model> modelList;
    private  final Context mContext;
    private boolean placeObjectOverObject = false;
    private final TouchHelper touchHelper;
    private final SnackbarHelper snackbarHelper;
    /*This contains the models which are selected and clears when any model is deleted.
    * Top represents the latest tapped model.*/
    private final Stack<TransformableNode> nodesSelected;
    private boolean firstTimeChildSelected;

    public ModelHelper(Context context, List<Model> modelList) {
        this.modelList = modelList;
        this.mContext = context;
        touchHelper = new TouchHelper();
        snackbarHelper = new SnackbarHelper();
        nodesSelected = new Stack<>();
        firstTimeChildSelected = true;
    }

    public List<Model> getModelList() {
        return modelList;
    }

    public void loadModel(int index){
        MainActivity activity = (MainActivity) mContext;
        if(activity != null){
            activity.model = modelList.get(index).getModelRenderable();
            activity.modelNumber = index;
        }
    }

    /*This method selected the particular node */
    private void selectNode(TransformableNode node) {

        MainActivity mainActivity = (MainActivity) mContext;

        mainActivity.showModelButtons(true);

        if(placeObjectOverObject) {

            PlaceModel(node);
            return;
        }

        if(!isAnchorNode(node.getParent()) && firstTimeChildSelected){
            firstTimeChildSelected = false;
            snackbarHelper.showMessageWithDismiss(mainActivity, "You need to move the child object with the joystick, gestures will move the parent.");
        }

        nodesSelected.remove(node);
        nodesSelected.push(node);
        node.select();
    }

    /*method place the model on the plane or over the object.*/
    public void PlaceModel(Node parent){
        MainActivity mainActivity = (MainActivity) mContext;

        TransformableNode model = new TransformableNode(mainActivity.arFragment.getTransformationSystem());
        model.setParent(parent);
        model.setRenderable(mainActivity.model).animate(true).start();
        model.setOnTouchListener((hitTestResult, ev) -> {
            if (touchHelper.triggerEvent(ev)) selectNode(model);
            return true;
        });
        if(!isAnchorNode(parent)){
            Vector3 position = parent.getLocalPosition();
            position.y += 0.2;
            model.setLocalPosition(position);
        }
        if(mainActivity.modelNumber == 6) {
            float f =  0.05f;
            model.setLocalScale(new Vector3(f,f,f));

            model.getScaleController().setMinScale(0.001f);
            model.getScaleController().setMaxScale(0.1f);
        }else if(mainActivity.modelNumber == 8){
            float f = 0.0005f;
            model.setWorldScale(new Vector3(f,f,f));
            model.getScaleController().setMinScale(0.0001f);
            model.getScaleController().setMaxScale(0.001f);
        }
        if(isAnchorNode(parent))
            selectNode(model);
    }

    /*Checks if the node is AnchorNode*/
    private boolean isAnchorNode(Node node){
        return node instanceof AnchorNode;
    }

    public boolean isPlaceObjectOverObject() {
        return placeObjectOverObject;
    }

    public void setPlaceObjectOverObject(boolean placeObjectOverObject) {
        this.placeObjectOverObject = placeObjectOverObject;
    }

    public Stack<TransformableNode> getNodesSelected() {
        return nodesSelected;
    }

    /*Detaches model from the parent*/
    public void deleteModel(){
        if(nodesSelected.isEmpty()) {
            Toast.makeText(mContext, "No node selected", Toast.LENGTH_SHORT).show();
            return;
        }
        AnchorNode node;
        TransformableNode nodeToBeDeleted = nodesSelected.pop();
        nodesSelected.clear();
        ((MainActivity)mContext).showModelButtons(false);
        Node n =  nodeToBeDeleted.getParent();
        assert n != null;
        if(n instanceof AnchorNode) {
            Log.d("Delete Model", "deleteModel: Anchor node");
            node = (AnchorNode) n;
            Objects.requireNonNull(node.getAnchor()).detach();
        }
        if(n instanceof TransformableNode){
            Log.d("Delete Model", "deleteModel: Transformable node");
            nodeToBeDeleted.setParent(null);
        }
        else if(!(n instanceof Camera) && !(n instanceof Sun)){
            Log.d("Delete Model", "deleteModel: Other node");
            n.setParent(null);
        }
    }

    /*Move axis in verical directions with the arrow buttons shown when we select the model.*/
    public void moveModelInYAxis(View view){
        TransformableNode node = getNodesSelected().peek();
        Vector3 position =node.getLocalPosition();
        if(view.getId() == R.id.moveDownInYAxisIV){
            position.y -= 0.01f;
        }else {
            position.y += 0.01f;
        }
        node.setLocalPosition(position);
    }
}
