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

@BTrace
public class PrintQuery {
	private static Aggregation prepareDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
	private static Aggregation prepareCount = Aggregations.newAggregation(AggregationFunction.COUNT);

	private static Aggregation executeDuration = Aggregations	.newAggregation(AggregationFunction.AVERAGE);
	private static Aggregation executeCount = Aggregations.newAggregation(AggregationFunction.COUNT);

	private static Aggregation executeInternalDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
	private static Aggregation executeInternalCount = Aggregations.newAggregation(AggregationFunction.COUNT);

	private static Aggregation autoCommitDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
	private static Aggregation autoCommitCount = Aggregations.newAggregation(AggregationFunction.COUNT);

	@OnMethod(clazz = "com.mysql.jdbc.ConnectionImpl", method = "prepareStatement", location = @Location(Kind.RETURN))
	public static void statementPrepare(@Duration long duration) {
		Aggregations.addToAggregation(prepareDuration, duration / 1000);
		Aggregations.addToAggregation(prepareCount, 1);
	}

	@OnMethod(clazz = "com.mysql.jdbc.ConnectionImpl", method = "setAutoCommit", location = @Location(Kind.RETURN))
	public static void autoCommit(@Duration long duration) {
		Aggregations.addToAggregation(autoCommitDuration, duration / 1000);
		Aggregations.addToAggregation(autoCommitCount, 1);
	}

	@OnMethod(clazz = "com.mysql.jdbc.PreparedStatement", method = "executeInternal", location = @Location(Kind.RETURN))
	public static void statementExecuteInternal(@Duration long duration) {
		Aggregations.addToAggregation(executeInternalDuration, duration / 1000);
		Aggregations.addToAggregation(executeInternalCount, 1);
	}

	@OnMethod(clazz = "com.mysql.jdbc.ConnectionImpl", method = "execSQL", location = @Location(Kind.RETURN))
	public static void statementExecuteSql(@Duration long duration) {
		Aggregations.addToAggregation(executeDuration, duration / 1000);
		Aggregations.addToAggregation(executeCount, 1);
	}

	
	@OnEvent
	public static void summary() {
		println("## com.mysql.jdbc.ConnectionImp.prepareStatement()");
		Aggregations.printAggregation("- call count : ", prepareCount);
		Aggregations.printAggregation("- average duration(microseconds) :",	prepareDuration);

		println("## com.mysql.jdbc.ConnectionImpl.setAutoCommit()");
		Aggregations.printAggregation("- call count : ", autoCommitCount);
		Aggregations.printAggregation("- average duration(microseconds) :",	autoCommitDuration);
		
		println("## com.mysql.jdbc.PreparedStatement.executeInternal()");
		Aggregations.printAggregation("- call count :", executeInternalCount);
		Aggregations.printAggregation("- average duration(microseconds):",	executeInternalDuration);
		

		println("## com.mysql.jdbc.ConnectionImpl.execSQL()");
		Aggregations.printAggregation("- call count :", executeCount);
		Aggregations.printAggregation("- average duration(microseconds):",	executeDuration);
		Sys.exit(0);
	}
}