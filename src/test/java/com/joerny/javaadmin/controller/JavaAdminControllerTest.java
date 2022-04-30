package com.joerny.javaadmin.controller;

import com.joerny.JavaAdminApplication;
import com.joerny.example.entity.BasicEntity;
import com.joerny.example.entity.BasicEntityRepository;
import com.joerny.example.entity.ChildEntity;
import com.joerny.example.entity.ChildEntityRepository;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.joerny.example.entity.SimpleEnum;
import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ActiveProfiles(profiles = "test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = JavaAdminApplication.class)
@WebAppConfiguration
public class JavaAdminControllerTest {
    private static final String OVERVIEW_URI = "/java-admin/overview";
    private static final String OVERVIEW_JSP_URL = "/java-admin/overview.jsp";

    private static final String LIST_URI = "/java-admin/list/";
    private static final String LIST_JSP_URL = "/java-admin/list.jsp";

    private static final String CREATE_URI = "/java-admin/create/";
    private static final String CREATE_JSP_URL = "/java-admin/create.jsp";

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private ChildEntityRepository childEntityRepository;
    @Autowired
    private BasicEntityRepository basicEntityRepository;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    public MockMvc getMockMvc() {
        return mockMvc;
    }

    @Test
    public void overview() throws Exception {
        final ResultActions getResult = getMockMvc().perform(MockMvcRequestBuilders.get(OVERVIEW_URI));
        getResult.andExpect(MockMvcResultMatchers.forwardedUrl(OVERVIEW_JSP_URL));
        getResult.andExpect(MockMvcResultMatchers.model().attribute("entities", Matchers.any(Collection.class)));
        getResult.andExpect(MockMvcResultMatchers.model().attribute("entities", Matchers.hasSize(2)));
    }

    @Test
    public void list() throws Exception {
        final LinkedList<SimpleEnum> enumList = new LinkedList<>();
        enumList.add(SimpleEnum.TRES);
        enumList.add(SimpleEnum.DUO);

        final BasicEntity basicEntity = new BasicEntity();
        basicEntity.setSimpleText("listTest");
        basicEntity.setSimpleFloat(2.3f);
        basicEntity.setSimpleEnum(enumList);

        basicEntityRepository.save(basicEntity);

        final ResultActions getResult = getMockMvc().perform(MockMvcRequestBuilders.get(LIST_URI + BasicEntity.class.getSimpleName()));
        getResult.andExpect(MockMvcResultMatchers.forwardedUrl(LIST_JSP_URL));
        getResult.andExpect(MockMvcResultMatchers.model().attribute("command", Matchers.notNullValue()));

        final JavaAdminListCommand command = (JavaAdminListCommand) getResult.andReturn().getModelAndView().getModel().get("command");
        Assert.assertEquals("BasicEntity", command.getEntityName());

        final Map<String, Map<String, List<String>>> entities = command.getEntities();

        Assert.assertEquals(1, entities.size());

        final Map<String, List<String>> testEntity = entities.entrySet().iterator().next().getValue();
        Assert.assertEquals(7, testEntity.size());

        Assert.assertEquals("listTest", testEntity.get("simpleText").get(0));
        Assert.assertEquals("[TRES, DUO]", ArrayUtils.toString(testEntity.get("simpleEnum")));
    }

    @Test
    public void createGet() throws Exception {
        final ResultActions getResult = getMockMvc().perform(MockMvcRequestBuilders.get(CREATE_URI + BasicEntity.class.getSimpleName()));
        getResult.andExpect(MockMvcResultMatchers.forwardedUrl(CREATE_JSP_URL));
        getResult.andExpect(MockMvcResultMatchers.request().attribute("entityName", Matchers.equalTo(BasicEntity.class.getSimpleName())));
    }

    @Test
    public void createSimplePost() throws Exception {
        final long count = basicEntityRepository.count();

        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(CREATE_URI + BasicEntity.class.getSimpleName());
        request.contentType(MediaType.APPLICATION_FORM_URLENCODED);
        request.param(BasicEntity.class.getSimpleName() + ".simpleText", "test");
        request.param(BasicEntity.class.getSimpleName() + ".simpleDouble", "3.5");
        request.param(BasicEntity.class.getSimpleName() + ".simpleFloat", "2.7");
        request.param(BasicEntity.class.getSimpleName() + ".simpleEnum", "DUO", "TRES");

        final ResultActions getResult = getMockMvc().perform(request);
        getResult.andExpect(MockMvcResultMatchers.status().is(302));
        getResult.andExpect(MockMvcResultMatchers.redirectedUrl(LIST_URI + BasicEntity.class.getSimpleName()));

        Assert.assertEquals(count + 1, basicEntityRepository.count());
    }

    @Test
    public void createPost() throws Exception {
        final long count = childEntityRepository.count();

        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(CREATE_URI + ChildEntity.class.getSimpleName());
        request.contentType(MediaType.APPLICATION_FORM_URLENCODED);
        request.param(ChildEntity.class.getSimpleName() + ".text", "test");

        final ResultActions getResult = getMockMvc().perform(request);
        getResult.andExpect(MockMvcResultMatchers.status().is(302));
        getResult.andExpect(MockMvcResultMatchers.redirectedUrl(LIST_URI + ChildEntity.class.getSimpleName()));

        Assert.assertEquals(count + 1, childEntityRepository.count());
    }
}