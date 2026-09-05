plugins {
	java
	id("net.ltgt.errorprone") version "5.1.1"
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("com.github.stephenc.jcip:jcip-annotations:1.0-1")
	implementation("com.google.code.findbugs:jsr305:3.0.2")
	implementation("com.google.errorprone:error_prone_annotations:2.50.0")
	errorprone("com.google.errorprone:error_prone_core:2.50.0")
	if (project.hasProperty("threadSafeCheck")) {
		// 기본 검사 목록에 없는 ThreadSafeChecker를 플러그인으로 등록한다.
		errorprone(project(":threadsafe-check"))
	}
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(25))
	}
}
