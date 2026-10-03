package net.benelog;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * JSR-305 package. Error Prone reports the @GuardedBy violation as a compile error.
 */
@ThreadSafe
public class JsrCounter {
	@GuardedBy("this")
	private int count;

	public void increment() {
		count++;
	}

	public synchronized int get() {
		return count;
	}
}
