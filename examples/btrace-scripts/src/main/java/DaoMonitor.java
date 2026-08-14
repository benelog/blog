import com.sun.btrace.BTraceUtils.Aggregations;
import com.sun.btrace.BTraceUtils.Sys;
import com.sun.btrace.aggregation.Aggregation;
import com.sun.btrace.aggregation.AggregationFunction;
import com.sun.btrace.aggregation.AggregationKey;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Duration;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnEvent;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.ProbeClassName;
import com.sun.btrace.annotations.ProbeMethodName;
import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class DaoMonitor {
	private static Aggregation servletDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
	private static Aggregation servletCount = Aggregations.newAggregation(AggregationFunction.COUNT);	
	private static Aggregation daoDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
	private static Aggregation daoCount = Aggregations.newAggregation(AggregationFunction.COUNT);
	
	@OnMethod(clazz = "org.springframework.web.servlet.FrameworkServlet", 
	         method = "service", 
	         location = @Location(Kind.RETURN))
	 public static void servlet(@Duration long duration) {
	     Aggregations.addToAggregation(servletDuration, 
	                                   duration/1000);
	     Aggregations.addToAggregation(servletCount, 1);
	 }
	 
	
	 
	 @OnMethod(clazz = "/edu\\.tdd\\.crm\\..*Repository/", 
			 method = "/.*/", 
			 location = @Location(Kind.RETURN))
	 public static void dao(@Duration long duration, @ProbeClassName String className, @ProbeMethodName String probeMethod) {
		 String callName = concat(concat(className , "."), probeMethod);
		 AggregationKey aggregationKey = Aggregations.newAggregationKey(callName);
		 Aggregations.addToAggregation(daoDuration, aggregationKey, duration/1000);
		 Aggregations.addToAggregation(daoCount, aggregationKey, 1L);

	 }
	 
	 
    @OnEvent
    public static void summary() {
    	println("# org.springframework.web.servlet.FrameworkServlet.service");
    	Aggregations.printAggregation("- count:", servletCount);
    	Aggregations.printAggregation("- average duration(microseconds):", servletDuration);
    	println("# DAO");
    	Aggregations.printAggregation("- count", daoCount);
    	Aggregations.printAggregation("- average duration(microseconds)", daoDuration);
    	Sys.exit(0);
    }
}