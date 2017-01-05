package ru.killer666.issuetimewatchdog;

import com.google.inject.Injector;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import roboguice.RoboGuice;

public class Application extends android.app.Application {
    public static final String TRACKOR_DOMAIN = "trackor.onevizion.com";
    public static final String TRACKOR_PROTOCOL = "https://";
    public static final String TRACKOR_BASEURL = TRACKOR_PROTOCOL + TRACKOR_DOMAIN;

    @Override
    public void onCreate() {
        super.onCreate();

        RoboGuice.setUseAnnotationDatabases(false);
        Injector injector = RoboGuice.getOrCreateBaseApplicationInjector(this);

        // Do nothing, auto initialize
        injector.getInstance(CreateTimeRecordsSettings.class);
    }

    public static class LoggerTypeListener implements TypeListener {
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

    public static class LoggerMembersInjector<T> implements MembersInjector<T> {
        private final Field field;
        private final Logger logger;

        LoggerMembersInjector(Field field) {
            this.field = field;
            this.logger = LoggerFactory.getLogger(field.getDeclaringClass().getSimpleName());
            field.setAccessible(true);
        }

        public void injectMembers(T t) {
            try {
                field.set(t, logger);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
