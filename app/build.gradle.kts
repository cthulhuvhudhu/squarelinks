/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.6/userguide/building_java_projects.html
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
//    id("com.google.devtools.ksp") version "1.9.22-1.0.17"

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}
//
//sourceSets.main {
//    java.srcDirs("build/generated/ksp/main/kotlin")
//}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")

    // This dependency is used by the application.
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("com.google.guava:guava:32.0.0-android")
    implementation("org.danilopianini:khttp:1.4.3")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("io.insert-koin:koin-core:3.5.0")
//    implementation("io.insert-koin:koin-annotations:1.3.1") // Annotations
//    ksp("io.insert-koin:koin-ksp-compiler:1.3.1") // Koin KSP

    implementation("org.jetbrains.kotlinx:atomicfu:0.18.5")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
}

application {
    // Define the main class for the application.
    mainClass.set("squarelinks.AppKt")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
