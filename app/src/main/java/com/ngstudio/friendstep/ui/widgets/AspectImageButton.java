package com.ngstudio.friendstep.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.ImageButton;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.ui.widgets.extension.AspectDrivenMeasurementExtension;


public class AspectImageButton extends ImageButton {
    private final AspectDrivenMeasurementExtension aspectDrivenMeasurement = new AspectDrivenMeasurementExtension(this);

    public AspectImageButton(Context context) {
        this(context, null);
    }

    public AspectImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.imageButtonStyle);
    }

    public AspectImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {
            final TypedArray array = context.obtainStyledAttributes(attrs,
                    R.styleable.AspectDrivenMeasurementExtension, defStyle,
                    R.style.ImageButtonGeneric);

            assert array != null;
            aspectDrivenMeasurement.initStyleable(context, array);
            array.recycle();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final Pair<Integer, Integer> measuredDimensions
                = aspectDrivenMeasurement.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (measuredDimensions != null) {
            setMeasuredDimension(measuredDimensions.first, measuredDimensions.second);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    public Pair<Integer, Float> getAspectRatio() {
        return aspectDrivenMeasurement.getAspectRatio();
    }

    public void setAspectRatio(float w, float h) {
        aspectDrivenMeasurement.setAspectRatio(w, h);
    }

    public void setAspectRatio(String aspectRatio) {
        aspectDrivenMeasurement.setAspectRatio(aspectRatio);
    }

}
