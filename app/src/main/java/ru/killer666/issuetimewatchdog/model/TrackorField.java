package ru.killer666.issuetimewatchdog.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TrackorField {
    String value() default "";
    String humanName() default "";
}
