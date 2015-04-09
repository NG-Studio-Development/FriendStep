package com.ngstudio.friendstep.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;


import com.ngstudio.friendstep.ui.widgets.extension.CapitalizerExtension;
import com.ngstudio.friendstep.ui.widgets.extension.TextViewFontExtension;

import org.jetbrains.annotations.NotNull;


public class EditTextFontView extends EditTextImeAware {
    private final TextViewFontExtension mTextFontViewExtension    = new TextViewFontExtension(this);
    private final CapitalizerExtension mTextCapitalizerExtension = new CapitalizerExtension(this);

    public EditTextFontView(Context context) {
        super(context);
    }

    public EditTextFontView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTextFontViewExtension.initStyleable(context, attrs);
        mTextCapitalizerExtension.initStyleable(context, attrs);
    }

    public EditTextFontView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTextFontViewExtension.initStyleable(context, attrs);
        mTextCapitalizerExtension.initStyleable(context, attrs);
    }


    public boolean setCustomFont(@NotNull Context context, String fontName) {
        return mTextFontViewExtension.setCustomFont(context, fontName);
    }


	public void setText(String text) {
		mTextCapitalizerExtension.setText(text);
	}

    public void setHint(String text) {
        mTextCapitalizerExtension.setHint(text);
    }
}
