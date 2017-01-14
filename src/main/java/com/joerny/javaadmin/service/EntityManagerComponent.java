package com.joerny.javaadmin.service;

import com.joerny.javaadmin.FieldAccessPrivilegedAction;
import com.joerny.javaadmin.controller.EntityInformation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityManagerComponent {
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

    public Class<?> getEntityClass(final String entityName) {
        return getEntityType(entityName).getJavaType();
    }

    public Map<String, Class<?>> getChildEntities(final String entityName) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        final EntityType<?> entity = getEntityType(entityName);

        final Map<String, Class<?>> childEntities = new HashMap<>();

        final Set attributes = entity.getDeclaredSingularAttributes();

        for (final Object attr : attributes) {
            final SingularAttribute attribute = (SingularAttribute) attr;

            if (isEntity(attribute.getJavaType())) {
                childEntities.put(attribute.getName(), attribute.getJavaType());
            }
        }

        return childEntities;
    }

    public Map<String, Class<?>> getEnums(final String entityName) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        final EntityType<?> entity = getEntityType(entityName);

        final Map<String, Class<?>> enums = new HashMap<>();

        final Set attributes = entity.getDeclaredSingularAttributes();

        for (final Object attr : attributes) {
            final SingularAttribute attribute = (SingularAttribute) attr;

            if (attribute.getJavaType().isEnum()) {
                enums.put(attribute.getName(), attribute.getJavaType());
            }
        }

        return enums;
    }

    public boolean isEntity(final Class<?> clazz) {
        boolean check;
        try {
            entityManagerFactory.getMetamodel().entity(clazz);
            check = true;
        } catch (IllegalArgumentException e) {
            check = false;
        }
        return check;
    }

    private EntityType<?> getEntityType(final String entityName) {
        final Set<EntityType<?>> entities = entityManagerFactory.getMetamodel().getEntities();

        EntityType<?> entity = null;
        for (final EntityType<?> entityTmp : entities) {
            if (entityTmp.getName().equals(entityName)) {
                entity = entityTmp;
            }
        }
        return entity;
    }

    public Map<String, Object> getFieldValues(final String entityName) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        final EntityType<?> entity = getEntityType(entityName);

        final Object object = entity.getJavaType().newInstance();

        final Map<String, Object> fields = getSingleAttrFieldValues(entity, object);

        final Set multiAttributes = entity.getDeclaredPluralAttributes();
        for (final Object attr : multiAttributes) {
            final Attribute attribute = (Attribute) attr;
            fields.put(attribute.getName(), new LinkedList());
        }

        return fields;
    }

    public Map<String, Object> getFieldValues(final String entityName, final Object object) throws NoSuchFieldException, IllegalAccessException {
        final EntityType<?> entity = getEntityType(entityName);
        final Map<String, Object> fields = getSingleAttrFieldValues(entity, object);

        for (final Object attr : entity.getDeclaredPluralAttributes()) {
            final PluralAttribute attribute = (PluralAttribute) attr;

            final List<String> values = new LinkedList<>();

            if (Collection.class.isAssignableFrom(object.getClass())) {
                final Field field = FieldAccessPrivilegedAction.getField(object.getClass(), attribute.getName());
                final Collection collection = (Collection) field.get(object);
                for (Object value : collection) {
                    values.add(Objects.toString(value));
                }
            }

            fields.put(attribute.getName(), values);
        }
        return fields;
    }


    private static Map<String, Object> getSingleAttrFieldValues(final EntityType<?> entity, final Object object)
            throws NoSuchFieldException, IllegalAccessException {
        final Map<String, Object> fields = new HashMap<>();
        final Class<?> aClass = object.getClass();
        for (final Object attr : entity.getDeclaredSingularAttributes()) {
            final SingularAttribute attribute = (SingularAttribute) attr;
            if (!attribute.isId()) {
                final Field field = FieldAccessPrivilegedAction.getField(aClass, attribute.getName());
                fields.put(attribute.getName(), Objects.toString(field.get(object), null));
            }
        }
        return fields;
    }

    public List<EntityInformation> getEntityInformation(final List children, final Class<?> javaType) throws NoSuchFieldException, IllegalAccessException {
        final EntityType<?> entity = entityManagerFactory.getMetamodel().entity(javaType);
        SingularAttribute id = null;
        for (final Object attr : entity.getDeclaredSingularAttributes()) {
            final SingularAttribute attribute = (SingularAttribute) attr;
            if (attribute.isId()) {
                id = attribute;
            }
        }
        final List<EntityInformation> information = new ArrayList<>(children.size());
        for (final Object child : children) {
            final Field field = FieldAccessPrivilegedAction.getField(javaType, id.getName());
            information.add(new EntityInformation(field.get(child), child.toString()));
        }
        return information;
    }

    public Class<?> getListElementClass(final Class<?> aClass, final String fieldName) {
        final EntityType<?> entity = entityManagerFactory.getMetamodel().entity(aClass);

        final ListAttribute<?, ?> listAttribute = entity.getDeclaredList(fieldName);
        return listAttribute.getElementType().getJavaType();
    }
}
