package net.benelog.report;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class ReportService {
	public String find(long id) {
		return "report-" + id;
	}
}
