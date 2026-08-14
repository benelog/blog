package ptest;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class BeanConverterTest {
	
	@Test
	public void testApacheCommonsBeanUtils(){
		BeanConverter converter = new ApacheCommonsBeanUtilsBeanConverter();
		List<User> beanList = converter.convertMapToBean(createMapListForTest(),User.class);
		assertConverted(beanList);				
	}

	@Test
	public void testApacheCommonsPropertyUtils(){
		BeanConverter converter = new ApacheCommonsPropertyUtilsBeanConverter();
		List<User> beanList = converter.convertMapToBean(createMapListForTest(),User.class);
		assertConverted(beanList);				
	}
	
	@Test
	public void testOpensymphony(){
		BeanConverter converter = new OpenSymphonyBeanConverter();
		List<User> beanList = converter.convertMapToBean(createMapListForTest(),User.class);
		assertConverted(beanList);				
	}
	
	@Test
	public void testByHand(){
		BeanConverter converter = new UserConverter();
		List<User> beanList = converter.convertMapToBean(createMapListForTest(),User.class);
		assertConverted(beanList);				
	}
	
	@Test
	public void testSpring(){
		BeanConverter converter = new SpringBeanConverter();
		List<User> beanList = converter.convertMapToBean(createMapListForTest(),User.class);
		assertConverted(beanList);				
	}

	private void assertConverted(List<User> beanList) {
		User converted = beanList.get(0);
		assertEquals(1,converted.getId());
		assertEquals(1,converted.getAge());
		assertEquals("내이름",converted.getName());
		assertEquals("내이름",converted.getName10());
		assertEquals("뻐꾸기",converted.getNickName());		
		assertEquals(new BigDecimal("1000100100"),converted.getIncome());
	}
	
	private List<Map<String, Object>> createMapListForTest() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> user = new HashMap<String, Object>();
		user.put("id", 1);
		user.put("age", 1);
		user.put("name", "내이름");
		user.put("name1", "내이름");
		user.put("name2", "내이름");
		user.put("name3", "내이름");
		user.put("name4", "내이름");
		user.put("name5", "내이름");
		user.put("name6", "내이름");
		user.put("name7", "내이름");
		user.put("name8", "내이름");
		user.put("name9", "내이름");
		user.put("name10", "내이름");
		user.put("income", new BigDecimal("1000100100"));
		user.put("address", "오늘 아침 내가 행복한 이유는 이런거지 오늘아침 내가 서러운 이유는 그런거야 ");
		user.put("introduce", "오늘 아침 내가 행복한 이유는 이런거지 오늘아침 내가 서러운 이유는 그런거야 ");
		user.put("married", true);
		user.put("nickName", "뻐꾸기");
		list.add(user);
		return list;
	}
}
