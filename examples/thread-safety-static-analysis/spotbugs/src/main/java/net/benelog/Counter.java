package net.benelog;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Modifies a {@code @GuardedBy("this")} field without a lock, but the locked ratio is low, so
 * SpotBugs does not report it by default; it appears with low priority under -PreportLow.
 */
@ThreadSafe
public class Counter {
	@GuardedBy("this")
	private int count;

	public void increment() {
		count++;
	}

	public synchronized int get() {
		return count;
	}
}
