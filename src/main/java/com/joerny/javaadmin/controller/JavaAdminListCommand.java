package com.joerny.javaadmin.controller;

import java.util.List;
import java.util.Map;

public class JavaAdminListCommand {
    private String entityName;
    private List<String> attributeNames;
    private Map<String, Map<String, List<String>>> entities;

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public Map<String, Map<String, List<String>>> getEntities() {
        return entities;
    }

    public void setEntities(Map<String, Map<String, List<String>>> entities) {
        this.entities = entities;
    }
}
