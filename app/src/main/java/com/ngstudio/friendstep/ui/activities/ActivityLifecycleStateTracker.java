package com.ngstudio.friendstep.ui.activities;

import android.app.Activity;
import android.util.Log;

import com.ngstudio.friendstep.ui.UILifecycleStateTrackerBase;

import org.jetbrains.annotations.NotNull;

public class ActivityLifecycleStateTracker extends UILifecycleStateTrackerBase {

    private static final String TAG = ActivityLifecycleStateTracker.class.getSimpleName();


    // Secondary states
    public static final int STATE_SAVED = 8; // Because it's not guaranteed to appear before or after onPause().

    // Primary states
    public static final int STATE_CREATED   = 1;
    public static final int STATE_STARTED   = 2;
    public static final int STATE_RESTORED  = 3;
    public static final int STATE_RESUMED   = 4;
    public static final int STATE_PAUSED    = 5;
    public static final int STATE_STOPPED   = 6;
    public static final int STATE_DESTROYED = 7;


    private final String activityName;

    ActivityLifecycleStateTracker(@NotNull Activity activity) {
        this.activityName = activity.getClass().getSimpleName();
    }


    @Override
    public void setState(int state, int mask) {
        super.setState(state, mask);

        Log.d(TAG, String.format("%s: 0x%x", activityName, getState(0x0f)));
    }



    public void reportStateCreated() {
        setState(STATE_CREATED, 0x7);
    }

    public void reportStateStarted() {
        setState(STATE_STARTED, 0x7);
    }

    public void reportStateRestored() {
        setState(STATE_RESTORED, 0xf);
    }

    public void reportStateResumed() {
        setState(STATE_RESUMED, 0xf);
    }

    public void reportStatePaused() {
        setState(STATE_PAUSED, 0x7);
    }

    public void reportStateSaved() {
        setState(STATE_SAVED, 0xf);
    }

    public void reportStateStopped() {
        setState(STATE_STOPPED, 0x7);
    }

    public void reportStateDestroyed() {
        setState(STATE_DESTROYED, 0x7);
    }


    public boolean isInSavedState() {
        return isInState(STATE_SAVED, 0xf);
    }

    public boolean isInDestroyedState() {
        return isInState(STATE_DESTROYED, 0x7);
    }

    public boolean areFragmentManipulatonsAllowed() {
        return !(isInSavedState() || isInDestroyedState());
    }

}