plugins {
	java
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework:spring-web:7.0.9")
	implementation("com.github.stephenc.jcip:jcip-annotations:1.0-1")
	testImplementation("com.tngtech.archunit:archunit-junit5:1.5.0")
	testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(25))
	}
}

tasks.test {
	useJUnitPlatform()
	testLogging {
		events("passed", "failed")
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
		showStackTraces = false
	}
}
