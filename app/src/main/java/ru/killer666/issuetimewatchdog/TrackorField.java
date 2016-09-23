package ru.killer666.issuetimewatchdog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface TrackorField {
    String value() default "";
}
