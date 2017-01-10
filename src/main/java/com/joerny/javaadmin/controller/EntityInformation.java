package com.joerny.javaadmin.controller;

public class EntityInformation {
    private final Object id;
    private final String description;

    public EntityInformation(Object id, String description) {
        this.id = id;
        this.description = description;
    }

    public Object getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
