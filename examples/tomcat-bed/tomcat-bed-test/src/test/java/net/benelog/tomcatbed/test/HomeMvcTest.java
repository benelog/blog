package net.benelog.tomcatbed.test;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({ 
	@ContextConfiguration({ "/spring/applicationContext.xml" }),
	@ContextConfiguration("/spring/mvc-config.xml")
})
public class HomeMvcTest {
	@Autowired
	private WebApplicationContext wac;

	private MockMvc mvc;

	@Before
	public void setup() {
		this.mvc = webAppContextSetup(this.wac).build();
	}

	@Test
	public void home() throws Exception {
	    mvc.perform(get("/"))
		   .andExpect(status().isOk())
		   .andExpect(view().name("home"));
	}

	@Test
	public void imageJson() throws Exception {
	    mvc.perform(get("/viewImage/cloud.json"))
		   .andExpect(status().isOk())
		   	.andExpect(content().string("{\"src\":\"/img/cloud.png\",\"height\":64,\"width\":64}"));
	}

	@Test
	public void containsImagePath() throws Exception {
	    mvc.perform(get("/viewImage/phone.json"))
		   .andExpect(status().isOk())
		   	.andExpect(content().string(containsString("/img/phone.png")));
	}
}
