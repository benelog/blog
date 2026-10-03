package com.google.errorprone.bugpatterns.threadsafety;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import javax.inject.Inject;

import com.google.errorprone.BugPattern;

/**
 * Wrapper that registers ThreadSafeChecker, which ships with Error Prone but is not
 * in its default check list, as a plugin check.
 */
@BugPattern(
		name = "ThreadSafe",
		summary = "Type declaration annotated with @ThreadSafe is not thread safe",
		severity = ERROR)
public class ThreadSafeCheck extends ThreadSafeChecker {

	/** Public no-argument constructor required by ServiceLoader. Error Prone uses the @Inject constructor. */
	public ThreadSafeCheck() {
		super(null, null);
	}

	@Inject
	ThreadSafeCheck(WellKnownThreadSafety wellKnownThreadSafety,
			ThreadSafeAnalysis.Factory threadSafeAnalysisFactory) {
		super(wellKnownThreadSafety, threadSafeAnalysisFactory);
	}
}
