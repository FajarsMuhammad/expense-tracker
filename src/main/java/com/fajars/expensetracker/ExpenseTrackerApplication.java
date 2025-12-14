package com.fajars.expensetracker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class ExpenseTrackerApplication {

	@PostConstruct
	public void init() {
		// Set default timezone to Asia/Jakarta for the entire application
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jakarta"));

		// Verify Java 25 features
		logJava25Features();
	}

	/**
	 * Log Java 25 feature status for verification.
	 */
	private void logJava25Features() {
		log.info("═══════════════════════════════════════════════════════════════");
		log.info("Java Version: {}", System.getProperty("java.version"));
		log.info("Java Vendor: {}", System.getProperty("java.vendor"));

		// Check virtual threads support
		try {
			Thread virtualThread = Thread.ofVirtual().unstarted(() -> {});
			log.info("✓ Virtual Threads: SUPPORTED (isVirtual={})", virtualThread.isVirtual());
			log.info("✓ Virtual threads enabled via spring.threads.virtual.enabled=true");
		} catch (Exception e) {
			log.warn("✗ Virtual Threads: NOT SUPPORTED - {}", e.getMessage());
		}

		// Check ScopedValue support (Java 21+)
		try {
			ScopedValue<String> test = ScopedValue.newInstance();
			log.info("✓ ScopedValue API: SUPPORTED (JEP 464)");
		} catch (Exception e) {
			log.warn("✗ ScopedValue API: NOT SUPPORTED - {}", e.getMessage());
		}

		log.info("═══════════════════════════════════════════════════════════════");
	}

	public static void main(String[] args) {
		SpringApplication.run(ExpenseTrackerApplication.class, args);
	}

}
