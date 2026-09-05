package com.google.errorprone.bugpatterns.threadsafety;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import javax.inject.Inject;

import com.google.errorprone.BugPattern;

/**
 * Error Prone에 들어 있지만 기본 검사 목록에는 등록되지 않은 ThreadSafeChecker를
 * 플러그인 검사로 등록하기 위한 래퍼.
 */
@BugPattern(
		name = "ThreadSafe",
		summary = "Type declaration annotated with @ThreadSafe is not thread safe",
		severity = ERROR)
public class ThreadSafeCheck extends ThreadSafeChecker {

	/** ServiceLoader가 요구하는 public 기본 생성자. Error Prone은 @Inject 생성자를 쓴다. */
	public ThreadSafeCheck() {
		super(null, null);
	}

	@Inject
	ThreadSafeCheck(WellKnownThreadSafety wellKnownThreadSafety,
			ThreadSafeAnalysis.Factory threadSafeAnalysisFactory) {
		super(wellKnownThreadSafety, threadSafeAnalysisFactory);
	}
}
