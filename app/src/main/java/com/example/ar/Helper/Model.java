package com.example.ar.Helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.ar.Helper.Enums.SHAPE;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

public class Model {
    private final String name;
    private String uri;
    private boolean isShape = false;
    private SHAPE shape = null;
    private ModelRenderable modelRenderable = null;
    private final Context mContext;
    private final int drawable;

    /*Use this if the model is glb*/
    public Model(Context context, String name, String uri, int drawable) {
        this.name = name;
        this.uri = uri;
        this.mContext = context;
        this.drawable = drawable;
        makeModel();
    }
    /*Use this if the model is to be made in the app ex. sphere */
    public Model(Context context, String name, boolean isShape, SHAPE shape, int drawable){
        this.name = name;
        this.mContext = context;
        this.isShape = isShape;
        if(isShape) {
            this.shape = shape;
        }
        this.drawable = drawable;
        makeModel();
    }

    public String getName() {
        return name;
    }

    public boolean isShape() {
        return isShape;
    }

    public SHAPE getShape() {
        return shape;
    }

    public int getDrawable() {
        return drawable;
    }

    public ModelRenderable getModelRenderable(){return modelRenderable;}
    private void makeObject() {
        MaterialFactory.makeOpaqueWithColor(mContext, new Color(android.graphics.Color.RED))
                .thenAccept(material -> {
                        switch (shape) {
                            case CUBE:
                                modelRenderable = ShapeFactory.makeCube(new Vector3(0.1f, 0.1f, 0.1f), new Vector3(0f, 0.1f, 0f), material);
                                return;
                            case SPHERE:
                                modelRenderable= ShapeFactory.makeSphere(0.1f, new Vector3(0f, 0.1f, 0f), material);
                                return;
                            default:
                                modelRenderable= ShapeFactory.makeCylinder(0.1f, 0.05f, new Vector3(0f, 0.025f, 0f), material);
                        }
                });

    }
    private void makeModel(){
        if(this.isShape){
            makeObject();
            return;
        }
        ModelRenderable.builder()
                .setSource(mContext, Uri.parse(uri))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(modelRenderable -> {
                        this.modelRenderable = modelRenderable;

                })
                .exceptionally(throwable -> {
                    Toast.makeText(mContext, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }
}
