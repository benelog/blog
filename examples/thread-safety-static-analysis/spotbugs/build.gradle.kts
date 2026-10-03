plugins {
	java
	id("com.github.spotbugs") version "6.5.11"
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("com.github.stephenc.jcip:jcip-annotations:1.0-1")
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(25))
	}
}

spotbugs {
	toolVersion.set("4.10.4")
	ignoreFailures.set(true)
	if (project.hasProperty("reportLow")) {
		// Report low-priority warnings too. The default reports medium and above only.
		reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
	}
}

tasks.spotbugsMain {
	val report = layout.buildDirectory.file("reports/spotbugs/main.txt")
	reports.create("text") {
		required.set(true)
		outputLocation.set(report)
	}
	doLast {
		println(report.get().asFile.readText())
	}
}
