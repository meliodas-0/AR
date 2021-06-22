package com.example.ar;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import com.example.ar.Helper.TouchHelper;

public class GLSurfaceActivity extends AppCompatActivity {

    GLSurfaceView surfaceView;
    TouchHelper touchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glsurface);
        surfaceView = findViewById(R.id.glSurfaceView);
        touchHelper = new TouchHelper();
        surfaceView.setOnTouchListener(touchHelper);

    }
}