package net.benelog;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * 원본 JCIP 패키지. Error Prone은 이 @GuardedBy를 인식하지 않아서 오류 없이 컴파일된다.
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
