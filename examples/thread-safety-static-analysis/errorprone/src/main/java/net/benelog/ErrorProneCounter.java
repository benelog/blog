package net.benelog;

import com.google.errorprone.annotations.ThreadSafe;
import com.google.errorprone.annotations.concurrent.GuardedBy;

/**
 * Error Prone 자체 패키지. @GuardedBy 위반을 컴파일 오류로 보고한다.
 */
@ThreadSafe
public class ErrorProneCounter {
	@GuardedBy("this")
	private int count;

	public void increment() {
		count++;
	}

	public synchronized int get() {
		return count;
	}
}
