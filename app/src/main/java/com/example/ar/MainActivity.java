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

import com.example.ar.Helper.DistanceHelper;
import com.example.ar.Helper.Enums.DISTANCE_TO_FIND_BETWEEN;
import com.example.ar.Helper.JoystickHelper;
import com.example.ar.Helper.Model;
import com.example.ar.Helper.ModelAdapter;
import com.example.ar.Helper.ModelHelper;
import com.example.ar.Helper.Enums.SHAPE;
import com.google.android.filament.ColorGrading;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
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
/*Initial Activity*/
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
    private ImageView deleteButtonIV;
    private ImageView upInYAxisIV;
    private ImageView downInYAxisIV;
    public ProgressDialog pd;
    private JoystickView joystick;
    public Dialog modelSelectingDialog;
    public Button measureButton;
    /*THESE ARE FOR SELECTION DIALOG BOXES GIVEN BELOW*/
    private final CharSequence[] distanceBetweenArray = new CharSequence[]{"Object to Object", "Plane to object", "Plane to plane"};
    private final CharSequence[] depthModes = new CharSequence[]{"Raw Depth", "Automatic ", "Disabled"};

    private int checkedItemInDepthMode;
    private int checkedItemInDistanceMode = -1;
    private boolean checkingDistance = false;

    public final Stack<Plane> planeStack = new Stack<>();

    private DistanceHelper distanceHelper;
    public ModelHelper modelHelper;
    private JoystickHelper joystickHelper;

    private Config.DepthMode depthMode = Config.DepthMode.DISABLED;

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
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }
        /*Initializers*/
        getSupportFragmentManager().addFragmentOnAttachListener(this);
        ImageView menuImageView = findViewById(R.id.mainActivityMenu);
        modelNameTextView = findViewById(R.id.modelNameTV);
        modelImageView = findViewById(R.id.ModelPhotoIV);
        deleteButtonIV = findViewById(R.id.deleteModelIV);
        upInYAxisIV = findViewById(R.id.moveUPInYAxis);
        downInYAxisIV = findViewById(R.id.moveDownInYAxisIV);
        measureButton = findViewById(R.id.distanceMeasuringButton);
        joystick = findViewById(R.id.joystick);
        distanceHelper = new DistanceHelper(this);

        modelHelper = new ModelHelper(this, /*Models*/getModelsList());
        joystickHelper = new JoystickHelper(this);
        pd = new ProgressDialog(this);
        modelHelper.loadModel( 0);
        pd.setMessage("Please wait, loading model");
        setModelDialogBox();

        /*OnclickListeners*/
        measureButton.setOnClickListener(view -> distanceHelper.checkDistance());
        menuImageView.setOnClickListener(
                v -> {
                    PopupMenu menu = new PopupMenu(this, v);
                    menu.setOnMenuItemClickListener(this::onOptionsItemSelected);
                    menu.inflate(R.menu.model_menu);
                    menu.show();
                }
        );
        deleteButtonIV.setOnClickListener(view -> modelHelper.deleteModel());
        upInYAxisIV.setOnClickListener(view ->
                modelHelper.moveModelInYAxis(view));
        downInYAxisIV.setOnClickListener(view ->
                modelHelper.moveModelInYAxis(view));
    }
/* SETTING ALL THE PARAMETERS FOR THE MODELS DIALOG*/
    private void setModelDialogBox() {
        modelSelectingDialog = new Dialog(this);

        modelSelectingDialog.setContentView(R.layout.alert_dialog_selection_view);
        RecyclerView recyclerView = modelSelectingDialog.findViewById(R.id.alertRecylerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        ModelAdapter modelAdapter = new ModelAdapter(this, modelHelper.getModelList());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(modelAdapter);
    }
//TODO: add models from online catalog.
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
/*CONTROL BUTTONS OF THE MODEL*/
    public void showModelButtons(boolean enable) {
        deleteButtonIV.setVisibility(enable ? View.VISIBLE : View.GONE);
        downInYAxisIV.setVisibility(enable ? View.VISIBLE : View.GONE);
        upInYAxisIV.setVisibility(enable ? View.VISIBLE : View.GONE);
        joystick.setVisibility(enable ? View.VISIBLE : View.GONE);
    }
/*USED TO SELECT PLANE WHOSE DISTANCE TO BE CALCULATED.*/
    private void selectPlane(Plane plane) {
        if (planeStack.isEmpty() || !planeStack.peek().equals(plane))
            planeStack.push(plane);
    }

    /*Menu Options Launcher*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        measureButton.setVisibility(View.GONE);
        distanceHelper.setDistanceToFindBetween(DISTANCE_TO_FIND_BETWEEN.OBJECT_TO_OBJECT);
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
                .setSingleChoiceItems(distanceBetweenArray, checkedItemInDistanceMode, (dialog, which) -> {
                    switch (distanceBetweenArray[which].toString()) {
                        case "Object to Object":
                            distanceHelper.setDistanceToFindBetween(DISTANCE_TO_FIND_BETWEEN.OBJECT_TO_OBJECT);
                            break;
                        case "Plane to object":
                            distanceHelper.setDistanceToFindBetween(DISTANCE_TO_FIND_BETWEEN.PLANE_TO_OBJECT);
                            break;
                        case "Plane to plane":
                            distanceHelper.setDistanceToFindBetween(DISTANCE_TO_FIND_BETWEEN.PLANE_TO_PLANE);
                            break;
                    }
                    distanceHelper.changeMeasurementTargets();
                    dialog.dismiss();
                }).create().show();
    }

    private void launchModelsSelectionMenuDialog() {


        modelSelectingDialog.show();

    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnTapArPlaneListener(this);
            arFragment.setOnViewCreatedListener(this);
            /*NECESSARY TO ENABLE ANY DEPTH MODE.*/
            arFragment.setOnSessionConfigurationListener(this);
        }

    }

    /*THIS METHOD WILL INITIALISE THE APP
    * RENDERER WILL BE ATTACHED WILL COLOR GRADING
    * HERE ColorGrading.ToneMapping should be LINEAR or FILMIC for better results*/

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
        /*This line will try to set the model according to the depth of the model
        * according the environment.
        * If the depthMode is set to Config.DepthMode.Disabled this will not work because depth will not be capture hence will not render any difference.
        * Use this with Config.DepthMode.AUTOMATIC or Config.DepthMode.RAW_DEPTH_ONLY*/
        arSceneView.getCameraStream()
                .setDepthOcclusionMode(CameraStream.DepthOcclusionMode.DEPTH_OCCLUSION_ENABLED);

        joystick.setOnMoveListener((angle, strength) -> joystickHelper.moveModel(angle, strength, /* Camera Position */arSceneView.getScene().getCamera().getLocalPosition()));

    }

    /* THIS METHOD WILL PLACE OBJECT ON THE PLANE OR USED TO FIND DISTANCE BETWEEN PLANES.*/
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