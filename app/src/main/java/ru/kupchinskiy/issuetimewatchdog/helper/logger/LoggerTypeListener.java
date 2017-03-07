package ru.kupchinskiy.issuetimewatchdog.helper.logger;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class LoggerTypeListener implements TypeListener {

    public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
        Class<?> clazz = typeLiteral.getRawType();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType() == Logger.class &&
                        Modifier.isStatic(field.getModifiers()) &&
                        Modifier.isPrivate(field.getModifiers())) {
                    typeEncounter.register(new LoggerMembersInjector<>(field));
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

}
