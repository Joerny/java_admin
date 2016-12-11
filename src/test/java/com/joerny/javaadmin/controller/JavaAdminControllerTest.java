package com.joerny.javaadmin.controller;

import com.joerny.JavaAdminApplication;

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

    @Autowired
    private WebApplicationContext _wac;
    private MockMvc _mockMvc;

    @Before
    public void setUp() {
        _mockMvc = MockMvcBuilders.webAppContextSetup(_wac).build();
    }

    public MockMvc getMockMvc() {
        return _mockMvc;
    }

    @Test
    public void overview() throws Exception {
        final ResultActions getResult = getMockMvc().perform(MockMvcRequestBuilders.get(OVERVIEW_URI));
        getResult.andExpect(MockMvcResultMatchers.forwardedUrl(OVERVIEW_JSP_URL));
    }
}