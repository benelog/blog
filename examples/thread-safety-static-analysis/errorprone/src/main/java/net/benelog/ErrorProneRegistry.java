package net.benelog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.errorprone.annotations.ThreadSafe;

/**
 * Error Prone's own @ThreadSafe. Error Prone 2.50.0 does not check it by default.
 * Registering ThreadSafeChecker with -PthreadSafeCheck reports fields modified without a lock
 * and final fields of non-thread-safe types as compile errors.
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
