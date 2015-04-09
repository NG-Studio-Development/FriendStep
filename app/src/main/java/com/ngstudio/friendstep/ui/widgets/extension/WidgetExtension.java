package com.ngstudio.friendstep.ui.widgets.extension;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;


import org.jetbrains.annotations.NotNull;

public abstract class WidgetExtension<T extends View> extends ExtensionBase<T> {


    public WidgetExtension(@NotNull T extendable) {
        super(extendable);
    }

    public abstract void initStyleable(Context context, AttributeSet attrs);
    public abstract void initStyleable(Context context, @NotNull TypedArray a);
}
