import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
	java
	id("org.springframework.boot") version "3.3.2"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.maf"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.data:spring-data-jdbc")
	implementation("com.h2database:h2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")// See https://docs.gradle.org/8.3/userguide/upgrading_version_8.html#test_framework_implementation_dependencies
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs = listOf(
		"-XX:+EnableDynamicAgentLoading",
	)
}

// This section causes useful test output to go to the terminal.
tasks.test {
	testLogging {
		events("passed", "skipped", "failed") //, "standardOut", "standardError"

		showExceptions = true
		exceptionFormat = TestExceptionFormat.FULL
		showCauses = true
		showStackTraces = true

		// Change to true for more verbose test output
		showStandardStreams = false
	}
}
