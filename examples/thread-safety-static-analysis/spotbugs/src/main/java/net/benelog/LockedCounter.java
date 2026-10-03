package net.benelog;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Same violation as Counter, but more synchronized methods raise
 * the proportion of locked accesses. SpotBugs reports IS_FIELD_NOT_GUARDED.
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
