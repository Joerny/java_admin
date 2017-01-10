package com.joerny.javaadmin.service;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
public class JavaAdminService {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private WebApplicationContext appContext;

    public Collection<String> getEntityNames() {
        final Set<EntityType<?>> entities = entityManagerFactory.getMetamodel().getEntities();

        final Collection<String> names = new ArrayList<>(entities.size());
        for (final EntityType<?> type : entities) {
            names.add(type.getName());
        }

        return names;
    }

    public Collection<String> getIdAttributeNames(final String entityName) {
        final EntityType<?> entity = getEntityType(entityName);

        final Collection<String> idNames = new LinkedList<>();
        for (final Object attr : entity.getDeclaredSingularAttributes()) {
            final SingularAttribute attribute = (SingularAttribute) attr;
            if (attribute.isId()) {
                idNames.add(attribute.getName());
            }
        }
        return idNames;
    }

    public Collection<String> getSingleAttributeNames(final String entityName) {
        final EntityType<?> entity = getEntityType(entityName);

        final Collection<String> singleAttributeNames = new LinkedList<>();
        for (final Object attr : entity.getDeclaredSingularAttributes()) {
            final SingularAttribute attribute = (SingularAttribute) attr;
            if (!attribute.isId()) {
                singleAttributeNames.add(attribute.getName());
            }
        }
        return singleAttributeNames;
    }

    public Collection<String> getMultipleAttributeNames(final String entityName) {
        final EntityType<?> entity = getEntityType(entityName);

        final Collection<String> multiAttributeNames = new LinkedList<>();
        for (final Object attr : entity.getDeclaredPluralAttributes()) {
            final Attribute attribute = (Attribute) attr;
            multiAttributeNames.add(attribute.getName());
        }
        return multiAttributeNames;
    }

    public EntityType<?> getEntityType(final String entityName) {
        final Set<EntityType<?>> entities = entityManagerFactory.getMetamodel().getEntities();

        EntityType<?> entity = null;
        for (final EntityType<?> entityTmp : entities) {
            if (entityTmp.getName().equals(entityName)) {
                entity = entityTmp;
            }
        }
        return entity;
    }

    public Map<String, Map<String, List<String>>> getEntityValues(final String entityName, final Iterable<String> idNames, final Iterable<String> attributeNames,
                                                                  final Iterable<String> multiAttributeNames) throws NoSuchFieldException, IllegalAccessException {
        final EntityType<?> entity = getEntityType(entityName);

        final JpaRepository<?, ?> repository = getJpaRepository(entity);

        final List<?> data = repository.findAll();

        final Map<String, Map<String, List<String>>> entities = new LinkedHashMap<>(data.size());
        for (final Object datum : data) {
            String id = null;

            final Map<String, List<String>> entityValues = new HashMap<>();

            for (final String idName : idNames) {
                final Field field = getField(datum.getClass(), idName);

                final List<String> values = new LinkedList<>();
                values.add(Objects.toString(field.get(datum)));

                id = field.get(datum).toString();

                entityValues.put(idName, values);
            }

            for (final String name : attributeNames) {
                final Field field = getField(datum.getClass(), name);

                final List<String> values = new LinkedList<>();
                values.add(Objects.toString(field.get(datum)));

                entityValues.put(name, values);
            }

            for (final String name : multiAttributeNames) {
                final Field field = getField(datum.getClass(), name);

                final List<String> values = new LinkedList<>();
                final Object object = field.get(datum);
                if (object == null) {
                    values.add("null");
                } else if (Collection.class.isAssignableFrom(object.getClass())) {
                    final Collection collection = (Collection) object;
                    for (final Object value : collection) {
                        values.add(Objects.toString(value));
                    }
                }

                entityValues.put(name, values);
            }

            entities.put(id, entityValues);
        }
        return entities;
    }

    public JpaRepository<?, ?> getJpaRepository(final Type<?> entity) {
        return getJpaRepository(entity.getJavaType());
    }

    public JpaRepository<?, ?> getJpaRepository(final Class<?> javaType) {
        final Repositories repositories = new Repositories(appContext);

        return (JpaRepository<?, ?>) repositories.getRepositoryFor(javaType);
    }

    public static Field getField(final Class<?> aClass, final String name) throws NoSuchFieldException {
        final Field field = aClass.getDeclaredField(name); //NoSuchFieldException
        AccessController.doPrivileged(new FieldAccessPrivilegedAction(field));
        return field;
    }

    private static class FieldAccessPrivilegedAction implements PrivilegedAction {
        private final Field field;

        public FieldAccessPrivilegedAction(final Field field) {
            this.field = field;
        }

        @Override
        public Object run() {
            field.setAccessible(true);
            return null;
        }
    }
}
