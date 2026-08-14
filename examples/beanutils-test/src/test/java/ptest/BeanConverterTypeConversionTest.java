package ptest;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class BeanConverterTypeConversionTest {
	
	
	@Test
	public void testTypeConversionApacheCommonsBeanUtils(){
		BeanConverter converter = new ApacheCommonsBeanUtilsBeanConverter();
		List<User> beanList = converter.convertMapToBean(createTestDataForTypeConversion(),User.class);
		User converted = beanList.get(0);
		assertConverted(converted);
	}
	@Test
	public void testTypeConversionApacheCommonsPropertyUtils(){
		BeanConverter converter = new ApacheCommonsPropertyUtilsBeanConverter();
		List<User> beanList = converter.convertMapToBean(createTestDataForTypeConversion(),User.class);
		User converted = beanList.get(0);
		assertConverted(converted);
	}
	@Test
	public void testTypeConversionOpenSympony(){
		BeanConverter converter = new OpenSymphonyBeanConverter();
		List<User> beanList = converter.convertMapToBean(createTestDataForTypeConversion(),User.class);
		User converted = beanList.get(0);
		assertConverted(converted);
	}
	
	@Test
	public void testTypeConversionSpring(){
		BeanConverter converter = new SpringBeanConverter();
		List<User> beanList = converter.convertMapToBean(createTestDataForTypeConversion(),User.class);
		User converted = beanList.get(0);
		assertConverted(converted);
	}

	private List<Map<String, Object>>  createTestDataForTypeConversion() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> user = new HashMap<String, Object>();
		user.put("id", "1");
		user.put("age", "23");
		user.put("income", "3333");
		list.add(user);
		return list;
	}
	
	private void assertConverted(User converted) {
		assertEquals(1,converted.getId());
		assertEquals(23,converted.getAge());
		assertEquals(new BigDecimal("3333"), converted.getIncome());
	}

}
