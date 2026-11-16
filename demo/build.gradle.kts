import org.jetbrains.kotlin.gradle.ExperimentalJsTestDsl

plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

// The demo is the integration test for the processor: the same annotated classes in `commonMain` are
// reflected for the JVM target (asserted in `jvmTest`) and for the JS target (shown in the page).
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

    sourceSets {
        commonMain.dependencies {
            api(project(":kmp-reflect-core"))
        }
        jsMain.dependencies {
            // DOM access goes through kotlin-browser's typed `web.*` API; detekt forbids mixing it
            // with `kotlinx.browser.*` or the JDK's `org.w3c.dom.*`.
            implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.11.1")
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

dependencies {
    // KSP runs per target, so both configurations are needed: each target gets its own generated
    // sources, and the tests run on the JVM while the page runs in the browser.
    add("kspJvm", project(":kmp-reflect-ksp"))
    add("kspJs", project(":kmp-reflect-ksp"))
}

ksp {
    // The module object the demo registers at start-up.
    arg("kmp.reflect.registry", "io.github.shijianjs.kmp.reflect.demo:DemoReflection")
    arg("kmp.reflect.module", "demo")
    // Resource directories are read at compile time, not packaged: the content is inlined.
    arg("kmp.reflect.resources", layout.projectDirectory.dir("data").asFile.absolutePath)
}

// ---------------------------------------------------------------------------------------------
// End-to-end suite
//
// Playwright's JUnit 5 integration plus Hutool's SimpleServer serving the bundled page. It reuses the
// Chromium that `kotlinInstallPlaywrightBrowsers` installs for the Kotlin/JS browser tests, hence the
// pinned Playwright version above. `jvmTest` is disabled to keep `build`/`check` browser-free; the
// unit assertions live in the dedicated `reflectionTest` task instead.
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

// Runs only the reflection assertions (no browser), which is what CI checks besides the page build.
val reflectionTestSourceSet = kotlin.targets.getByName("jvm").compilations.getByName("test")
tasks.register<Test>("reflectionTest") {
    group = "verification"
    description = "Asserts the generated metadata on the JVM target."
    useJUnitPlatform()
    testClassesDirs = reflectionTestSourceSet.output.classesDirs
    classpath = files(reflectionTestSourceSet.output.allOutputs, reflectionTestSourceSet.runtimeDependencyFiles)
    filter { includeTestsMatching("io.github.shijianjs.kmp.reflect.demo.*") }
    systemProperty("junit.jupiter.execution.timeout.default", "30 s")
    outputs.upToDateWhen { false }
}

tasks.named<Test>("jvmTest") { enabled = false }
