package ptest;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

public class ApacheCommonsPropertyUtilsBeanConverter implements BeanConverter {

	public <T extends Map<String, Object>, C> List<C> convertMapToBean(
			List<T> list, Class<C> clazz) {
		List<C> beanList = new ArrayList<C>();

		for (T source : list) {
			C bean = null;
			try {
				bean = clazz.newInstance();

				PropertyDescriptor[] targetPds = PropertyUtils
						.getPropertyDescriptors(clazz);

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
			beanList.add(bean);
		}
		return beanList;
	}
}