package net.benelog.report;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class ReportFormatter {
	private final StringBuilder buffer = new StringBuilder();

	public String format(String title, String body) {
		buffer.setLength(0);
		return buffer.append(title).append('\n').append(body).toString();
	}
}
