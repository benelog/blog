package net.benelog;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Counter와 같은 위반이지만 synchronized 메서드가 더 많아서
 * lock을 잡은 접근 비율이 높다. SpotBugs가 IS_FIELD_NOT_GUARDED로 보고한다.
 */
@ThreadSafe
public class LockedCounter {
	@GuardedBy("this")
	private int count;

	public void increment() {
		count++;
	}

	public synchronized int get() {
		return count;
	}

	public synchronized void reset() {
		count = 0;
	}

	public synchronized boolean isZero() {
		return count == 0;
	}
}
