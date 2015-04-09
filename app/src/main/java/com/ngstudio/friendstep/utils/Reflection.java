package com.ngstudio.friendstep.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Reflection {

    public static class Invoker<T> {

        private final Method m;

        private Invoker(@NotNull Method m) {
            if (!m.isAccessible()) {
                m.setAccessible(true);
            }
            this.m = m;
        }

        public Method getMethod() {
            return m;
        }

        @SuppressWarnings("unchecked")
        public <R> R invokeFor(T receiver, Object... args) {
            try {
                return (R) m.invoke(receiver, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new Error(e);
            }
        }
    }

    public static <T> Invoker<T> invoker(@NotNull String methodName, @NotNull Class<T> clazz, Class<?>... args) {
        try {
            return new Invoker<>(clazz.getDeclaredMethod(methodName, args));
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }


    public static class Creator<T> {

        private final Constructor<T> c;

        private Creator(@NotNull Constructor<T> c) {
            if (!c.isAccessible()) {
                c.setAccessible(true);
            }
            this.c = c;
        }

        public Constructor<T> getConstructor() {
            return c;
        }

        @SuppressWarnings("unchecked")
        public T newInstanceFor(Object... args) {
            try {
                return c.newInstance(args);
            } catch (InstantiationException | IllegalAccessException
                    | InvocationTargetException e) {
                throw new Error(e);
            }
        }
    }

    public static <T> Creator<T> creator(@NotNull Class<T> clazz, Class<?>... args) {
        try {
            return new Creator<>(clazz.getDeclaredConstructor(args));
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }

}
