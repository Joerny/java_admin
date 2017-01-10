package com.joerny.javaadmin.controller;

import com.joerny.javaadmin.service.JavaAdminService;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/java-admin")
public class JavaAdminController {
    private static final String[] DATE_PARSE_PATTERNS = {"dd.MM.yyyy", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.S"};

    @Autowired
    private JavaAdminService javaAdminService;

    @GetMapping("/overview")
    public String overview(final Model model) {
        model.addAttribute("entities", javaAdminService.getEntityNames());

        return "java-admin/overview";
    }

    @GetMapping("/list/{entityName}")
    public String list(@PathVariable final String entityName, final Model model) throws NoSuchFieldException, IllegalAccessException {
        final Collection<String> attributeNames = javaAdminService.getSingleAttributeNames(entityName);
        final Collection<String> idNames = javaAdminService.getIdAttributeNames(entityName);
        final Collection<String> multiAttributeNames = javaAdminService.getMultipleAttributeNames(entityName);

        final Map<String, Map<String, List<String>>> entities = javaAdminService.getEntityValues(entityName, idNames, attributeNames, multiAttributeNames);

        final List<String> names = new LinkedList<>(idNames);
        names.addAll(attributeNames);
        names.addAll(multiAttributeNames);

        final JavaAdminListCommand command = new JavaAdminListCommand();
        command.setEntityName(entityName);
        command.setAttributeNames(names);
        command.setEntities(entities);

        model.addAttribute("command", command);

        return "java-admin/list";
    }

    @GetMapping("/create/{entityName}")
    public String create(@PathVariable final String entityName, final Model model) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        model.addAttribute("childEntities", javaAdminService.getChildEntities(entityName));
        model.addAttribute("fields", javaAdminService.getFieldValues(entityName));

        return "java-admin/create";
    }

    @GetMapping(value = "/delete/{entityName}/{id}")
    public String delete(@PathVariable String entityName, @PathVariable Long id) throws NoSuchFieldException, IllegalAccessException {
        final Type<?> entity = javaAdminService.getEntityType(entityName);

        final JpaRepository repository = javaAdminService.getJpaRepository(entity);

        final Object object = repository.findOne(id);

        repository.delete(object);

        return "redirect:/java-admin/list/" + entityName;
    }

    @PostMapping(value = "/edit/{entityName}/{id}")
    public String edit(@PathVariable String entityName, @PathVariable Long id, @RequestBody MultiValueMap<String,String> formData) throws NoSuchFieldException, IllegalAccessException, ParseException {
        final EntityType<?> entity = javaAdminService.getEntityType(entityName);

        final JpaRepository repository = javaAdminService.getJpaRepository(entity);

        Object object = repository.findOne(id);

        fillObject(formData, entity, object);

        repository.saveAndFlush(object);

        return "redirect:/java-admin/list/" + entityName;
    }

    @PostMapping(value = "/create/{entityName}")
    public String create(@PathVariable String entityName, @RequestBody MultiValueMap<String,String> formData) throws NoSuchFieldException, IllegalAccessException, InstantiationException, ParseException {
        final EntityType<?> entity = javaAdminService.getEntityType(entityName);

        final JpaRepository repository = javaAdminService.getJpaRepository((EntityType<?>) entity);

        Object object = entity.getJavaType().newInstance();

        fillObject(formData, entity, object);

        repository.save(object);

        return "redirect:/java-admin/list/" + entityName;
    }

    private void fillObject(MultiValueMap<String, String> formData, EntityType<?> entity, Object object) throws NoSuchFieldException, IllegalAccessException, ParseException {
        final Class<?> aClass = object.getClass();

        for (final Map.Entry<String, List<String>> entry : formData.entrySet()) {
            final String key = entry.getKey();
            if (key.startsWith(aClass.getSimpleName())) {
                final String[] split = key.split("\\.");
                final Attribute attribute = entity.getAttribute(split[1]);
                final Field field = JavaAdminService.getField(aClass, attribute.getName());
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
                    } else if (javaAdminService.isEntity(declaringClass)) {
                        final JpaRepository fieldRepository = javaAdminService.getJpaRepository(declaringClass);
                        final Object child = fieldRepository.getOne(Long.parseLong(value));
                        field.set(object, child);
                    } else {
                        throw new IllegalStateException("Type could not be handled!");
                    }
                }
            }
        }
    }

    @GetMapping(value = "/edit/{entityName}/{id}")
    public String show(@PathVariable final String entityName, @PathVariable final Long id, final Model model) throws NoSuchFieldException, IllegalAccessException {
        final EntityType<?> entity = javaAdminService.getEntityType(entityName);

        final JpaRepository repository = javaAdminService.getJpaRepository(entity);

        final Object object = repository.findOne(id);

        final Map<String, List<EntityInformation>> childEntities = new HashMap<>();

        final Map<String, Object> fields = new HashMap<>();
        final Class<?> aClass = object.getClass();
        for (final Object attr : entity.getDeclaredSingularAttributes()) {
            final SingularAttribute attribute = (SingularAttribute) attr;
            final Field field = JavaAdminService.getField(aClass, attribute.getName());
            if (!attribute.isId()) {
                fields.put(attribute.getName(), Objects.toString(field.get(object), null));
            }
            if (javaAdminService.isEntity(field.getType())) {
                final List<EntityInformation> childInformation = javaAdminService.getEntityInformation(field.getType());
                childEntities.put(attribute.getName(), childInformation);
            }
        }

        for (final Object attr : entity.getDeclaredPluralAttributes()) {
            final PluralAttribute attribute = (PluralAttribute) attr;
            final Field field = JavaAdminService.getField(aClass, attribute.getName());

            final List<String> values = new LinkedList<>();

            if (Collection.class.isAssignableFrom(object.getClass())) {
                final Collection collection = (Collection) field.get(object);
                for (Object value : collection) {
                    values.add(Objects.toString(value));
                }
            }

            fields.put(attribute.getName(), values);
        }

        model.addAttribute("childEntities", childEntities);
        model.addAttribute("fields", fields);
        model.addAttribute("entityName", entity.getName());

        return "java-admin/edit";
    }

}
