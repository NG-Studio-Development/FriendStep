package com.ngstudio.friendstep.ui.widgets.extension;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;


import com.ngstudio.friendstep.R;

import org.jetbrains.annotations.NotNull;

import java.security.InvalidParameterException;

public class AspectDrivenMeasurementExtension extends WidgetExtension<View> {
	public AspectDrivenMeasurementExtension(View viewExtended) {
		super(viewExtended);
	}

	@Override
	public void initStyleable(Context context, AttributeSet attrs) {
		final TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.AspectDrivenMeasurementExtension);

		assert styledAttrs != null;
		initStyleable(context, styledAttrs);
		styledAttrs.recycle();
	}

    @Override
    public void initStyleable(Context context, @NotNull TypedArray a) {
        this.setAspectRatio(a.getString(R.styleable.AspectDrivenMeasurementExtension_aspectRatio));
    }


    public static final int ASPECT_RATIO_UNSPECIFIED = -1;
	public static final int ASPECT_RATIO_WIDTH_TO_HEIGHT = 0;
	public static final int ASPECT_RATIO_HEIGHT_TO_WIDTH = 1;


	int relation;
	float aspectRatio;

	public void setAspectRatio(float w, float h) {
		if (w < .0f && h < .0f) {
			relation = ASPECT_RATIO_UNSPECIFIED;
			aspectRatio = Float.NaN;
			return;
		} else if (w <= .0f || h <= .0f)
			throw new IllegalArgumentException("Aspect W/H must be positive!");

		if (w >= h) {
			relation = ASPECT_RATIO_WIDTH_TO_HEIGHT;
			aspectRatio = w / h;
		} else {
			relation = ASPECT_RATIO_HEIGHT_TO_WIDTH;
			aspectRatio = h / w;
		}
		getExtendable().forceLayout();
	}

	public void setAspectRatio(String aspectPair) {
		if (aspectPair == null || aspectPair.isEmpty())
			aspectPair = "-1:-1";

		try {
			String[] members = aspectPair.split(":");
			setAspectRatio(Float.parseFloat(members[0]),
				Float.parseFloat(members[1]));
		} catch (Exception e) {
			throw new InvalidParameterException(
				"Aspect ratio should be specified like this: \"4:3\"");
		}
	}

	@NotNull
	public Pair<Integer, Float> getAspectRatio() {
		return Pair.create(relation, aspectRatio);
	}


	public Pair<Integer, Integer> onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (relation == ASPECT_RATIO_UNSPECIFIED)
			return null;

		int wmode = View.MeasureSpec.getMode(widthMeasureSpec);
		int hmode = View.MeasureSpec.getMode(heightMeasureSpec);

        if (wmode == View.MeasureSpec.EXACTLY
                && hmode == View.MeasureSpec.EXACTLY)
            return null;

        int w = wmode != View.MeasureSpec.UNSPECIFIED ?
                View.MeasureSpec.getSize(widthMeasureSpec) : Integer.MAX_VALUE;
        int h = hmode != View.MeasureSpec.UNSPECIFIED ?
                View.MeasureSpec.getSize(heightMeasureSpec) : Integer.MAX_VALUE;

		switch (relation) {
			case ASPECT_RATIO_WIDTH_TO_HEIGHT:
				int width = Math.round(h * aspectRatio);
				if (wmode == View.MeasureSpec.EXACTLY || width > w) {
                    h = Math.round(w / aspectRatio);
				} else {
					w = width;
				}
				break;

			case ASPECT_RATIO_HEIGHT_TO_WIDTH:
				int height = Math.round(w * aspectRatio);
				if (hmode == View.MeasureSpec.EXACTLY || height > h) {
					w = Math.round(h / aspectRatio);
				} else {
					h = height;
				}
				break;
		}
		return Pair.create(w, h);
	}
}
