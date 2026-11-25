pluginManagement {
    repositories {
        // The docs site is built with the ktjs-docusaurus Gradle plugin. Released versions come from
        // Central; `mavenLocal` is what serves a SNAPSHOT one, which is how the site is developed
        // against an unreleased wrapper. An empty local repository falls through to the next
        // repository, so the order costs nothing in CI.
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }

    // The Gradle plugin lives in its own build so that `:demo` can apply it by id, the way a consumer
    // would. An included build also means the plugin is compiled before any project that uses it.
    includeBuild("gradle/kmp-reflect-gradle-plugin")

    // The plugin's version is a catalog entry (`libs.plugins.ktjs.docusaurus` in
    // `gradle/libs.versions.toml`); `docs` applies it with `alias(...)`, so no resolution strategy
    // is needed here.
}

dependencyResolutionManagement {
    repositories {
        // Same reasoning as above: the wrapper klib of a SNAPSHOT version comes from `mavenLocal`.
        // The trap: a POM-only `kotlin-stdlib` in the local repository has no JS variant, and Gradle
        // does not fall back once it has found metadata — the compiler then fails on
        // `Unresolved reference 'js'` in a file that never mentions it. Keep the Kotlin coordinates
        // pinned to Central so a stray local POM cannot shadow them.
        mavenLocal()
        exclusiveContent {
            forRepository { mavenCentral() }
            filter {
                includeGroup("org.jetbrains.kotlin-wrappers")
                includeModule("org.jetbrains.kotlin", "kotlin-js-plain-objects")
                includeGroup("org.jetbrains.kotlin")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "kmp-reflect"

// The runtime API: annotations, the metadata model, the registry, dynamic proxies and inlined
// resources. This is what a consumer compiles against.
include(":kmp-reflect-core")

// The KSP processor that turns annotated classes into metadata. A plain JVM module: KSP runs inside
// the Kotlin compiler on the build machine, never on the target platform.
include(":kmp-reflect-ksp")

// The demo page, published under /demo/ of the Pages site. Not a published module. It applies KSP
// itself, so it doubles as the integration test for both the JVM and the JS target.
include(":demo")

// The documentation site: Kotlin configuration plus Markdown, assembled by the ktjs-docusaurus
// Gradle plugin into a static site that `pagesSite` publishes at the Pages root.
include(":docs")
