import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
}

@Suppress("UNCHECKED_CAST")
val versionSuffix = rootProject.extra["versionSuffix"] as String
version = rootProject.extra["kmpReflectVersion"].toString() + versionSuffix
description = "KSP processor that generates kmp-reflect metadata, proxy classes and inlined resources."

kotlin {
    compilerOptions {
        // KSP loads the processor inside the Kotlin compiler's JVM; 11 is the lowest target that
        // both JDK 21 (CI) and JDK 25 (local) can load without preview features.
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

java {
    // Gradle validates that `compileJava` and `compileKotlin` agree; this module has no Java
    // sources, but the tasks still have to line up.
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.14")
    implementation("com.squareup:kotlinpoet:2.4.0")
    implementation("com.squareup:kotlinpoet-ksp:2.2.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
