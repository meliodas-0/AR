package com.example.ar.Helper;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;


/**
 * Helper to manage the touch gestures. Hides the Android boilerplate code, and exposes simpler
 * methods.
 */
public class TouchHelper{
    //Gesture constants
    private float mDownX;
    private float mDownY;
    private final float SCROLL_THRESHOLD = 10;
    boolean isClick = false;
/*
* returns true if the tap is detected and returns false when drag is detected.*/
    public boolean triggerEvent(MotionEvent ev){
        Log.i("motionevent action", "onTapPlane: " + ev.getAction());
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                isClick = true;
                mDownX = ev.getX();
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isClick) {
                    isClick = false;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isClick && (Math.abs(mDownX - ev.getX()) > SCROLL_THRESHOLD || Math.abs(mDownY - ev.getY()) > SCROLL_THRESHOLD))
                    isClick = false;
                break;
        }
        return false;
    }

}
