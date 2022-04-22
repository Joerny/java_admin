package com.joerny.javaadmin.controller;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<String> getAttributeNames() {
        return attributeNames;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setAttributeNames(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Map<String, Map<String, List<String>>> getEntities() {
        return entities;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setEntities(Map<String, Map<String, List<String>>> entities) {
        this.entities = entities;
    }
}
