package com.ngstudio.friendstep.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;


public class AdapterLinearLayout extends LinearLayout {

    Adapter adapter;
    OnItemClickListener onItemClickListener;
    View selectedView;

    public AdapterLinearLayout(Context context) {
        super(context);
    }

    public AdapterLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        fillViews(adapter);
    }

    private void fillViews(final Adapter adapter) {
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            View child  = adapter.getView(i,null,this);
            addView(child);
        }
        invalidate();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;

        for(int i = 0; i < getChildCount(); i++) {
            final int position = i;
            getChildAt(i).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AdapterLinearLayout.this.onItemClickListener.onItemClick(adapter,position,v);
                }
            });
        }
    }

    public boolean setSelected(int position) {
        View view = getChildAt(position);
        if(view == null)
            return false;

        if(view.equals(selectedView))
            return false;

        if(selectedView != null)
            selectedView.setSelected(false);
        view.setSelected(true);
        selectedView = view;
        return true;
    }

    public interface OnItemClickListener {
        public void onItemClick(Adapter adapter, int pos, View v);
    }
}
