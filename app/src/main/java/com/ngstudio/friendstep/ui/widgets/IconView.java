package com.ngstudio.friendstep.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;


import com.ngstudio.friendstep.R;

import org.jetbrains.annotations.NotNull;


public class IconView extends View {
	public IconView(Context context) {
		this(context, null);
	}

	public IconView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public IconView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		initStyleable(context, attrs);
	}


	private void initStyleable(Context context, AttributeSet attrs) {
		if (attrs == null)
			return;

		final TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.IconView);

		assert styledAttrs != null;
		this.setEnabled(styledAttrs.getBoolean(R.styleable.IconView_square, true));

		this.setScaleX(styledAttrs.getFloat(R.styleable.IconView_scale_x, 1.f));
		this.setScaleY(styledAttrs.getFloat(R.styleable.IconView_scale_y, 1.f));
		styledAttrs.recycle();
	}


	private boolean mSquare;

	public void setSquare(boolean square) {
		if (mSquare == square)
			return;

		mSquare = square;
		forceLayout();
	}


	private float scaleX, scaleY;

	public void setScaleX(float amount) {
		this.scaleX = amount;
	}

	public float getScaleX() {
		return scaleX;
	}

	public void setScaleY(float amount) {
		this.scaleY = amount;
	}

	public float getScaleY() {
		return scaleY;
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final Drawable d = getBackground();
		if (d == null) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}

		final LayoutParams layoutParams = getLayoutParams();
		assert layoutParams != null;

		refreshDrawableState();
		int width = layoutParams.width == LayoutParams.WRAP_CONTENT ? d.getIntrinsicWidth() :
			Math.max(d.getIntrinsicWidth(), MeasureSpec.getSize(widthMeasureSpec));
		int height = layoutParams.height == LayoutParams.WRAP_CONTENT ? d.getIntrinsicHeight() :
			Math.max(d.getIntrinsicHeight(), MeasureSpec.getSize(heightMeasureSpec));

		if (mSquare) {
			int square = Math.max(width, height);
			setMeasuredDimension(square, square);
			return;
		}

		setMeasuredDimension(width, height);
	}


	private float scaleCenterX, scaleCenterY;

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (changed) {
			scaleCenterX = (right - left) / 2.f;
			scaleCenterY = (bottom - top) / 2.f;
		}
	}


	@Override
	public void draw(@NotNull Canvas canvas) {
		canvas.save();
		canvas.scale(scaleX, scaleY, scaleCenterX, scaleCenterY);

		super.draw(canvas);

		canvas.restore();
	}

}
