package com.joerny.javaadmin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JavaAdminService {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    public Collection<String> getEntityNames() {
        final Set<EntityType<?>> entities = entityManagerFactory.getMetamodel().getEntities();

        final Collection<String> names = new ArrayList<>(entities.size());
        for (final EntityType<?> type : entities) {
            names.add(type.getName());
        }

        return names;
    }
}
