import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    java
    id("com.google.cloud.tools.jib") version "3.4.3"
}

group = "com.valr"
version = "1.0.0-SNAPSHOT"

val dockerBaseImage = "amazoncorretto:17.0.7-alpine"

repositories {
    mavenCentral()
}

sourceSets {
    val integrationTest by creating {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        java.srcDir(file("src/integrationTest/kotlin"))
        resources.srcDir(file("src/integrationTest/resources"))
    }
}

configurations {
    val integrationTestImplementation by getting {
        extendsFrom(configurations.testImplementation.get())
    }
    val integrationTestRuntimeOnly by getting {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
//	implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("io.kotest:kotest-runner-junit5-jvm:4.6.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.12.0")

    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
    target {
        jvmToolchain(17)
    }
}


jib {
    from {
        image = dockerBaseImage
    }
    to {
        image = "${project.group}/${rootProject.name}:${project.version}"
    }

    container {
        mainClass = "com.valr.assignment.AssignmentApplicationKt"
        jvmFlags = listOf("-Xms512m", "-Xmx1024m")
        ports = listOf("8081")
    }
}


tasks.withType<Test> {
    useJUnitPlatform()
}


tasks.withType<ProcessResources> {
    doLast {
        val propertiesFile = file("${buildDir}/resources/main/version.properties")
        propertiesFile.parentFile.mkdirs()
        val properties = Properties()
        properties.setProperty("version", rootProject.version.toString())
        properties.setProperty("name", rootProject.name)
        propertiesFile.writer().use { properties.store(it, null) }
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    mustRunAfter(tasks.test)
}

// Ensure Docker image is built before integration tests run
tasks.named("integrationTest") {
    dependsOn(tasks.named("jibDockerBuild"))
}

tasks.named("check") {
    dependsOn(tasks.named("integrationTest"))
}