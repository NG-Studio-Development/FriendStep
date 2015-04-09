package com.ngstudio.friendstep.ui.adapters;

import android.content.Context;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple ArrayAdapter with back-compatibility with Android SDK lower 11. Provides base filtering feature
 *
 * @param <T>
 */
public class BaseArrayAdapter<T> extends ArrayAdapter<T> {

	public BaseArrayAdapter(Context context) {
		super(context, 0);
	}

	public BaseArrayAdapter(Context context, int res) {
		super(context, res);
	}

	public BaseArrayAdapter(Context context, int style, List<T> list) {
		super(context, style, list);
	}


	protected Filter mFilter;

	@Override
	public Filter getFilter() {
		return mFilter;
	}

	public void setFilter(Filter filter) {
        mFilter = filter;
    }


	protected final List<T> mOriginal = new ArrayList<>();

    @Override
    public void add(T object) {
        super.add(object);
        mOriginal.add(object);
    }

    @Override
    public void remove(T object) {
        super.remove(object);
        mOriginal.remove(object);
    }

    @Override
    public void clear() {
        super.clear();
        mOriginal.clear();
    }


	private final Object mLock = new Object();

    public void addAll(Collection<? extends T> collection) {
        addAll(collection,true);
    }

    public void addAll(Collection<? extends T> collection, boolean addIfExists) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            super.addAll(collection);
        else {
	        setNotifyOnChange(false);
            synchronized (mLock) {
                for (T item : collection) {
                    if(addIfExists) {
                        add(item);
                    } else {
                        if(getPosition(item) == -1)
                            add(item);
                    }
                }
            }
	        notifyDataSetChanged();
        }
    }
}
