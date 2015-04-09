package com.alexutils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Alex on 27.01.14.
 */
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface DbAnnotation{
    String name() default "";
    String dbType() default "text";
    String foreignKey() default  "";
    boolean isEncrypted() default false;
    String[] mapFields() default {};
}