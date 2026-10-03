package net.benelog;

import java.util.List;

import javax.annotation.concurrent.Immutable;

/**
 * JSR-305 @Immutable. Error Prone does not recognize it, so this compiles without errors.
 */
@Immutable
public class JsrMemo {
	private String content;
	private final List<String> tags;

	public JsrMemo(String content, List<String> tags) {
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
