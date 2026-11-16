plugins {
    kotlin("multiplatform")
}

@Suppress("UNCHECKED_CAST")
val versionSuffix = rootProject.extra["versionSuffix"] as String
version = rootProject.extra["kmpReflectVersion"].toString() + versionSuffix
description = "Compile-time reflection for Kotlin Multiplatform: annotations, metadata model, dynamic proxy and inlined classpath resources."

kotlin {
    // Only JVM and JS: every bit of metadata in this project is produced by KSP, and KSP does not
    // generate code for Kotlin/Native or Wasm targets. Shipping the runtime for those would only
    // promise an API that can never have any data behind it. See the targets note in AGENTS.md.
    jvm()

    js {
        useEsModules()

        // Tests run on Node: the metadata model has no DOM dependency.
        nodejs()

        binaries.library()
    }

    compilerOptions {
        // Kotlin/JS dead-code-eliminates a library's JS output down to the `@JsExport` roots, and
        // public API entry points are reached by name from generated code, not by static calls.
        freeCompilerArgs.add("-Xir-dce=false")
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
