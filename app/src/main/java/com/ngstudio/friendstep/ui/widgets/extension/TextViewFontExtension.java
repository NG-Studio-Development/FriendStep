package com.ngstudio.friendstep.ui.widgets.extension;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;


import com.ngstudio.friendstep.R;

import org.jetbrains.annotations.NotNull;

public class TextViewFontExtension extends WidgetExtension<TextView> {
	private static final String TAG = TextViewFontExtension.class.getSimpleName();

	public TextViewFontExtension(TextView textView) {
		super(textView);
	}

	public void initStyleable(Context context, AttributeSet attrs) {
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextViewFontExtension);
//		if (a == null) {
//			Log.e(TAG, "Can't obtain styled attributes.");
//			return;
//		}

        assert a != null;
        initStyleable(context, a);
		a.recycle();
	}

    @Override
    public void initStyleable(Context context, @NotNull TypedArray a) {
        String customFont = a.getString(R.styleable.TextViewFontExtension_customFont);
        setCustomFont(context, customFont);
    }


    public boolean setCustomFont(@NotNull Context context, String fontName) {
		try {
			getExtendable().setTypeface(fontName != null ?
				Typeface.createFromAsset(context.getAssets(), fontName) : getExtendable().getTypeface());
		} catch (Exception e) {
			Log.e(TAG, "Could not get typeface: " + e.getMessage());
			return false;
		}
		return true;
	}
}
