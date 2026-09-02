package net.benelog;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * JSR-305 패키지. Error Prone이 @GuardedBy 위반을 컴파일 오류로 보고한다.
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
