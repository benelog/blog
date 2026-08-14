import static com.sun.btrace.BTraceUtils.*;

import java.lang.reflect.Field;

import com.sun.btrace.BTraceUtils.Sys;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.Self;

@BTrace
public class DbcpMonitorSimple {
	
	private static final String DS_CLASS = "org.apache.commons.dbcp.BasicDataSource";


	@OnMethod(clazz = DS_CLASS, method = "getConnection")
	public static void onGetConnection(@Self Object basicDataSource) {

		Field urlField = field(DS_CLASS, "url");
		Object url = get(urlField, basicDataSource);
		print("=====DBCP BasicDataSource info (");
		print(url);
		println(" ) ==========");
		printFields(basicDataSource);
		Field poolField = field(DS_CLASS, "connectionPool");

		Object pool = get(poolField, basicDataSource);
		println("=====connectionPool (GenericObjectPool) info====");
		printFields(pool);
		println("==========");
		Sys.exit(0);
	}
}
