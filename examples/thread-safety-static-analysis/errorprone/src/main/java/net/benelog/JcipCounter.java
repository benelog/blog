package net.benelog;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Original JCIP package. Error Prone does not recognize this @GuardedBy, so this compiles without errors.
 */
@ThreadSafe
public class JcipCounter {
	@GuardedBy("this")
	private int count;

	public void increment() {
		count++;
	}

	public synchronized int get() {
		return count;
	}
}
