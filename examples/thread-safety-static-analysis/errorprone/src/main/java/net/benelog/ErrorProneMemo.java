package net.benelog;

import java.util.List;

import com.google.errorprone.annotations.Immutable;

/**
 * Error Prone 자체 패키지의 @Immutable. final이 아닌 필드와
 * final이지만 가변 타입인 필드를 모두 컴파일 오류로 보고한다.
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
