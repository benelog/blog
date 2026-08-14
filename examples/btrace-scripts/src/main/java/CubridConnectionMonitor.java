import static com.sun.btrace.BTraceUtils.*;
import com.sun.btrace.BTraceUtils.Aggregations;
import com.sun.btrace.BTraceUtils.Sys;
import com.sun.btrace.aggregation.Aggregation;
import com.sun.btrace.aggregation.AggregationFunction;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Duration;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnEvent;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.TLS;
 
@BTrace
public class CubridConnectionMonitor {
	private static Aggregation servletDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
	private static Aggregation servletCount = Aggregations.newAggregation(AggregationFunction.COUNT);

	private static Aggregation prepareDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);	
	private static Aggregation prepareCount = Aggregations.newAggregation(AggregationFunction.COUNT);
	
	private static Aggregation executeDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
	private static Aggregation executeCount = Aggregations.newAggregation(AggregationFunction.COUNT);
	
	@TLS static long executeTimeInThread;
	@TLS static long prepareTimeInThread;
	
	@OnMethod(clazz = "org.springframework.web.servlet.FrameworkServlet", 
	         method = "service", 
	         location = @Location(Kind.RETURN))
	 public static void servlet(@Duration long duration) {
	     Aggregations.addToAggregation(servletDuration, duration/1000);
	     Aggregations.addToAggregation(servletCount, 1);
	}
	 

	 @OnMethod(clazz = "cubrid.jdbc.driver.CUBRIDConnection", 
	         method = "prepareStatement", 
	         location = @Location(Kind.RETURN))
	 public static void statementPrepare(@Duration long duration) {
	     Aggregations.addToAggregation(prepareDuration, duration/1000);
	     Aggregations.addToAggregation(prepareCount, 1);
	 }
	 
	 @OnMethod(clazz = "cubrid.jdbc.driver.CUBRIDPreparedStatement", 
			 method = "execute", 
			 location = @Location(Kind.RETURN))
	 public static void statementExecute(@Duration long duration) {
	     Aggregations.addToAggregation(executeDuration, duration/1000);
	     Aggregations.addToAggregation(executeCount, 1);
	 }

	 
    @OnEvent
    public static void summary() {
    	print("# org.springframework.web.servlet.FrameworkServlet.service");
    	Aggregations.printAggregation("- call count:", servletCount);
    	Aggregations.printAggregation("- average duration(microseconds):", servletDuration);
    	print("# cubrid.jdbc.driver.CUBRIDConnection.prepareStatement");    	
    	Aggregations.printAggregation("- call count : ", prepareCount);
    	Aggregations.printAggregation("- average duration(microseconds) :", prepareDuration);
    	print("# cubrid.jdbc.driver.CUBRIDPreparedStatement.execute");    	
    	Aggregations.printAggregation("- call count :", executeCount);
    	Aggregations.printAggregation("- average duration(microseconds):", executeDuration);
    	Sys.exit(0);
    }
}