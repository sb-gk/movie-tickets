plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.0.0"
}

group = "au.com.sportsbet"
version = "1.0.0"
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

repositories {
    mavenCentral()
}

// Define integration test source set
sourceSets {
    create("integrationTest") {
        java {
            srcDir("src/integration-test/java")
        }
        resources {
            srcDir("src/integration-test/resources")
        }
        compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Integration test dependencies (same as test)
    "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+UseSerialGC")
    description = "Runs unit tests"
}

// Create integration test task
val integrationTest =
    tasks.register<Test>("integrationTest") {
        description = "Runs integration tests"
        group = "verification"

        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath

        useJUnitPlatform()
        jvmArgs("-XX:+UseSerialGC")

        shouldRunAfter(tasks.test)
    }

// Make check task depend on integration tests
tasks.check {
    dependsOn(integrationTest)
}

spotless {
    java {
        googleJavaFormat("1.28.0")
        removeUnusedImports()
        target("src/**/*.java")
    }

    kotlinGradle {
        ktlint("1.3.1")
    }
}
