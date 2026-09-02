package net.benelog;

import net.jcip.annotations.Immutable;

/**
 * {@code @Immutable}로 선언했지만 final이 아닌 필드가 있어서
 * SpotBugs가 JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS로 보고한다.
 */
@Immutable
public class Memo {
	private String content;

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}
}
