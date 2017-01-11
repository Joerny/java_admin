package com.joerny.javaadmin;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

public final class FieldAccessPrivilegedAction implements PrivilegedAction {
    private final Field field;

    private FieldAccessPrivilegedAction(final Field field) {
        this.field = field;
    }

    public static Field getField(final Class<?> aClass, final String name) throws NoSuchFieldException {
        final Field field = aClass.getDeclaredField(name); //NoSuchFieldException
        AccessController.doPrivileged(new FieldAccessPrivilegedAction(field));
        return field;
    }

    @Override
    public Object run() {
        field.setAccessible(true);
        return null;
    }
}
