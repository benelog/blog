package ptest;

import java.util.List;
import java.util.Map;

public interface BeanConverter {
	public <T extends Map<String, Object>, C> List<C> convertMapToBean(
			List<T> mapList, Class<C> targetClass);
}
