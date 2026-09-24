package net.benelog;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * {@code @GuardedBy("this")} 필드를 lock 없이 수정하지만 lock 비율이 낮아서
 * SpotBugs 기본 설정에서는 보고되지 않고, -PreportLow에서 낮은 우선순위로 나타난다.
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
