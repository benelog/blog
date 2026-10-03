package net.benelog;

import net.jcip.annotations.Immutable;

/**
 * Declared {@code @Immutable} but has a non-final field, so
 * SpotBugs reports JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS.
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
