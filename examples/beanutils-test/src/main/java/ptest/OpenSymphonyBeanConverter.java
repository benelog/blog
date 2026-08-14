package ptest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.opensymphony.util.BeanUtils;

public class OpenSymphonyBeanConverter implements BeanConverter{

	public <T extends Map<String, Object>, C> List<C> convertMapToBean(
			List<T> list, Class<C> targetClass) {
		List<C> beanList = new ArrayList<C>();

		for (Map<String, Object> map : list) {
			C bean = null;
			try {
				bean = targetClass.newInstance();
				BeanUtils.setValues(bean, map, null);
			} catch (InstantiationException e) {
				new IllegalArgumentException("Cannot initiate class", e);
			} catch (IllegalAccessException e) {
				new IllegalStateException("Cannot access the property", e);
			}
			beanList.add(bean);
		}
		return beanList;
	}
}
