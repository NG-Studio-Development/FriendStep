package com.ngstudio.friendstep.ui;

public class UILifecycleStateTrackerBase {

    public static final int STATE_CONSTRUCTED = 0;


    // isInState may be checked from another thread;
    private volatile int state = STATE_CONSTRUCTED;


    public void setState(int state, int mask) {
        this.state = (~mask & this.state) | state;
    }


    public int getState(int mask) {
        return this.state & mask;
    }


    public boolean isInState(int state, int mask) {
        return getState(mask) >= state;
    }

}
