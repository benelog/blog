package net.benelog;

import com.google.errorprone.annotations.ThreadSafe;
import com.google.errorprone.annotations.concurrent.GuardedBy;

/**
 * Error Prone's own package. Error Prone reports the @GuardedBy violation as a compile error.
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
