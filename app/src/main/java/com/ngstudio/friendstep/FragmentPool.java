package com.ngstudio.friendstep;

import android.support.v4.app.Fragment;
import java.util.HashMap;

public class FragmentPool {

    private static FragmentPool instance = new FragmentPool();
    private HashMap<Class, Fragment> hmFragment;

    public static FragmentPool getInstance() {
        return instance;
    }

    private FragmentPool() {
        hmFragment = new HashMap<Class,Fragment>();
    }

    public Fragment newObject(Class classObject) {
        Fragment fragment = null;
        if (!hmFragment.containsKey(classObject))
            try{
                fragment = (Fragment) Class.forName(classObject.getName()).newInstance();
            }catch (Exception ex) { ex.printStackTrace(); }
        else
            fragment = popFragment(classObject);
        return fragment;
    }

    public Fragment popFragment(Class classObject) {
            return hmFragment.remove(classObject);
    }

    public void free(Class classObject, Fragment fragment) {
        hmFragment.put(classObject,fragment);
    }
}
