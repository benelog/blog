import static com.sun.btrace.BTraceUtils.*;

import java.lang.reflect.Field;
import java.util.Map;

import com.sun.btrace.BTraceUtils.Sys;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.OnEvent;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.Self;

@BTrace
public class DbcpActiveConnectionMonitor {
	
	private static Map<Object, Object> activeNumsByUrl = newHashMap();
	
	private static final String DS_CLASS = "org.apache.commons.dbcp.BasicDataSource";
	
	@OnMethod(clazz = DS_CLASS, method = "getConnection")
	public static void onGetConnection(@Self Object basicDataSource) {
		Class<?> dsClass = classOf(basicDataSource);
		Object url = getUrl(dsClass, basicDataSource);
		Object numberOfActive = getNumberOfActive(basicDataSource, dsClass);
		put(activeNumsByUrl,url,numberOfActive);
	}

    @OnEvent
    public static void print() {
    	println("-------------- active connection numbers by DB connection --------------------");
        if (size(activeNumsByUrl) != 0) {
            printMap(activeNumsByUrl);
        } else {
        	println("no 'getConnection' calls");
        }
		Sys.exit(0);
    }
    
	private static Object getUrl(Class<?> dsClass, Object basicDataSource) {
		Field urlField = field(dsClass, "url");
		return get(urlField, basicDataSource);
	}

	private static Object getNumberOfActive(Object basicDataSource, Class<?> dsClass) {
		Field poolField = field(dsClass, "connectionPool");
		Object pool = get(poolField, basicDataSource);
		Field numActiveField = field(classOf(pool), "_numActive");
		Object numActive = get(numActiveField, pool);
		return numActive;
	}

}
