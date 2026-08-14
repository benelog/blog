package net.benelog.tomcatbed.test;

import static net.sourceforge.jwebunit.junit.JWebUnit.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext-ui-test.xml"})
public class ZipCodePageTest {
	@Value("#{testServer.baseUrl}")
	String baseUrl;

	@Before
   public void prepare() {
		setBaseUrl(baseUrl);
		setScriptingEnabled(false);
    }

	@Test
	public void zipCodeForm() {
		beginAt("/zipCodeForm"); 
		assertFormPresent();
		assertSubmitButtonPresent();
		
		setTextField("zipCode", "121-270");
	   submit();

	   assertResponseCode(HttpStatus.OK.value());
		assertTitleEquals("Address");
		assertElementPresent("address");
		assertMatch("서울특별시 마포구 상암동");
		System.out.println(getPageSource());
	}
}
