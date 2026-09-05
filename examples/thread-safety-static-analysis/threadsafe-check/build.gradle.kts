plugins {
	java
}

repositories {
	mavenCentral()
}

dependencies {
	compileOnly("com.google.errorprone:error_prone_core:2.50.0")
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(25))
	}
}
