package com.joerny.javaadmin.controller;

import com.joerny.javaadmin.service.JavaAdminService;

import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
    public String create(@PathVariable final String entityName, final Model model)
            throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        model.addAttribute("childEntities", javaAdminService.getChildEntities(entityName));
        model.addAttribute("fields", javaAdminService.getFieldValues(entityName));

        return "java-admin/create";
    }

    @PostMapping("/create/{entityName}")
    public String create(@PathVariable final String entityName, @RequestBody final MultiValueMap<String,String> formData)
            throws NoSuchFieldException, IllegalAccessException, InstantiationException, ParseException {
        javaAdminService.saveEntity(entityName, formData);

        return "redirect:/java-admin/list/" + entityName;
    }

    @GetMapping("/edit/{entityName}/{id}")
    public String show(@PathVariable final String entityName, @PathVariable final Long id, final Model model)
            throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        model.addAttribute("childEntities", javaAdminService.getChildEntities(entityName));
        model.addAttribute("fields", javaAdminService.getFieldValues(entityName, id));

        return "java-admin/edit";
    }

    @PostMapping("/edit/{entityName}/{id}")
    public String edit(@PathVariable final String entityName, @PathVariable final Long id, @RequestBody final MultiValueMap<String,String> formData)
            throws NoSuchFieldException, IllegalAccessException, ParseException {
        javaAdminService.saveEntity(entityName, id, formData);

        return "redirect:/java-admin/list/" + entityName;
    }

    @GetMapping("/delete/{entityName}/{id}")
    public String delete(@PathVariable final String entityName, @PathVariable final Long id) throws NoSuchFieldException, IllegalAccessException {
        javaAdminService.deleteEntity(entityName, id);

        return "redirect:/java-admin/list/" + entityName;
    }
}
