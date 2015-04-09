package com.ngstudio.friendstep.model.connectivity;

import android.net.ConnectivityManager;
import android.os.Build;
import android.util.SparseArray;

import org.intellij.lang.annotations.MagicConstant;

import java.util.ArrayList;
import java.util.List;


public class NetworkPriority {
    private final SparseArray<Integer> priorities = new SparseArray<Integer>();

    NetworkPriority(int[] priorities) {
        for (int i = 0; i < priorities.length; ++i) {
            this.priorities.append(i, priorities[i]);
        }
    }


    public int size() {
        return priorities.size();
    }

    public int getAffinityAt(int index) {
        return priorities.valueAt(index);
    }

    public int getPriorityAt(int index) {
        return priorities.keyAt(index);
    }


    public static final int NETWORK_PRIORITY_UNDEFINED = -1;
    public static final int NETWORK_AFFINITY_UNDEFINED = Integer.MIN_VALUE;

    public int getNetworkPriority(int networkAffinity) {
        int valueIndex = priorities.indexOfValue(networkAffinity);
        if (valueIndex >= 0)
            return priorities.keyAt(valueIndex);
        return NETWORK_PRIORITY_UNDEFINED;
    }

    public int getNetworkAffinity(int networkPriority) {
        Integer value = priorities.get(networkPriority);
        if (value == null)
            return NETWORK_AFFINITY_UNDEFINED;
        return value;
    }


    public static class Builder {
        private final List<Integer> priorities = new ArrayList<Integer>();

        public Builder prioritize(@MagicConstant(valuesFromClass = ConnectivityManager.class) int networkType) {
            final Integer networkTypeBox = networkType;
            if (!priorities.contains(networkTypeBox))
                priorities.add(networkTypeBox);
            return this;
        }


        private static final int[] PRIORITIES_DEFAULT;

        static {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
                PRIORITIES_DEFAULT = new int[]{
                        ConnectivityManager.TYPE_MOBILE,
                        ConnectivityManager.TYPE_WIFI,
                        ConnectivityManager.TYPE_WIMAX
                };
            } else {
                PRIORITIES_DEFAULT = new int[]{
                        ConnectivityManager.TYPE_BLUETOOTH,
                        ConnectivityManager.TYPE_MOBILE,
                        ConnectivityManager.TYPE_WIFI,
                        ConnectivityManager.TYPE_WIMAX,
                        ConnectivityManager.TYPE_ETHERNET
                };
            }
        }


        public NetworkPriority build() {
            if (priorities.isEmpty())
                return new NetworkPriority(PRIORITIES_DEFAULT);

            try {
                int[] prioritiesArray = new int[priorities.size()];
                for (int i = 0; i < prioritiesArray.length; ++i) {
                    prioritiesArray[i] = priorities.get(i);
                }
                return new NetworkPriority(prioritiesArray);
            } finally {
                priorities.clear();
            }
        }
    }
}
