package ptest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class BeanToPerformanceTest {

	@Test
	public void testApacheCommonsBeanUtils() {
		BeanConverter converter = new ApacheCommonsBeanUtilsBeanConverter();
		executeIncrementally(converter);
	}
	
	@Test
	public void testApacheCommonsPropertyUtils() {
		BeanConverter converter = new ApacheCommonsPropertyUtilsBeanConverter();
		executeIncrementally(converter);
	}

	@Test
	public void testOpenSymphony() {
		BeanConverter converter = new OpenSymphonyBeanConverter();
		executeIncrementally(converter);
	}

	@Test
	public void testSpring() {
		BeanConverter converter = new SpringBeanConverter();
		executeIncrementally(converter);
	}
	
	@Test
	public void testByHand() {
		BeanConverter converter = new UserConverter();
		executeIncrementally(converter);
	}
	
	private void excuecteBeanConverter(BeanConverter converter, int iterations) {
		List<Map<String, Object>> testList = createMapListForTest(iterations);
		long start = System.currentTimeMillis();
		List<User> beanList = converter.convertMapToBean(testList, User.class);
		long end = System.currentTimeMillis();
		System.out.printf("%s,%d times, %d milliseconds \r\n", converter
				.getClass().getSimpleName(), iterations, (end - start));
	}

	private void executeIncrementally(BeanConverter converter) {
		for (int i = 0; i <= 100000; i += 10000) {
			excuecteBeanConverter(converter, i);
		}
	}

	private List<Map<String, Object>> createMapListForTest(int iterations) {
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
		for (int i = 0; i < iterations; i++) {
			list.add(user);
		}
		return list;
	}
}
