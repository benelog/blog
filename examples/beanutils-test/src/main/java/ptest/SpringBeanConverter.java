package ptest;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;

public class SpringBeanConverter implements BeanConverter {

	public <T extends Map<String, Object>, C> List<C> convertMapToBean(
			List<T> list, Class<C> clazz) {
		List<C> beanList = new ArrayList<C>();
		for (Map<String, Object> source : list) {
			C bean = toBean(source, clazz);
			beanList.add(bean);

		}
		return beanList;
	}

	private <C> C toBean(Map<String, Object> source, Class<C> targetClass) {

		C bean = null;
		try {
			bean = targetClass.newInstance();
			PropertyDescriptor[] targetPds = BeanUtils
					.getPropertyDescriptors(targetClass);

			for (PropertyDescriptor desc : targetPds) {
				Object value = source.get(desc.getName());
				if (value != null) {
					Method writeMethod = desc.getWriteMethod();
					if (writeMethod != null) {
						writeMethod.invoke(bean, new Object[] { value });
					}
				}
			}
		} catch (InstantiationException e) {
			new IllegalArgumentException("Cannot initiate class",e);
		} catch (IllegalAccessException e) {
			new IllegalStateException("Cannot access the property",e);
		} catch (InvocationTargetException e) {
			new IllegalArgumentException(e);
		}
		return bean;
	}
}
