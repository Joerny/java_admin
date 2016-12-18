package com.joerny.javaadmin.controller;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
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
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

@Controller
@RequestMapping("/java-admin")
public class JavaAdminController {
    private static final String[] DATE_PARSE_PATTERNS = {"dd.MM.yyyy", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.S"};

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private WebApplicationContext appContext;

    @GetMapping(value = "/overview")
    public String overview(Model model) {
        Set<EntityType<?>> entities = entityManagerFactory.getMetamodel().getEntities();

        model.addAttribute("entities", entities);

        return "java-admin/overview";
    }

    @GetMapping(value = "/list/{entityName}")
    public String list(@PathVariable final String entityName, final Model model) throws NoSuchFieldException, IllegalAccessException {
        final EntityType<?> entity = getEntityType(entityName);

        final JpaRepository repository = getJpaRepository(entity);

        final List<?> data = repository.findAll();

        final List<String> attributeNames = new LinkedList<>();
        final List<String> idNames = new LinkedList<>();

        for (Object attr : entity.getDeclaredSingularAttributes()) {
            SingularAttribute attribute = (SingularAttribute) attr;
            if (attribute.isId()) {
                idNames.add(attribute.getName());
            } else {
                attributeNames.add(attribute.getName());
            }
        }

        final List<String> multiAttributeNames = new LinkedList<>();
        for (Object attr : entity.getDeclaredPluralAttributes()) {
            PluralAttribute attribute = (PluralAttribute) attr;
            multiAttributeNames.add(attribute.getName());
        }

        final Map<String, Map<String, List<String>>> entities = new LinkedHashMap<>();
        for (Object datum : data) {
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
                    Collection collection = (Collection) object;
                    for (Object value : collection) {
                        values.add(Objects.toString(value));
                    }
                }

                entityValues.put(name, values);
            }

            entities.put(id, entityValues);
        }


        final List names = new LinkedList(idNames);
        names.addAll(attributeNames);
        names.addAll(multiAttributeNames);

        final JavaAdminListCommand command = new JavaAdminListCommand();
        command.setEntityName(entity.getName());
        command.setAttributeNames(names);
        command.setEntities(entities);

        model.addAttribute("command", command);

        return "java-admin/list";
    }

    @GetMapping(value = "/delete/{entityName}/{id}")
    public String delete(@PathVariable String entityName, @PathVariable Long id) throws NoSuchFieldException, IllegalAccessException {
        final EntityType<?> entity = getEntityType(entityName);

        final JpaRepository repository = getJpaRepository(entity);

        Object object = repository.findOne(id);

        repository.delete(object);

        return "redirect:/java-admin/list/" + entityName;
    }

    @PostMapping(value = "/edit/{entityName}/{id}")
    public String edit(@PathVariable String entityName, @PathVariable Long id, @RequestBody MultiValueMap<String,String> formData) throws NoSuchFieldException, IllegalAccessException, ParseException {
        final EntityType<?> entity = getEntityType(entityName);

        final JpaRepository repository = getJpaRepository(entity);

        Object object = repository.findOne(id);

        fillObject(formData, entity, object);

        repository.saveAndFlush(object);

        return "redirect:/java-admin/list/" + entityName;
    }

    @PostMapping(value = "/create/{entityName}")
    public String create(@PathVariable String entityName, @RequestBody MultiValueMap<String,String> formData) throws NoSuchFieldException, IllegalAccessException, InstantiationException, ParseException {
        final EntityType<?> entity = getEntityType(entityName);

        final JpaRepository repository = getJpaRepository(entity);

        Object object = entity.getJavaType().newInstance();

        fillObject(formData, entity, object);

        repository.save(object);

        return "redirect:/java-admin/list/" + entityName;
    }

    private void fillObject(@RequestBody MultiValueMap<String, String> formData, EntityType<?> entity, Object object) throws NoSuchFieldException, IllegalAccessException, ParseException {
        final Class<?> aClass = object.getClass();

        for (Map.Entry<String, List<String>> entry : formData.entrySet()) {
            final String key = entry.getKey();
            if (key.startsWith(aClass.getSimpleName())) {
                final String[] split = key.split("\\.");
                Attribute attribute = entity.getAttribute(split[1]);
                final Field field = getField(aClass, attribute.getName());
                final String value = entry.getValue().get(0);

                if (!(field.get(object) == null && value.equals("null"))) {
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
                    } else {
                        field.set(object, value);
                    }
                }
            }
        }
    }

    @GetMapping(value = "/create/{entityName}")
    public String create(@PathVariable final String entityName, final Model model) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        final EntityType<?> entity = getEntityType(entityName);

        final Object object = entity.getJavaType().newInstance();

        final Map<String, Object> fields = new HashMap<>();
        final Set attributes = entity.getDeclaredSingularAttributes();
        final Class<?> aClass = object.getClass();
        for (final Object attr : attributes) {
            final SingularAttribute attribute = (SingularAttribute) attr;
            final Field field = getField(aClass, attribute.getName());
            if (attribute.isId()) {
                model.addAttribute("id", field.get(object));
            } else {
                fields.put(attribute.getName(), Objects.toString(field.get(object)));
            }
        }

        final Set multiAttributes = entity.getDeclaredPluralAttributes();
        for (final Object attr : multiAttributes) {
            PluralAttribute attribute = (PluralAttribute) attr;
            fields.put(attribute.getName(), new LinkedList());
        }

        model.addAttribute("fields", fields);
        model.addAttribute("entity", entity);

        return "java-admin/create";
    }

    private Field getField(Class<?> aClass, String name) throws NoSuchFieldException {
        final Field field = aClass.getDeclaredField(name); //NoSuchFieldException
        AccessController.doPrivileged(new FieldAccessPrivilegedAction(field));
        return field;
    }

    @GetMapping(value = "/edit/{entityName}/{id}")
    public String show(@PathVariable String entityName, @PathVariable Long id, Model model) throws NoSuchFieldException, IllegalAccessException {
        final EntityType<?> entity = getEntityType(entityName);

        final JpaRepository repository = getJpaRepository(entity);

        Object object = repository.findOne(id);

        final Map<String, Object> fields = new HashMap<>();
        final Class<?> aClass = object.getClass();
        for (Object attr : entity.getDeclaredSingularAttributes()) {
            SingularAttribute attribute = (SingularAttribute) attr;
            final Field field = getField(aClass, attribute.getName());
            if (!attribute.isId()) {
                fields.put(attribute.getName(), Objects.toString(field.get(object)));
            }
        }

        for (Object attr : entity.getDeclaredPluralAttributes()) {
            PluralAttribute attribute = (PluralAttribute) attr;
            final Field field = getField(aClass, attribute.getName());

            final List<String> values = new LinkedList<>();

            if (Collection.class.isAssignableFrom(object.getClass())) {
                final Collection collection = (Collection) field.get(object);
                for (Object value : collection) {
                    values.add(Objects.toString(value));
                }
            }

            fields.put(attribute.getName(), values);
        }

        model.addAttribute("fields", fields);
        model.addAttribute("entityName", entity.getName());

        return "java-admin/edit";
    }

    private JpaRepository getJpaRepository(EntityType<?> entity) {
        Repositories repositories = new Repositories(appContext);

        return (JpaRepository) repositories.getRepositoryFor(entity.getJavaType());
    }

    private EntityType<?> getEntityType(final String entityName) {
        final Set<EntityType<?>> entities = entityManagerFactory.getMetamodel().getEntities();

        EntityType<?> entity = null;
        for (EntityType<?> entityTmp : entities) {
            if (entityTmp.getName().equals(entityName)) {
                entity = entityTmp;
            }
        }
        return entity;
    }

    private static class FieldAccessPrivilegedAction implements PrivilegedAction {
        private final Field field;

        public FieldAccessPrivilegedAction(Field field) {
            this.field = field;
        }

        @Override
        public Object run() {
            field.setAccessible(true);
            return null;
        }
    }
}
