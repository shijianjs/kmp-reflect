import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    kotlin("multiplatform")
}

@Suppress("UNCHECKED_CAST")
val versionSuffix = rootProject.extra["versionSuffix"] as String
version = rootProject.extra["kmpReflectVersion"].toString() + versionSuffix
description = "Compile-time reflection for Kotlin Multiplatform: annotations, metadata model, dynamic proxy and inlined classpath resources."

kotlin {
    // Targets are declared by host. A Kotlin/Native compilation is always built for the host's own
    // target in this repository, so `./gradlew build` never asks for an executable it cannot link or
    // run; CI gets linuxX64, a Windows checkout gets mingwX64, a Mac checkout gets the Apple ones.
    // Consumers are unaffected: the published klibs cover whatever the host that published them
    // could compile, and every other platform resolves a source-only klib from the metadata.
    jvm()

    js {
        useEsModules()

        // Tests run on Node: the metadata model has no DOM dependency.
        nodejs()

        binaries.library()

        // Kotlin/JS dead-code-eliminates a library's JS output down to the `@JsExport` roots, and
        // public API entry points are reached by name from generated code, not by static calls.
        compilerOptions.freeCompilerArgs.add("-Xir-dce=false")
    }

    // Kotlin/Wasm: KSP 2 emits into the wasm compilation too, and the runtime is plain Kotlin. Node is
    // enough for the tests — nothing here needs a browser. No `binaries.library()`: the artifact a
    // consumer compiles against is the klib, and asking for a linked wasm binary would drag binaryen
    // (a separate download) into every `build` for no benefit.
    wasmJs {
        nodejs()
    }

    when {
        HostManager.hostIsMingw -> mingwX64()
        HostManager.hostIsLinux -> linuxX64()
        HostManager.hostIsMac -> {
            macosX64()
            macosArm64()
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
