package ptest;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

public class ApacheCommonsBeanUtilsBeanConverter implements BeanConverter {

	public <T extends Map<String,Object>, C> List<C> convertMapToBean(List<T> list,
			Class<C> clazz) {
		List<C> beanList = new ArrayList<C>();

		for (T item : list) {
			C bean = null;
			try {
				bean = clazz.newInstance();
				BeanUtils.populate(bean, item);
			} catch (InstantiationException e) {
				new IllegalArgumentException("Cannot initiate class",e);
			} catch (IllegalAccessException e) {
				new IllegalStateException("Cannot access the property",e);
			} catch (InvocationTargetException e) {
				new IllegalArgumentException(e);
			}
			beanList.add(bean);
		}
		return beanList;
	}
}