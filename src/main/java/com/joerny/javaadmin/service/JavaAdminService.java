package com.joerny.javaadmin.service;

import com.joerny.javaadmin.controller.EntityInformation;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.util.*;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.WebApplicationContext;

@Service
public class JavaAdminService {
    private static final String[] DATE_PARSE_PATTERNS = {"dd.MM.yyyy", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.S"};

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

    public Map<String, List<?>> getChildEntities(final String entityName) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        final EntityType<?> entity = getEntityType(entityName);

        final Map<String, List<?>> childEntities = new HashMap<>();

        final Set attributes = entity.getDeclaredSingularAttributes();

        for (final Object attr : attributes) {
            final SingularAttribute attribute = (SingularAttribute) attr;

            if (isEntity(attribute.getJavaType())) {
                final List<EntityInformation> childInformation = getEntityInformation(attribute.getJavaType());
                childEntities.put(attribute.getName(), childInformation);
            }
        }

        return childEntities;
    }

    public JpaRepository<?, ?> getJpaRepository(final Type<?> entity) {
        return getJpaRepository(entity.getJavaType());
    }

    private JpaRepository<?, ?> getJpaRepository(final Class<?> javaType) {
        final Repositories repositories = new Repositories(appContext);

        return (JpaRepository<?, ?>) repositories.getRepositoryFor(javaType);
    }

    public static Field getField(final Class<?> aClass, final String name) throws NoSuchFieldException {
        final Field field = aClass.getDeclaredField(name); //NoSuchFieldException
        AccessController.doPrivileged(new FieldAccessPrivilegedAction(field));
        return field;
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

    public List<EntityInformation> getEntityInformation(final Class<?> javaType) throws NoSuchFieldException, IllegalAccessException {
        final JpaRepository fieldRepository = getJpaRepository(javaType);
        final List children = fieldRepository.findAll();
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
            final Field field = getField(javaType, id.getName());
            information.add(new EntityInformation(field.get(child), child.toString()));
        }
        return information;
    }

    public Map<String, Object> getFieldValues(@PathVariable final String entityName) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        final EntityType<?> entity = getEntityType(entityName);

        final Object object = entity.getJavaType().newInstance();

        final Map<String, Object> fields = new HashMap<>();
        final Set attributes = entity.getDeclaredSingularAttributes();
        final Class<?> aClass = object.getClass();
        for (final Object attr : attributes) {
            final SingularAttribute attribute = (SingularAttribute) attr;
            final Field field = getField(aClass, attribute.getName());
            if (!attribute.isId()) {
                fields.put(attribute.getName(), Objects.toString(field.get(object), null));
            }
        }

        final Set multiAttributes = entity.getDeclaredPluralAttributes();
        for (final Object attr : multiAttributes) {
            final Attribute attribute = (Attribute) attr;
            fields.put(attribute.getName(), new LinkedList());
        }

        return fields;
    }

    public void saveEntity(final String entityName, final Map<String, List<String>> formData)
            throws InstantiationException, IllegalAccessException, NoSuchFieldException, ParseException {
        final EntityType<?> entity = getEntityType(entityName);

        final JpaRepository repository = getJpaRepository(entity);

        final Object object = entity.getJavaType().newInstance();

        fillObject(formData, entity, object);

        repository.save(object);
    }

    public void fillObject(final Map<String, List<String>> formData, final EntityType<?> entity, final Object object) throws NoSuchFieldException, IllegalAccessException, ParseException {
        final Class<?> aClass = object.getClass();

        for (final Map.Entry<String, List<String>> entry : formData.entrySet()) {
            final String key = entry.getKey();
            if (key.startsWith(aClass.getSimpleName())) {
                final String[] split = key.split("\\.");
                final Attribute attribute = entity.getAttribute(split[1]);
                final Field field = getField(aClass, attribute.getName());
                final String value = entry.getValue().get(0);

                if (split.length == 3 && split[2].equals("null_value")) {
                    field.set(object, null);
                } else {
                    final Class<?> declaringClass = field.getType();
                    if (declaringClass.equals(Date.class)) {
                        field.set(object, DateUtils.parseDate(value, DATE_PARSE_PATTERNS));
                    } else if (declaringClass.equals(Long.class) || declaringClass.equals(long.class)) {
                        field.set(object, Long.parseLong(value));
                    } else if (declaringClass.equals(Integer.class) || declaringClass.equals(int.class)) {
                        field.set(object, Integer.parseInt(value));
                    } else if (declaringClass.equals(Boolean.class) || declaringClass.equals(boolean.class)) {
                        field.set(object, Boolean.parseBoolean(value));
                    } else if (List.class.isAssignableFrom(declaringClass)) {
                        final ListAttribute listAttribute = entity.getDeclaredList(attribute.getName());
                        if (listAttribute.getElementType().getJavaType().isEnum()) {
                            List values = new LinkedList();
                            for (String entryValue : entry.getValue()) {
                                values.add(Enum.valueOf(listAttribute.getElementType().getJavaType(), entryValue));
                            }
                            field.set(object, values);
                        } else {
                            field.set(object, entry.getValue());
                        }
                    } else if (declaringClass.equals(String.class)) {
                        field.set(object, value);
                    } else if (isEntity(declaringClass)) {
                        final JpaRepository fieldRepository = getJpaRepository(declaringClass);
                        final Object child = fieldRepository.getOne(Long.parseLong(value));
                        field.set(object, child);
                    } else {
                        throw new IllegalStateException("Type could not be handled!");
                    }
                }
            }
        }
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
