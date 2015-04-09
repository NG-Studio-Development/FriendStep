package com.ngstudio.friendstep.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class SmartReference<T> extends WeakReference<T> {
    public SmartReference(T r) {
        super(r);
    }

    public SmartReference(T r, ReferenceQueue<? super T> q) {
        super(r, q);
    }

    @Override
    public boolean equals(Object o) {
        final Object objectRef = get();
        if (o == null)
            return objectRef == null;
        return o.equals(objectRef) || objectRef != null && (o instanceof SmartReference)
                && objectRef.equals(((SmartReference) o).get()) || super.equals(o);
    }

}