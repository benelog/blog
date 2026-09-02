package net.benelog;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * {@code @GuardedBy("this")} 필드를 lock 없이 수정하지만
 * SpotBugs는 이 위반을 보고하지 않는다.
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
