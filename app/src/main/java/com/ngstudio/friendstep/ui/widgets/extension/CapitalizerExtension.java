package com.ngstudio.friendstep.ui.widgets.extension;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.utils.TextUtils;

import org.jetbrains.annotations.NotNull;


public class CapitalizerExtension extends WidgetExtension<TextView> {
    public CapitalizerExtension(TextView viewExtended) {
        super(viewExtended);
    }


    private static final int CAPITALIZER_OFF = -1;
    private TextUtils.Capitalize capitalizerType;

    @Override
    public void initStyleable(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CapitalizerExtension);

        assert a != null;
        initStyleable(context, a);
        a.recycle();
    }

    @Override
    public void initStyleable(Context context, @NotNull TypedArray a) {
        int capitalizerType = a.getInt(R.styleable.CapitalizerExtension_capitalizeText, CAPITALIZER_OFF);
        if (capitalizerType != CAPITALIZER_OFF)
            setCapitalizerType(TextUtils.Capitalize.fromValue(capitalizerType));
    }


    public CharSequence getCapitalizedText(CharSequence text) {
        if (text == null || capitalizerType == null)
            return text;
        return TextUtils.textCapitalize(text.toString(), capitalizerType);
    }

    public void setText(CharSequence text) {
        getExtendable().setText(getCapitalizedText(text));
    }

    public void setHint(CharSequence text) {
        getExtendable().setHint(getCapitalizedText(text));
    }

    public TextUtils.Capitalize getCapitalizerType() {
        return capitalizerType;
    }

    public void setCapitalizerType(TextUtils.Capitalize capitalizerType) {
        if (this.capitalizerType == capitalizerType)
            return;
        onCapitalizerTypeChanged(this.capitalizerType = capitalizerType);
    }

    protected void onCapitalizerTypeChanged(TextUtils.Capitalize capitalizerType) {
        if (capitalizerType != null) {
            CharSequence text = getExtendable().getText();
            if (text != null)
                setText(text);

            text = getExtendable().getHint();
            if (text != null)
                setHint(text);
        }
    }
}
