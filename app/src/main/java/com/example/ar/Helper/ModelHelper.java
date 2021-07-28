package com.example.ar.Helper;

import android.content.Context;

import com.example.ar.MainActivity;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.List;
import java.util.Stack;

public class ModelHelper {
    private final List<Model> modelList;
private  final Context mContext;
private boolean placeObjectOverObject = false;
private final TouchHelper touchHelper;
    private final SnackbarHelper snackbarHelper;
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

    private boolean isAnchorNode(Node node){
        return node instanceof AnchorNode;
    }

    public CharSequence[] getModelsNameList(){
        CharSequence[] list = new CharSequence[modelList.size()];
        for(int i = 0; i<modelList.size(); i++){
            Model model = modelList.get(i);
            list[i] = model.getName();
        }
        return  list;
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
}
