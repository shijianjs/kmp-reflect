import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.4.21"
    `java-gradle-plugin`
}

group = "io.github.shijianjs"

// The single version of the whole project lives in the root's gradle.properties. This is an included
// build, so it cannot read the root's `version` object — only the file, which is the same source.
val projectVersion: String = providers.fileContents(layout.settingsDirectory.file("../../gradle.properties"))
    .asText
    .map { text ->
        text.lineSequence()
            .first { it.trim().startsWith("kmp-reflect.version") }
            .substringAfter('=')
            .trim()
    }
    .get()
val releaseBuild: Boolean = providers.gradleProperty("kmp.reflect.release")
    .map(String::toBoolean)
    .getOrElse(false)
version = projectVersion + if (releaseBuild) "" else "-SNAPSHOT"

description = "Gradle plugin that wires kmp-reflect's KSP processor into every Kotlin Multiplatform target."

kotlin {
    compilerOptions {
        // The plugin runs inside Gradle's JVM. 11 keeps it loadable on the oldest JDK Gradle 9 accepts.
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// The processor's coordinates default to this plugin's own version, so a consumer only has to write
// `kmpReflect { registry.set(...) }`. Generated rather than hardcoded: the two versions cannot drift.
val generateVersion = tasks.register("generatePluginVersion") {
    val outputDir = layout.buildDirectory.dir("generated/version/kotlin")
    val version = project.version.toString()
    inputs.property("version", version)
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get()
            .file("io/github/shijianjs/kmp/reflect/gradle/KmpReflectVersion.kt")
            .asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |package io.github.shijianjs.kmp.reflect.gradle
            |
            |/** Written at build time from the project version; keep the processor and the plugin in step. */
            |internal const val KMP_REFLECT_VERSION: String = "$version"
            |
            """.trimMargin(),
        )
    }
}
kotlin.sourceSets.named("main") { kotlin.srcDir(generateVersion) }

dependencies {
    // Type-safe access to `ksp { arg(...) }`. Compile-only: the KSP plugin is on the consumer's
    // buildscript classpath by definition, and this module must not drag a compiler in.
    compileOnly("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.14")
}

gradlePlugin {
    plugins {
        create("kmpReflect") {
            id = "io.github.shijianjs.kmp-reflect"
            displayName = "kmp-reflect"
            description = "Generates KMP reflection metadata with KSP on every declared target."
            implementationClass = "io.github.shijianjs.kmp.reflect.gradle.KmpReflectPlugin"
        }
    }
}
