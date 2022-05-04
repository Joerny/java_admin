package com.joerny.javaadmin.service;

import com.joerny.javaadmin.FieldAccessPrivilegedAction;
import com.joerny.javaadmin.controller.EntityInformation;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.Entity;

@Service
public class JavaAdminService {
    private static final String[] DATE_PARSE_PATTERNS = {"dd.MM.yyyy", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.S"};

    @Autowired
    private EntityManagerComponent entityManagerComponent;

    @Autowired
    private WebApplicationContext appContext;

    public Collection<String> getEntityNames() {
        return entityManagerComponent.getEntityNames();
    }

    public Collection<String> getIdAttributeNames(final String entityName) {
        return entityManagerComponent.getIdAttributeNames(entityName);
    }

    public Collection<String> getSingleAttributeNames(final String entityName) {
        return entityManagerComponent.getSingleAttributeNames(entityName);
    }

    public Collection<String> getMultipleAttributeNames(final String entityName) {
        return entityManagerComponent.getMultipleAttributeNames(entityName);
    }

    public Map<String, Map<String, List<String>>> getEntityValues(final String entityName, final Iterable<String> idNames, final Iterable<String> attributeNames,
                                                                  final Iterable<String> multiAttributeNames) throws NoSuchFieldException, IllegalAccessException {
        final Class<?> entityClass = entityManagerComponent.getEntityClass(entityName);

        final JpaRepository<?, ?> repository = getJpaRepository(entityClass);

        final List<?> data = repository.findAll();

        final Map<String, Map<String, List<String>>> entities = new LinkedHashMap<>(data.size());
        for (final Object datum : data) {
            String id = null;

            final Map<String, List<String>> entityValues = new HashMap<>();

            for (final String idName : idNames) {
                final Field field = FieldAccessPrivilegedAction.getField(datum.getClass(), idName);

                final List<String> values = new LinkedList<>();
                values.add(getString(field.get(datum)));

                id = field.get(datum).toString();

                entityValues.put(idName, values);
            }

            for (final String name : attributeNames) {
                final Field field = FieldAccessPrivilegedAction.getField(datum.getClass(), name);

                final List<String> values = new LinkedList<>();
                values.add(getString(field.get(datum)));

                entityValues.put(name, values);
            }

            for (final String name : multiAttributeNames) {
                final Field field = FieldAccessPrivilegedAction.getField(datum.getClass(), name);

                final List<String> values = new LinkedList<>();
                final Object object = field.get(datum);
                if (object == null) {
                    values.add("null");
                } else if (Collection.class.isAssignableFrom(object.getClass())) {
                    final Collection collection = (Collection) object;
                    for (final Object value : collection) {
                        values.add(getString(value));
                    }
                }

                entityValues.put(name, values);
            }

            entities.put(id, entityValues);
        }
        return entities;
    }

    private String getString(Object object) throws NoSuchFieldException, IllegalAccessException {
        final String desc;

        if (object != null && entityManagerComponent.isEntity(object.getClass())) {
            desc = entityManagerComponent.getEntityInformation(Collections.singletonList(object), object.getClass()).get(0).getDescription();
        } else {
            desc = Objects.toString(object);
        }
        return desc;
    }

    public Map<String, List<?>> getChildEntities(final String entityName) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        final Map<String, List<?>> childEntityInfos = new HashMap<>();

        final Map<String, Class<?>> childEntityClasses = entityManagerComponent.getChildEntities(entityName);
        for (final Map.Entry<String, Class<?>> child : childEntityClasses.entrySet()) {
            final Class<?> javaType = child.getValue();
            final JpaRepository fieldRepository = getJpaRepository(javaType);
            final List children = fieldRepository.findAll();
            childEntityInfos.put(child.getKey(), entityManagerComponent.getEntityInformation(children, javaType));
        }

        final Map<String, Class<?>> enumClasses = entityManagerComponent.getEnums(entityName);
        for (final Map.Entry<String, Class<?>> enumClass : enumClasses.entrySet()) {
            final Class javaType = enumClass.getValue();
            final Collection<Enum> enums = EnumSet.allOf(javaType);
            final List<EntityInformation> information = new ArrayList<>(enums.size());
            for (Enum singleEnum : enums) {
                information.add(new EntityInformation(singleEnum.name(), singleEnum.toString()));
            }
            childEntityInfos.put(enumClass.getKey(), information);
        }

        return childEntityInfos;
    }

    private JpaRepository<?, ?> getJpaRepository(final Class<?> javaType) {
        final Repositories repositories = new Repositories(appContext);

        return (JpaRepository<?, ?>) repositories.getRepositoryFor(javaType).orElseThrow(RuntimeException::new);
    }

    public Map<String, Object> getFieldValues(final String entityName) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        return entityManagerComponent.getFieldValues(entityName);
    }

    public Map<String, Object> getFieldValues(final String entityName, final Long id) throws NoSuchFieldException, IllegalAccessException {
        final Class<?> entityClass = entityManagerComponent.getEntityClass(entityName);

        final JpaRepository repository = getJpaRepository(entityClass);

        final Object object = repository.findById(id).get();

        return entityManagerComponent.getFieldValues(entityName, object);
    }

    public void saveEntity(final String entityName, final Map<String, List<String>> formData)
            throws InstantiationException, IllegalAccessException, NoSuchFieldException, ParseException {
        final Class<?> entityClass = entityManagerComponent.getEntityClass(entityName);

        final JpaRepository repository = getJpaRepository(entityClass);

        final Object object = entityClass.newInstance();

        fillObject(formData, object);

        repository.save(object);
    }

    public void saveEntity(final String entityName, final Long id, final Map<String, List<String>> formData)
            throws NoSuchFieldException, IllegalAccessException, ParseException {
        final Class<?> entityClass = entityManagerComponent.getEntityClass(entityName);

        final JpaRepository repository = getJpaRepository(entityClass);

        Object object = repository.findById(id).get();

        fillObject(formData, object);

        repository.saveAndFlush(object);
    }

    public void deleteEntity(final String entityName, final Long id) {
        final Class<?> entityClass = entityManagerComponent.getEntityClass(entityName);

        final JpaRepository repository = getJpaRepository(entityClass);

        final Object object = repository.findById(id).get();

        repository.delete(object);
    }


    private void fillObject(final Map<String, List<String>> formData, final Object object)
            throws NoSuchFieldException, IllegalAccessException, ParseException {
        final Class<?> entityClass = object.getClass();

        for (final Map.Entry<String, List<String>> entry : formData.entrySet()) {
            final String key = entry.getKey();
            if (key.startsWith(entityClass.getSimpleName())) {
                final String[] split = key.split("\\.");
                final String fieldName = split[1];
                final Field field = FieldAccessPrivilegedAction.getField(entityClass, fieldName);

                final Object fieldValue;
                if (split.length == 3 && split[2].equals("null_value")) {
                    fieldValue = null;
                } else {
                    final Class<?> fieldClass = field.getType();
                    fieldValue = getFieldValue(entityClass, fieldName, fieldClass, entry.getValue());
                }

                field.set(object, fieldValue);
            }
        }
    }

    private Object getFieldValue(final Class<?> aClass, final String fieldName, final Class declaringClass, final List<String> entityValues)
            throws ParseException {
        final Object fieldValue;
        final String value = entityValues.get(0);
        if (List.class.isAssignableFrom(declaringClass)) {
            final Class elementType = entityManagerComponent.getListElementClass(aClass, fieldName);
            if (elementType.isEnum()) {
                List values = new LinkedList();
                for (String entryValue : entityValues) {
                    values.add(Enum.valueOf(elementType, entryValue));
                }
                fieldValue = values;
            } else {
                fieldValue = entityValues;
            }
        } else if (entityManagerComponent.isEntity(declaringClass)) {
            final JpaRepository fieldRepository = getJpaRepository(declaringClass);
            fieldValue = fieldRepository.getOne(Long.parseLong(value));
        } else if (declaringClass.isEnum()) {
            fieldValue = Enum.valueOf(declaringClass, value);
        } else if (declaringClass.equals(Date.class)) {
            fieldValue = DateUtils.parseDate(value, DATE_PARSE_PATTERNS);
        } else if (declaringClass.equals(Long.class) || declaringClass.equals(long.class)) {
            fieldValue = Long.parseLong(value);
        } else if (declaringClass.equals(Integer.class) || declaringClass.equals(int.class)) {
            fieldValue = Integer.parseInt(value);
        } else if (declaringClass.equals(Boolean.class) || declaringClass.equals(boolean.class)) {
            fieldValue = Boolean.parseBoolean(value);
        } else if (declaringClass.equals(Double.class)) {
            fieldValue = Double.parseDouble(value);
        } else if (declaringClass.equals(Float.class)) {
            fieldValue = Float.parseFloat(value);
        } else if (declaringClass.equals(String.class)) {
            fieldValue = value;
        } else {
            throw new IllegalStateException("Type could not be handled!");
        }
        return fieldValue;
    }
}
