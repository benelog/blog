package net.benelog;

import java.util.List;

import com.google.errorprone.annotations.Immutable;

/**
 * Error Prone's own @Immutable. Both the non-final field and
 * the final field of a mutable type are reported as compile errors.
 */
@Immutable
public class ErrorProneMemo {
	private String content;
	private final List<String> tags;

	public ErrorProneMemo(String content, List<String> tags) {
		this.content = content;
		this.tags = tags;
	}

	public String getContent() {
		return content;
	}

	public List<String> getTags() {
		return tags;
	}
}
