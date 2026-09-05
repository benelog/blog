package net.benelog;

import java.util.List;

import javax.annotation.concurrent.Immutable;

/**
 * JSR-305 패키지의 @Immutable. Error Prone이 인식하지 않아 오류 없이 컴파일된다.
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
