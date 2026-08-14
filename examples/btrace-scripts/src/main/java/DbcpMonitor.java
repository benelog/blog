import static com.sun.btrace.BTraceUtils.*;

import java.lang.reflect.Field;

import com.sun.btrace.BTraceUtils.Sys;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.Self;

@BTrace
public class DbcpMonitor {

	private static final String DS_CLASS = "org.apache.commons.dbcp.BasicDataSource";
	
	@OnMethod(clazz = DS_CLASS, method = "getConnection")
	public static void onGetConnection(@Self Object basicDataSource) {

		Class<?> dsClass = classOf(basicDataSource);
		printTitle(dsClass, basicDataSource);
		printFields(basicDataSource);
		printNumberOfActive(basicDataSource, dsClass);
		println("=======================================");
		Sys.exit(0);
	}

	private static void printTitle(Class<?> dsClass, Object basicDataSource) {
		Field urlField = field(dsClass, "url");
		Object url = get(urlField, basicDataSource);
		print("=====DBCP BasicDataSource info (");
		print(url);
		println(" ) ========");
	}

	private static void printNumberOfActive(Object basicDataSource, Class<?> dsClass) {
		Field poolField = field(dsClass, "connectionPool");
		Object numActive = getNumActive(basicDataSource, poolField);
		println(concat("===== number of Active : ", str(numActive)));
	}

	private static Object getNumActive(Object basicDataSource, Field poolField) {
		Object pool = get(poolField, basicDataSource);
		Field numActiveField = field(classOf(pool), "_numActive");
		Object numActive = get(numActiveField, pool);
		return numActive;
	}

}
