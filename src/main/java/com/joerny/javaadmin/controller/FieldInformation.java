package com.joerny.javaadmin.controller;

public class FieldInformation {
    private String name;
    private boolean canBeNull;
    private Object value;

    public FieldInformation(String name, boolean canBeNull, Object value) {
        this.name = name;
        this.canBeNull = canBeNull;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public boolean isCanBeNull() {
        return canBeNull;
    }

    public Object getValue() {
        return value;
    }
}
