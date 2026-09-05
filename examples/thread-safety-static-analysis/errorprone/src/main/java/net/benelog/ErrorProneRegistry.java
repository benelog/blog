package net.benelog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.errorprone.annotations.ThreadSafe;

/**
 * Error Prone 자체 패키지의 @ThreadSafe. 기본 설정의 Error Prone 2.50.0은 검사하지 않는다.
 * -PthreadSafeCheck로 ThreadSafeChecker를 등록하면 lock 없이 바뀌는 필드와
 * 스레드 안전하지 않은 타입의 final 필드를 컴파일 오류로 보고한다.
 */
@ThreadSafe
public class ErrorProneRegistry {
	private int count;
	private final Map<String, String> entries = new HashMap<>();
	private final ConcurrentHashMap<String, String> safeEntries = new ConcurrentHashMap<>();

	public void register(String key, String value) {
		count++;
		entries.put(key, value);
		safeEntries.put(key, value);
	}

	public int getCount() {
		return count;
	}
}
