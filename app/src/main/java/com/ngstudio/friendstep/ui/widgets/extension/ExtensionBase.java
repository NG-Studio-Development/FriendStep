package com.ngstudio.friendstep.ui.widgets.extension;


import org.jetbrains.annotations.NotNull;

public class ExtensionBase<T> {

    private final T extendable;


    public ExtensionBase(@NotNull T extendable) {
        this.extendable = extendable;
    }


    public T getExtendable() {
        return this.extendable;
    }

}
