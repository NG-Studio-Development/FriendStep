package com.ngstudio.friendstep.ui.widgets.extension;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import org.jetbrains.annotations.NotNull;


public class ImeAwareExtension extends WidgetExtension<EditText> {
	public ImeAwareExtension(EditText viewExtended) {
		super(viewExtended);
	}

	@Override
	public void initStyleable(Context context, AttributeSet attrs) {
		/* Nothing to do */
	}

    @Override
    public void initStyleable(Context context, @NotNull TypedArray a) {
		/* Nothing to do */
    }


    private OnPreImeEventListener mPreImeEventListener;

	public void setOnPreImeEventListener(OnPreImeEventListener preImeEventListener) {
		mPreImeEventListener = preImeEventListener;
	}


	public boolean onKeyPreIme(int keyCode, @NotNull KeyEvent event) {
		if (mPreImeEventListener != null)
			return mPreImeEventListener.onPreImeEvent(keyCode, event);
		return false;
	}


	public interface OnPreImeEventListener {
		public boolean onPreImeEvent(int keyCode, KeyEvent event);
	}
}
