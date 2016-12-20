package com.joerny.javaadmin.controller;

import com.joerny.JavaAdminApplication;
import com.joerny.example.entity.BasicEntity;

import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
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

    @Autowired
    private WebApplicationContext wac;
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
        getResult.andExpect(MockMvcResultMatchers.model().attribute("entities", Matchers.any(Set.class)));
        getResult.andExpect(MockMvcResultMatchers.model().attribute("entities", Matchers.hasSize(1)));
    }

    @Test
    public void list() throws Exception {
        final ResultActions getResult = getMockMvc().perform(MockMvcRequestBuilders.get(LIST_URI + BasicEntity.class.getSimpleName()));
        getResult.andExpect(MockMvcResultMatchers.forwardedUrl(LIST_JSP_URL));
        getResult.andExpect(MockMvcResultMatchers.model().attribute("command", Matchers.notNullValue()));
    }
}