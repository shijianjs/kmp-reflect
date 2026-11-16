import org.jetbrains.kotlin.gradle.ExperimentalJsTestDsl
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("io.github.shijianjs.kmp-reflect")
}

// The demo is the integration test for the processor: the same annotated classes in `commonMain` are
// reflected for every target. The browser page exercises the JS target; the assertions in
// `commonTest` run on each target the host can execute (a Windows checkout gets mingwX64, Linux gets
// linuxX64, a Mac checkout the Apple binary).
val nativeTarget: String? = when {
    HostManager.hostIsMingw -> "mingwX64"
    HostManager.hostIsLinux -> "linuxX64"
    HostManager.hostIsMac -> "macosArm64"
    else -> null
}

kotlin {
    js {
        useEsModules()

        // The demo is a standalone page, so the JS target needs an executable webpack bundle.
        binaries.executable()
        browser {
            @OptIn(ExperimentalJsTestDsl::class)
            test { chromium() }
        }
    }

    jvm()

    // Kotlin/Wasm has no page of its own here: it is covered by the same assertions in `commonTest`,
    // which is what proves the generated metadata compiles and resolves on that backend too.
    wasmJs {
        nodejs()
    }

    when (nativeTarget) {
        "mingwX64" -> mingwX64()
        "linuxX64" -> linuxX64()
        "macosArm64" -> macosArm64()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":kmp-reflect-core"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jsMain.dependencies {
            // DOM access goes through kotlin-browser's typed `web.*` API; detekt forbids mixing it
            // with `kotlinx.browser.*` or the JDK's `org.w3c.dom.*`.
            implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.11.1")
            // The page reads its CSS through dom-kit's typed DSL: `createElement` narrowed by tag,
            // `inlineStyle` over csstype, and one `cssSheet` for the rules inline style cannot express.
            implementation("io.github.shijianjs:ktjs-dom-kit:0.1.0")
        }
        jvmTest.dependencies {
            implementation("com.microsoft.playwright:playwright:1.61.0")
            implementation("cn.hutool:hutool-all:5.8.48")
            implementation("org.junit.jupiter:junit-jupiter:6.1.3")
            implementation(kotlin("test-junit5"))
            runtimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
        }
    }
}

// `commonTest` depends on `kotlin-test`, which Gradle resolves per target; the JVM test compilation
// additionally depends on `kotlin-test-junit5` for the Playwright suite. Both artifacts provide the
// `kotlin-test-framework-impl` capability, so the JVM test classpath has to be told which one wins,
// or the build fails with a capability conflict.
listOf("jvmTestCompileClasspath", "jvmTestRuntimeClasspath").forEach { configurationName ->
    configurations.named(configurationName) {
        resolutionStrategy.capabilitiesResolution.withCapability("org.jetbrains.kotlin:kotlin-test-framework-impl") {
            candidates.firstOrNull { it.id.displayName.startsWith("org.jetbrains.kotlin:kotlin-test-junit5") }
                ?.let { select(it) }
        }
    }
}

// The plugin adds the processor to every target's KSP configuration (JVM, JS, Wasm, and the host's
// native target alike) and forwards these options, so nothing target-specific is written here.
kmpReflect {
    // The module object the demo registers at start-up.
    registry.set("io.github.shijianjs.kmp.reflect.demo:DemoReflection")
    module.set("demo")
    // Resource directories are read at compile time, not packaged: the content is inlined. The plugin
    // also declares them as KSP task inputs, so editing a resource reruns processing.
    resources.from(layout.projectDirectory.dir("data"))
    // This build compiles the processor itself; a consumer leaves it unset and resolves the released
    // artifact at the plugin's own version.
    processorProject.set(":kmp-reflect-ksp")
}

// ---------------------------------------------------------------------------------------------
// End-to-end suite
//
// Playwright's JUnit 5 integration plus Hutool's SimpleServer serving the bundled page. It reuses the
// Chromium that `kotlinInstallPlaywrightBrowsers` installs for the Kotlin/JS browser tests, hence the
// pinned Playwright version above. `jvmTest` is disabled to keep `build`/`check` browser-free; the
// unit assertions live in `commonTest` and run per target through the usual test tasks.
// ---------------------------------------------------------------------------------------------
val jvmE2eCompilation = kotlin.targets.getByName("jvm").compilations.getByName("test")

tasks.register<Test>("e2eTest") {
    group = "verification"
    description = "Runs the Kotlin/JVM Playwright suite against the bundled demo page."
    useJUnitPlatform()
    dependsOn("jsBrowserDistribution")
    dependsOn(":demo:kotlinInstallPlaywrightBrowsers")

    testClassesDirs = jvmE2eCompilation.output.classesDirs
    classpath = files(jvmE2eCompilation.output.allOutputs, jvmE2eCompilation.runtimeDependencyFiles)
    systemProperty("demo.dist", layout.buildDirectory.dir("dist/js/productionExecutable").get().asFile.absolutePath)
    systemProperty("e2e.output", layout.buildDirectory.dir("e2e-results").get().asFile.absolutePath)
    systemProperty("junit.jupiter.execution.timeout.default", "30 s")

    outputs.upToDateWhen { false }
}

// Runs only the metadata assertions on the JVM target (no browser): the same class runs on every
// target through `allTests`, this task is the quick JVM-only entry point.
tasks.register<Test>("reflectionTest") {
    group = "verification"
    description = "Asserts the generated metadata on the JVM target."
    useJUnitPlatform()
    testClassesDirs = jvmE2eCompilation.output.classesDirs
    classpath = files(jvmE2eCompilation.output.allOutputs, jvmE2eCompilation.runtimeDependencyFiles)
    filter { includeTestsMatching("io.github.shijianjs.kmp.reflect.demo.*") }
    systemProperty("junit.jupiter.execution.timeout.default", "30 s")
    outputs.upToDateWhen { false }
}

// The Playwright classes belong to the e2e task; leaving them in `jvmTest` would make `build` need
// a browser.
tasks.named<Test>("jvmTest") { enabled = false }
