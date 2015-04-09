package com.ngstudio.friendstep.testc;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.google.android.gms.maps.model.Marker;

public abstract class OnInfoWindowElemTouchListener implements OnTouchListener {
    private final View view;
    //private final Drawable bgDrawableNormal;
    //private final Drawable bgDrawablePressed;
    private final Handler handler = new Handler();

    private Marker marker;
    private boolean pressed = false;

    public OnInfoWindowElemTouchListener(View view) {
        this.view = view;
        //this.bgDrawableNormal = bgDrawableNormal;
        //this.bgDrawablePressed = bgDrawablePressed;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if ( 0 <= event.getX() && event.getX() <= view.getWidth() &&
                    0 <= event.getY() && event.getY() <= view.getHeight() ) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: startPress(); break;
                case MotionEvent.ACTION_UP: handler.postDelayed(confirmClickRunnable, 150); break;
                case MotionEvent.ACTION_CANCEL: endPress(); break;
                default: break;
            }
        } else { endPress(); }
        return false;
    }

    private void startPress() {
        //setPressed(true);
        Log.d("CLICK_M", "startPress()");
        if (!pressed) {
            pressed = true;
            handler.removeCallbacks(confirmClickRunnable);
            //view.setBackground(bgDrawablePressed);
            setPressed(true);
            if (marker != null)
                marker.showInfoWindow();
        }
    }

    /*private void startPress() {
        if (!pressed) {
            pressed = true;
            handler.removeCallbacks(confirmClickRunnable);
            view.setBackground(bgDrawablePressed);
            if (marker != null)
                marker.showInfoWindow();
        }
    }*/


    private boolean endPress() {
        Log.d("CLICK_M", "endPress()");
        //setPressed(false);
        if (pressed) {
            this.pressed = false;
            handler.removeCallbacks(confirmClickRunnable);
            //view.setBackground(bgDrawableNormal);
            setPressed(false);
            if (marker != null)
                marker.showInfoWindow();
            return true;
        }
        else
            return false;
    }

    private void setPressed(boolean pressed) {
        ((Button)view).setPressed(pressed);
        ((Button)view).invalidate();
    }

    private final Runnable confirmClickRunnable = new Runnable() {
        public void run() {
            if (endPress()) { onClickConfirmed(view, marker); }
        }
    };

    protected abstract void onClickConfirmed(View v, Marker marker);
}