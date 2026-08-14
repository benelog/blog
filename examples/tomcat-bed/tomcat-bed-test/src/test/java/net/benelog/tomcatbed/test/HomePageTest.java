package net.benelog.tomcatbed.test;

import static net.sourceforge.jwebunit.junit.JWebUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import net.benelog.tomcatbed.domain.Image;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext-ui-test.xml"})
public class HomePageTest {
	@Value("#{testServer.baseUrl}")
	String baseUrl;

	@Before
    public void prepare() {
		setBaseUrl(baseUrl);
		setScriptingEnabled(false);
    }

	@Test
	public void home() {
		beginAt("/"); 

		assertResponseCode(HttpStatus.OK.value());
		assertButtonPresent("computer");
		assertButtonPresent("cloud");
		assertButtonPresent("code");
		System.out.println(getPageSource());
	}

	@Test
	public void imageRequest() {
		RestTemplate restClient = new RestTemplate();
		Image image = restClient.getForObject(baseUrl+"viewImage/{imageId}.json", Image.class, "cloud");
		assertThat(image.getSrc(), is("/img/cloud.png"));
	}
}
