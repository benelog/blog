package net.benelog.web;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.benelog.report.ReportFormatter;
import net.benelog.report.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {
	private final ReportService reportService;
	private final ReportFormatter formatter = new ReportFormatter();
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	@GetMapping("/reports/{id}")
	public String report(@PathVariable long id) {
		return formatter.format(dateFormat.format(new Date()), reportService.find(id));
	}
}
