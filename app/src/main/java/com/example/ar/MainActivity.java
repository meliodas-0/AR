package com.example.ar;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.example.ar.Helper.TouchHelper;
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
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.EngineInstance;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.Renderer;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TranslationController;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        ArFragment.OnViewCreatedListener,
        BaseArFragment.OnSessionConfigurationListener {
    private ArFragment arFragment;
    private Renderable model;
    private ViewRenderable viewRenderable;
    private Map<CharSequence, String> models;
    private String uri;
    private ImageView menuImageView;
    private boolean shapeSelected = true;
    private TranslationController translationController;
    Stack<TransformableNode> nodesSelected = new Stack<>();

    private CharSequence[] modelNameArray;
    private CharSequence[] distanceBetweenArray = new CharSequence[]{"Object to Object", "Plane to object", "Plane to plane"};

    private int checkedItemInDistanceBetweenArray = 0;

    private Stack<Plane> planeList = new Stack<>();

    private TouchHelper touchHelper;

    private Button measureButton;

    private SHAPE shapeForm = SHAPE.CUBE;

    private DISTANCE_TO_FIND_BETWEEN distanceToFindBetween = DISTANCE_TO_FIND_BETWEEN.OBJECT_TO_OBJECT;
    private int checkedItemInModelNameArray = 0;
    private boolean checkingDistance = false;

    private enum SHAPE {
        CUBE,
        SPHERE,
        CYLINDER;

    }

    private enum DISTANCE_TO_FIND_BETWEEN {
        OBJECT_TO_OBJECT,
        PLANE_TO_OBJECT,
        PLANE_TO_PLANE;

    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addFragmentOnAttachListener(this);
        menuImageView = findViewById(R.id.mainActivityMenu);
        measureButton = findViewById(R.id.distanceMeasuringButton);

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }
        fillModels();
        loadModels();
        touchHelper = new TouchHelper();
        menuImageView.setOnClickListener(
                v -> {
                    PopupMenu menu = new PopupMenu(this, v);
                    menu.setOnMenuItemClickListener(this::onOptionsItemSelected);
                    menu.inflate(R.menu.model_menu);
                    menu.show();
                }
        );
//        configureSelectedViewRenderables();
    }

    private void configureSelectedViewRenderables() {
        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);
        final ViewRenderable[] v = new ViewRenderable[1];
        ViewRenderable.builder()
                .setView(this, R.layout.title_card)
                .build()
                .thenAccept(viewRenderable -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        v[0] = viewRenderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });

//        selectedOne = new Node();
//        selectedOne.setEnabled(false);
//        selectedOne.setRenderable(v[0]);
//        selectedTwo = new Node();
//        selectedTwo.setEnabled(false);
//        selectedTwo.setRenderable(v[0]);
    }

    private void selectNode(TransformableNode node) {
//        if (distanceToFindBetween == DISTANCE_TO_FIND_BETWEEN.PLANE_TO_PLANE) {
//            Toast.makeText(this, "Select Plane not object", Toast.LENGTH_SHORT).show();
//            selectedOne.setEnabled(false);
//            selectedTwo.setEnabled(false);
//            return;
//        }
//        else if(distanceToFindBetween == DISTANCE_TO_FIND_BETWEEN.PLANE_TO_OBJECT){
//            selectedOne.setEnabled(false);
//            selectedTwo.setEnabled(false);
//            selectedOne.setParent(node);
//            selectedOne.setEnabled(true);
//        }

        if(nodesSelected.contains(node)){
            nodesSelected.remove(node);
        }
        nodesSelected.push(node);
        node.select();
        translationController = node.getTranslationController();

//        selectedOne.setParent(second);
//        selectedOne.setLocalPosition(new Vector3(0, 0.1f, 0));
//        selectedOne.setEnabled(true);
//        if (first != null) {
//            second.select();
//            first.select();
//            selectedTwo.setParent(second);
//            selectedTwo.setLocalPosition(new Vector3(0, 0.1f, 0));
//            selectedTwo.setEnabled(true);
//        }
    }

    private void selectPlane(Plane plane) {
        if (planeList.isEmpty() || !planeList.peek().equals(plane))
            planeList.push(plane);
    }

    public void changeModelDistanceFromGround (View view){
        if(nodesSelected.isEmpty()){
                Toast.makeText(this, "Select a node", Toast.LENGTH_SHORT).show();
                return;
        }
        TransformableNode node = nodesSelected.peek();
        Vector3 pos = node.getLocalPosition();
        if(view.getId() == R.id.decreaseSizeIV){
            pos.y -= 0.1;
        }else{
            pos.y += 0.1;
        }
        node.setLocalPosition(pos);
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


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.models_option:
                measureButton.setVisibility(View.GONE);
                distanceToFindBetween = DISTANCE_TO_FIND_BETWEEN.OBJECT_TO_OBJECT;
                checkingDistance = false;
                launchModelsSelectionMenuDialog();
                return true;
            case R.id.find_distance:
                measureButton.setVisibility(View.VISIBLE);
                checkingDistance = true;
                launchDistanceSelectionMenuDialog();
                return true;
//            case R.id.mewtwoMenuItem:
//                this.uri = models.get("mewtwo");
//                shapeSelected = false;
//                loadModels();
//                Toast.makeText(this, "mewtwo selected", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.tigerMenuItem:
//                this.uri = models.get("tiger");
//                shapeSelected = false;
//                loadModels();
//                Toast.makeText(this, "tiger selected", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.sofaMenuItem:
//                this.uri = models.get("sofa");
//                shapeSelected = false;
//                loadModels();
//                Toast.makeText(this, "sofa selected", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.woodentable:
//                this.uri = models.get("woodentable");
//                shapeSelected = false;
//                loadModels();
//                Toast.makeText(this, "wooden table selected", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.cube:
//                this.shapeForm = SHAPE.CUBE;
//                makeObject();
//                shapeSelected = true;
//                Toast.makeText(this, "cube selected", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.Cylinder:
//                this.shapeForm = SHAPE.CYLINDER;
//                makeObject();
//                shapeSelected = true;
//                Toast.makeText(this, "Cylinder selected", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.Sphere:
//                this.shapeForm = SHAPE.SPHERE;
//                makeObject();
//                shapeSelected = true;
//                Toast.makeText(this, "Sphere selected", Toast.LENGTH_SHORT).show();
//                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchDistanceSelectionMenuDialog() {
        Toast.makeText(this, "You might not be able to place objects while in this mode. If you want to change the distance please select model from menu.  ", Toast.LENGTH_LONG).show();
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(distanceBetweenArray, checkedItemInDistanceBetweenArray, (dialog, which) -> {
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
        new AlertDialog.Builder(this)
                .setTitle("Select Models")
                .setSingleChoiceItems(modelNameArray, checkedItemInModelNameArray, (dialog, which) -> {
                    checkedItemInModelNameArray = which;
                    this.uri = models.get(modelNameArray[which]);
                    switch (modelNameArray[which].toString()) {
                        case "Sphere":
                            this.shapeForm = SHAPE.SPHERE;
                            shapeSelected = true;
                            break;
                        case "Cube":
                            this.shapeForm = SHAPE.CUBE;
                            shapeSelected = true;
                            break;
                        case "Cylinder":
                            this.shapeForm = SHAPE.CYLINDER;
                            shapeSelected = true;
                            break;
                        default:
                            shapeSelected = false;
                    }
                    loadModels();
                    dialog.dismiss();
                }).create().show();
    }
    public void deleteModel(View view){
        if(nodesSelected.isEmpty()) return;
        AnchorNode node;
        Node n =  nodesSelected.pop().getParent();
        if(n instanceof AnchorNode) {
            node = (AnchorNode) n;
            node.getAnchor().detach();
        }
        if(!(n instanceof Camera) && !(n instanceof Sun)){
            n.setParent(null);
        }
    }

    private void fillModels() {
        this.models = new HashMap<>();

        models.put("mewtwo", "mewtwo.glb");
        models.put("tiger", "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb");
        models.put("sofa", "sofa.glb");
        models.put("woodentable", "woodentable/scene.gltf");
        models.put("Sphere", "");
        models.put("Cube", "");
        models.put("Cylinder", "");
        models.put("Cabinet", "Cabinet/scene.gltf");
        models.put("Cabinet 2" , "otherCabinet/scene.gltf");
//        models.put("3Model","3M/3_model.glb");
//        models.put("3_model (1)", "3M/3_model (1).gltf");
        modelNameArray = new CharSequence[models.size()];
        System.arraycopy(models.keySet().toArray(modelNameArray), 0, modelNameArray, 0, models.size());
        this.shapeForm = SHAPE.SPHERE;
        makeObject();
    }

    private void makeObject() {
        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);
        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
                .thenAccept(material -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null)
                        switch (activity.shapeForm) {
                            case CUBE:
                                activity.model = ShapeFactory.makeCube(new Vector3(0.1f, 0.1f, 0.1f), new Vector3(0f, 0.1f, 0f), material);
                                return;
                            case SPHERE:
                                activity.model = ShapeFactory.makeSphere(0.1f, new Vector3(0f, 0.1f, 0f), material);
                                return;
                            default:
                                activity.model = ShapeFactory.makeCylinder(0.1f, 0.05f, new Vector3(0f, 0.025f, 0f), material);
                        }
                });

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
/*
        arSceneView.getCameraStream()
                .setDepthOcclusionMode(CameraStream.DepthOcclusionMode.DEPTH_OCCLUSION_ENABLED);*/

    }

    public void loadModels() {
        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);
        if (shapeSelected) makeObject();
        else
            ModelRenderable.builder()
                    .setSource(this, Uri.parse(this.uri))
                    .setIsFilamentGltf(true)
                    .setAsyncLoadEnabled(true)
                    .build()
                    .thenAccept(model -> {
                        MainActivity activity = weakActivity.get();
                        if (activity != null) {
                            activity.model = model;
                        }
                    })
                    .exceptionally(throwable -> {
                        Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                        return null;
                    });
        ViewRenderable.builder()
                .setView(this, R.layout.title_card)
                .build()
                .thenAccept(viewRenderable -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

        if (!checkingDistance) {
            if (model == null || viewRenderable == null) {
                Toast.makeText(this, "Loading..." + model + viewRenderable, Toast.LENGTH_SHORT).show();
                return;
            }

            try{// Create the Anchor.
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());

                // Create the transformable model and add it to the anchor.
                TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
                model.setParent(anchorNode);
                model.setRenderable(this.model);
//            Objects.requireNonNull(model.getRenderableInstance()).animate(true).start();
                model.select();
                model.setOnTouchListener((hitTestResult, ev) -> {
                    if (touchHelper.triggerEvent(ev)) selectNode(model);
                    return true;
                });
                if(this.uri != null && this.uri.contains("wooden")) {
                    Toast.makeText(this, this.uri, Toast.LENGTH_SHORT).show();
                    model.getScaleController().setMaxScale(0.05f);
                    model.getScaleController().setMinScale(0.01f);
                }
            }catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } else {
            selectPlane(plane);
        }
        /*Node tigerTitleNode = new Node();
        tigerTitleNode.setParent(model);
        tigerTitleNode.setEnabled(false);
        tigerTitleNode.setLocalPosition(new Vector3(0.0f, 0.01f, 0.0f));
        tigerTitleNode.setRenderable(viewRenderable);
        tigerTitleNode.setEnabled(true);
        loadModels();*/
    }
}