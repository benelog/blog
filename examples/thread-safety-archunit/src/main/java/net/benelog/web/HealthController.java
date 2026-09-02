package net.benelog.web;

import java.time.Clock;
import java.time.format.DateTimeFormatter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
	private final Clock clock = Clock.systemUTC();
	private final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

	@GetMapping("/health")
	public String health() {
		return formatter.format(clock.instant());
	}
}
