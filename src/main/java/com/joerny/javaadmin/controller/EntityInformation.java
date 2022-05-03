package com.joerny.javaadmin.controller;

public class EntityInformation {
    private final Object id;
    private final String description;

    public EntityInformation(Object id, Object entity) {
        this.id = id;

        String desc = getString(id, entity);

        this.description = desc;
    }

    public static String getString(Object id, Object entity) {
        String desc;
        try {
            if (entity.getClass().getMethod("toString").getDeclaringClass() != Object.class) {
                desc = entity.toString();
            } else {
                desc = entity.getClass().getSimpleName() + " (ID: " + id + ")";
            }
        } catch (NoSuchMethodException e) {
            desc = entity.getClass().getSimpleName() + " (ID: " + id + ")";
        }
        return desc;
    }

    public Object getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
