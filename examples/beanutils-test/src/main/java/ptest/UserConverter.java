package ptest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserConverter implements BeanConverter {

	public <T extends Map<String, Object>, C> List<C> convertMapToBean(
			List<T> list, Class<C> clazz) {

		List<C> beanList = new ArrayList<C>();

		for (Map<String, Object> map : list) {
			User user = new User();
			user.setName((String)map.get("name"));
			user.setName1((String)map.get("name1"));
			user.setName2((String)map.get("name2"));
			user.setName3((String)map.get("name3"));
			user.setName4((String)map.get("name4"));
			user.setName5((String)map.get("name5"));
			user.setName6((String)map.get("name6"));
			user.setName7((String)map.get("name7"));
			user.setName8((String)map.get("name8"));
			user.setName9((String)map.get("name9"));
			user.setName10((String)map.get("name10"));
			user.setAddress((String)map.get("address"));
			user.setAge((Integer)map.get("age"));
			user.setId((Integer)map.get("id"));
			user.setIncome((BigDecimal)map.get("income"));
			user.setIntroduce((String)map.get("introduce"));
			user.setNickName((String)map.get("nickName"));
			user.setMarried((Boolean)map.get("married"));

			@SuppressWarnings("unchecked")
			C bean = (C)user;
			beanList.add(bean);
		}
		return beanList;
	}
}