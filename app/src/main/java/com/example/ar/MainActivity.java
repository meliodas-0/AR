package com.example.ar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ar.Helper.Model;
import com.example.ar.Helper.ModelAdapter;
import com.example.ar.Helper.ModelHelper;
import com.example.ar.Helper.Enums.SHAPE;
import com.google.android.filament.ColorGrading;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.Sun;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.CameraStream;
import com.google.ar.sceneform.rendering.EngineInstance;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.Renderer;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        ArFragment.OnViewCreatedListener,
        BaseArFragment.OnSessionConfigurationListener {
    public ArFragment arFragment;
    public Renderable model;
    public int modelNumber;
    public TextView modelNameTextView;
    public CircleImageView modelImageView;
    private ImageView menuImageView, deleteButtonIV, upInYAxisIV, downInYAxisIV;
    private ArSceneView arSceneView;

    public ProgressDialog pd;
    private JoystickView joystick;
    public Dialog modelSelectingDialog;
    private final CharSequence[] distanceBetweenArray = new CharSequence[]{"Object to Object", "Plane to object", "Plane to plane"};
    private final CharSequence[] depthModes = new CharSequence[]{"Raw Depth", "Automatic ", "Disabled"};

    private int checkedItemInDepthMode;

    private final Stack<Plane> planeList = new Stack<>();

    private Button measureButton;

    private DISTANCE_TO_FIND_BETWEEN distanceToFindBetween = DISTANCE_TO_FIND_BETWEEN.OBJECT_TO_OBJECT;
    private boolean checkingDistance = false;

    public ModelHelper modelHelper;
    private Config.DepthMode depthMode = Config.DepthMode.DISABLED;

    private enum DISTANCE_TO_FIND_BETWEEN {
        OBJECT_TO_OBJECT,
        PLANE_TO_OBJECT,
        PLANE_TO_PLANE

    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(this.depthMode)) {
            config.setDepthMode(this.depthMode);
        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addFragmentOnAttachListener(this);
        menuImageView = findViewById(R.id.mainActivityMenu);
        modelNameTextView = findViewById(R.id.modelNameTV);
        modelImageView = findViewById(R.id.ModelPhotoIV);
        deleteButtonIV = findViewById(R.id.deleteModelIV);
        upInYAxisIV = findViewById(R.id.moveUPInYAxis);
        downInYAxisIV = findViewById(R.id.moveDownInYAxisIV);
        measureButton = findViewById(R.id.distanceMeasuringButton);
        joystick = findViewById(R.id.joystick);

        modelHelper = new ModelHelper(this, getModelsList());
        pd = new ProgressDialog(this);
        pd.setMessage("Please wait, loading model");
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }
        modelHelper.loadModel( 0);
        menuImageView.setOnClickListener(
                v -> {
                    PopupMenu menu = new PopupMenu(this, v);
                    menu.setOnMenuItemClickListener(this::onOptionsItemSelected);
                    menu.inflate(R.menu.model_menu);
                    menu.show();
                }
        );
        modelSelectingDialog = new Dialog(this);

        modelSelectingDialog.setContentView(R.layout.alert_dialog_selection_view);
        RecyclerView recyclerView = modelSelectingDialog.findViewById(R.id.alertRecylerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        ModelAdapter modelAdapter = new ModelAdapter(this, modelHelper.getModelList());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(modelAdapter);
    }

    private List<Model> getModelsList() {
List<Model> models = new ArrayList<>();
        models.add(new Model(this, "Sphere", true, SHAPE.SPHERE, R.drawable.sphere));
        models.add(new Model(this, "Cube", true, SHAPE.CUBE, R.drawable.cube));
        models.add(new Model(this, "Cylinder", true, SHAPE.CYLINDER, R.drawable.cylinder));
        models.add(new Model(this, "Tiger", "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb", -1));
        models.add(new Model(this, "sofa", "sofa.glb", -1));
        models.add(new Model(this, "Cabinet", "Cabinet/scene.gltf", R.drawable.cabinet));
        models.add(new Model(this, "Bee", "Bee.glb", -1));
        models.add(new Model(this, "desk", "desk.glb", R.drawable.desk));
        models.add(new Model(this, "Lamp", "Lamp.glb", R.drawable.lamp));
        models.add(new Model(this, "book", "Book.glb", -1));
        return models;
    }

    public void showModelButtons(boolean enable) {
        deleteButtonIV.setVisibility(enable ? View.VISIBLE : View.GONE);
        downInYAxisIV.setVisibility(enable ? View.VISIBLE : View.GONE);
        upInYAxisIV.setVisibility(enable ? View.VISIBLE : View.GONE);
        joystick.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void selectPlane(Plane plane) {
        if (planeList.isEmpty() || !planeList.peek().equals(plane))
            planeList.push(plane);
    }

    private float measureObjectToObjectDistance(Vector3 startPose, Vector3 endPose) {
        float distance;
        float dx = startPose.x - endPose.x;
        float dy = startPose.y - endPose.y;
        float dz = startPose.z - endPose.z;

        distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance;
    }

    private float measureObjectToPlaneDistance(Vector3 objectPose, Pose planePose) {

        float distance;
        float dx = objectPose.x - planePose.tx();
        float dy = objectPose.y - planePose.ty();
        float dz = objectPose.z - planePose.tz();

        distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance;
    }

    private float measurePlaneToPlaneDistance(Pose plane1pose, Pose plane2Pose) {
        float distance;
        float dx = plane1pose.tx() - plane2Pose.tx();
        float dy = plane1pose.ty() - plane2Pose.ty();
        float dz = plane1pose.tz() - plane2Pose.tz();

        distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance;
    }

    public void checkDistance(View view) {
        String result = "";
        Stack<TransformableNode> nodesSelected = modelHelper.getNodesSelected();

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
                if (planeList.size() < 2) result = "Select Two Planes.";
                else {
                    Plane plane1 = planeList.pop();
                    Plane plane2 = planeList.peek();
                    planeList.add(plane1);
                    result = "Distance between planes is : " + measurePlaneToPlaneDistance(plane1.getCenterPose(), plane2.getCenterPose()) + "m";
                }
                break;
            case PLANE_TO_OBJECT:
                if (planeList.isEmpty() || !nodesSelected.isEmpty())
                    result = "Either plane or object is not selected.";
                else {
                    result = "Distance : " + measureObjectToPlaneDistance(nodesSelected.peek().getLocalPosition(), planeList.peek().getCenterPose()) + "m";
                }
        }

        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
    }

    public void changeMeasurementTargets() {
//        Intent intent = new Intent(this, GLSurfaceActivity.class);
//        startActivity(intent);

        switch (distanceToFindBetween) {
            case OBJECT_TO_OBJECT:
                measureButton.setText(R.string.object_to_object_distance);
                break;
            case PLANE_TO_OBJECT:
                measureButton.setText(R.string.object_to_plane_distance);
                break;
            case PLANE_TO_PLANE:
                measureButton.setText(R.string.plane_to_plane_distance);
                break;
        }

    }

    public void moveModel(int angle , int strength){
        TransformableNode node = modelHelper.getNodesSelected().peek();
        //TODO: This needs some calibration this is not perfect.
            double a = Math.toRadians(angle);
            Vector3 cameraPosition = arSceneView.getScene().getCamera().getLocalPosition();
            double str = strength * 0.0001;
            Vector3 position = node.getLocalPosition();
            cameraPosition = Vector3.subtract(position, cameraPosition);

            //Get the angle of the camera and move the object according to that angle this needs calibration the most.
            a += Math.acos(Math.sqrt(cameraPosition.x * cameraPosition.x / (cameraPosition.x * cameraPosition.x + cameraPosition.z * cameraPosition.z))) + Math.toRadians(-90);

            position.x += str * Math.cos(a);
            position.z -= str * Math.sin(a);
        node.setLocalPosition(position);
    }
    public void moveModelInYAxis(View view){
        TransformableNode node = modelHelper.getNodesSelected().peek();
        Vector3 position =node.getLocalPosition();
        if(view.getId() == R.id.moveDownInYAxisIV){
            position.y -= 0.01f;
        }else {
            position.y += 0.01f;
        }
        node.setLocalPosition(position);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        measureButton.setVisibility(View.GONE);
        distanceToFindBetween = DISTANCE_TO_FIND_BETWEEN.OBJECT_TO_OBJECT;
        checkingDistance = false;

        switch (item.getItemId()) {

            case R.id.models_option:
                launchModelsSelectionMenuDialog();
                return true;
            case R.id.find_distance:
                measureButton.setVisibility(View.VISIBLE);
                checkingDistance = true;
                launchDistanceSelectionMenuDialog();
                return true;
            case R.id.changeDepthMode:
                launchChangeDepthModeSelectionMenuDialog();
                return true;
            case R.id.togglePlacementOverObject:
                modelHelper.setPlaceObjectOverObject(!modelHelper.isPlaceObjectOverObject());
                Toast.makeText(this, "Placement over object set to " + modelHelper.isPlaceObjectOverObject(), Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchChangeDepthModeSelectionMenuDialog() {
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(depthModes, checkedItemInDepthMode, (dialog, which) -> {
                    String depth = "D";
                    switch (which) {
                        case 0: depthMode = Config.DepthMode.RAW_DEPTH_ONLY;
                        depth = "R";
                            break;

                        case 1: depthMode = Config.DepthMode.AUTOMATIC;
                        depth = "A";
                            break;

                        case 2 : depthMode = Config.DepthMode.DISABLED;
                    }
                    SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                    editor.putString("DepthMode", depth);
                    editor.commit();
                    editor.apply();
                    Toast.makeText(this, "Please Restart the app to see changes", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }).create().show();
    }

    private void launchDistanceSelectionMenuDialog() {
        Toast.makeText(this, "You might not be able to place objects while in this mode. If you want to change the distance please select model from menu.  ", Toast.LENGTH_LONG).show();
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(distanceBetweenArray, checkedItemInDepthMode, (dialog, which) -> {
                    switch (distanceBetweenArray[which].toString()) {
                        case "Object to Object":
                            distanceToFindBetween = DISTANCE_TO_FIND_BETWEEN.OBJECT_TO_OBJECT;
                            break;
                        case "Plane to object":
                            distanceToFindBetween = DISTANCE_TO_FIND_BETWEEN.PLANE_TO_OBJECT;
                            break;
                        case "Plane to plane":
                            distanceToFindBetween = DISTANCE_TO_FIND_BETWEEN.PLANE_TO_PLANE;
                            break;
                    }
                    changeMeasurementTargets();
                    dialog.dismiss();
                }).create().show();
    }

    private void launchModelsSelectionMenuDialog() {


        modelSelectingDialog.show();

    }
    public void deleteModel(View view){
        Stack<TransformableNode> nodesSelected = modelHelper.getNodesSelected();
        if(nodesSelected.isEmpty()) {
            Toast.makeText(this, "No node selected", Toast.LENGTH_SHORT).show();
            return;
        }
        AnchorNode node;
        TransformableNode nodeToBeDeleted = nodesSelected.pop();
        nodesSelected.clear();
        showModelButtons(false);
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

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnTapArPlaneListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnSessionConfigurationListener(this);
        }

    }

    @Override
    public void onViewCreated(ArFragment arFragment, ArSceneView arSceneView) {
        Renderer renderer = arSceneView.getRenderer();

        if (renderer != null) {
            renderer.getFilamentView().setColorGrading(
                    new ColorGrading.Builder()
                            .toneMapping(ColorGrading.ToneMapping.LINEAR)
                            .build(EngineInstance.getEngine().getFilamentEngine())
            );
        }
        this.arSceneView = arSceneView;
        arSceneView.getCameraStream()
                .setDepthOcclusionMode(CameraStream.DepthOcclusionMode.DEPTH_OCCLUSION_ENABLED);
        joystick.setOnMoveListener(this::moveModel);

    }
    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

        if (!checkingDistance) {
            if (model == null) {
                Toast.makeText(this, "Select Model" + model, Toast.LENGTH_SHORT).show();
                return;
            }
            try{// Create the Anchor.
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());
                modelHelper.PlaceModel(/*parent*/anchorNode);

            }catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            selectPlane(plane);
        }
    }

    @Override
    protected void onResume() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        switch (preferences.getString("DepthMode", "D")){
            case "A" : this.depthMode = Config.DepthMode.AUTOMATIC; checkedItemInDepthMode = 0;break;
            case "R" : this.depthMode = Config.DepthMode.RAW_DEPTH_ONLY;
            checkedItemInDepthMode = 1; break;
            default : this.depthMode = Config.DepthMode.DISABLED;
            checkedItemInDepthMode = 2;
        }
        super.onResume();
    }
}