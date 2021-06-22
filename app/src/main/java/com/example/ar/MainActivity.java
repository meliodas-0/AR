package com.example.ar;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.CameraStream;
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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        ArFragment.OnViewCreatedListener ,
        BaseArFragment.OnSessionConfigurationListener
{
//TODO: Add check box whether to find the distance or to place the objects. Then accordingly Do the needy.
    private ArFragment arFragment;
    private Renderable model;
    private ViewRenderable viewRenderable;
    private Map<String, String> models;
    private String uri;
    private ImageView menuImageView;
    private TransformableNode first, second;
    private boolean shapeSelected = true;

    private TouchHelper touchHelper;

    private Button measureButton;

    private SHAPE shapeForm = SHAPE.CUBE;

    private DISTANCE_TO_FIND_BETWEEN distanceToFindBetween = DISTANCE_TO_FIND_BETWEEN.OBJECT_TO_OBJECT;

    private enum SHAPE {
        CUBE,
        SPHERE,
        CYLINDER;

    }
    private enum DISTANCE_TO_FIND_BETWEEN{
        OBJECT_TO_OBJECT,
        PLANE_TO_OBJECT,
        PLANE_TO_PLANE;

    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if(session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)){
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
    }

    private void selectNode(TransformableNode node) {
        first = second;
        second = node;
        if (first != null) {
            second.select();
            first.select();
        }
    }

    private float measureDistance(Vector3 startPose, Vector3 endPose){
        float distance;
        float dx = startPose.x - endPose.x;
        float dy = startPose.y - endPose.y;
        float dz = startPose.z - endPose.z;

        distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance;
    }

    public void checkDistance(View view) {
        String result = "";

        switch (distanceToFindBetween){
            case OBJECT_TO_OBJECT:
                if (first == null || second == null) {
                    result = "Select two nodes";
                } else {

                    float distance = measureDistance(first.getWorldPosition(), second.getWorldPosition());

                    result = "distance between the selected nodes is" + distance + "m";

                }
                break;
//                TODO: Add more cases here
        }

        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
    }

    public void changeMeasurementTargets(View view){
//        Intent intent = new Intent(this, GLSurfaceActivity.class);
//        startActivity(intent);

        switch (distanceToFindBetween){
            case OBJECT_TO_OBJECT: distanceToFindBetween = DISTANCE_TO_FIND_BETWEEN.PLANE_TO_OBJECT;
                measureButton.setText(R.string.object_to_plane_distance);break;
            case PLANE_TO_OBJECT: distanceToFindBetween = DISTANCE_TO_FIND_BETWEEN.PLANE_TO_PLANE;
                measureButton.setText(R.string.plane_to_plane_distance);break;
            case PLANE_TO_PLANE: distanceToFindBetween = DISTANCE_TO_FIND_BETWEEN.OBJECT_TO_OBJECT;
                measureButton.setText(R.string.object_to_object_distance);break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.model_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.mewtwoMenuItem:
                this.uri = models.get("mewtwo");
                shapeSelected = false;
                loadModels();
                Toast.makeText(this, "mewtwo selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.tigerMenuItem:
                this.uri = models.get("tiger");
                shapeSelected = false;
                loadModels();
                Toast.makeText(this, "tiger selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.sofaMenuItem:
                this.uri = models.get("sofa");
                shapeSelected = false;
                loadModels();
                Toast.makeText(this, "sofa selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.woodentable:
                this.uri = models.get("woodentable");
                shapeSelected = false;
                loadModels();
                Toast.makeText(this, "wooden table selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.cube:
                this.shapeForm = SHAPE.CUBE;
                makeObject();
                shapeSelected = true;
                Toast.makeText(this, "cube selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.Cylinder:
                this.shapeForm = SHAPE.CYLINDER;
                makeObject();
                shapeSelected = true;
                Toast.makeText(this, "Cylinder selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.Sphere:
                this.shapeForm = SHAPE.SPHERE;
                makeObject();
                shapeSelected = true;
                Toast.makeText(this, "Sphere selected", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fillModels() {
        this.models = new HashMap<>();

        models.put("mewtwo", "mewtwo.glb");
        models.put("tiger", "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb");
        models.put("sofa", "sofa.glb");
        models.put("woodentable", "woodentable/scene.gltf");
        models.put("diningtable", "diningtable/scene.gltf");
        this.shapeForm = SHAPE.SPHERE;
        makeObject();
    }

    private void makeObject() {
        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);
        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.BLUE))
                .thenAccept(material -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null)
                        switch (activity.shapeForm) {
                            case CUBE:
                                activity.model = ShapeFactory.makeCube(new Vector3(0.1f, 0.1f, 0.1f), new Vector3(0f, 0f, 0f), material);
                                return;
                            case SPHERE:
                                activity.model = ShapeFactory.makeSphere(0.1f, new Vector3(0f, 0f, 0f), material);
                                return;
                            default:
                                activity.model = ShapeFactory.makeCylinder(0.1f, 0.05f, new Vector3(0f, 0f, 0f), material);
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
                            .toneMapping(ColorGrading.ToneMapping.FILMIC)
                            .build(EngineInstance.getEngine().getFilamentEngine())
            );
        }

        arSceneView.getCameraStream()
                .setDepthOcclusionMode(CameraStream.DepthOcclusionMode.DEPTH_OCCLUSION_ENABLED);

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
        if (model == null || viewRenderable == null) {
            Toast.makeText(this, "Loading..." + model + viewRenderable, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(this.model);
        Objects.requireNonNull(model.getRenderableInstance()).animate(true).start();
        model.select();
        model.setOnTouchListener((hitTestResult, ev) -> {
            if(touchHelper.triggerEvent(ev)) selectNode(model);
            return true;
        });



        /*Node tigerTitleNode = new Node();
        tigerTitleNode.setParent(model);
        tigerTitleNode.setEnabled(false);
        tigerTitleNode.setLocalPosition(new Vector3(0.0f, 0.01f, 0.0f));
        tigerTitleNode.setRenderable(viewRenderable);
        tigerTitleNode.setEnabled(true);
        loadModels();*/
    }
}