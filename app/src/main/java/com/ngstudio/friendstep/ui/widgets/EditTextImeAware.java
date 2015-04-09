package com.ngstudio.friendstep.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;


import com.ngstudio.friendstep.ui.widgets.extension.ImeAwareExtension;

import org.jetbrains.annotations.NotNull;


public class EditTextImeAware extends EditText {
    private final ImeAwareExtension imeAwareExtension = new ImeAwareExtension(this);

    public EditTextImeAware(Context context) {
        super(context);
    }

    public EditTextImeAware(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextImeAware(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setOnPreImeEventListener(ImeAwareExtension.OnPreImeEventListener preImeEventListener) {
        imeAwareExtension.setOnPreImeEventListener(preImeEventListener);
    }


    @Override
    public boolean onKeyPreIme(int keyCode, @NotNull KeyEvent event) {
		return imeAwareExtension.onKeyPreIme(keyCode, event);
    }
}
