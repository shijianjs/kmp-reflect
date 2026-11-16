---
title: Platforms
sidebar_position: 6
description: Every Kotlin backend KSP can generate into, and what differs on each.
---

# Platforms

## Targets

| Target | Metadata | Invocation | Proxy | Resources | Test run |
| --- | --- | --- | --- | --- | --- |
| JVM (also Android apps) | yes | yes | yes | yes | `jvmTest` / `reflectionTest` |
| Kotlin/JS (browser, Node) | yes | yes | yes | yes | `jsNodeTest` / `jsBrowserTest` |
| Kotlin/Wasm (`wasmJs`) | yes | yes | yes | yes | `wasmJsNodeTest` |
| Kotlin/Native | yes | yes | yes | yes | `<nativeTarget>Test` |

The runtime library is `commonMain`: one `expect`/`actual` pair reports a class name, and everything the
library answers otherwise comes from generated data. **KSP generates Kotlin sources, so every backend
that KSP can feed is a backend this library works on** — the generated code is plain Kotlin, with
`kotlin.io.encoding.Base64` and ordinary calls.

## Native targets are declared by host

A Kotlin/Native compilation is always built for the host that runs the build, so this repository
declares exactly one native target:

| Build host | Native target |
| --- | --- |
| Windows | `mingwX64` |
| Linux (CI) | `linuxX64` |
| macOS | `macosArm64`, `macosX64` |

That keeps `./gradlew build` honest — it never asks for an executable the host cannot link or run — and
it does not limit what gets published: a klib is produced for the declared targets, and every consumer
resolves the variant it needs. If you need iOS or Android Native klibs, publish from a host that can
build them (Apple targets need macOS); declaring a cross target you cannot link only makes your own
`build` fail.

The demo follows the same rule: its `commonTest` metadata assertions run on whichever native target the
host declared, so a Windows checkout proves Native works through `mingwX64Test`.

## Wiring KSP per target

KSP 2 has no catch-all configuration: the processor is registered per target, and KSP creates one task
per target that has it.

```kotlin
dependencies {
    add("kspJvm", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspJs", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspWasmJs", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspMingwX64", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspLinuxX64", "io.github.shijianjs:kmp-reflect-ksp:<version>")
}
```

For a project with many native targets, loop over them:

```kotlin
kotlin.targets
    .filter { it.name != "metadata" }
    .forEach { target ->
        add("ksp${target.name.replaceFirstChar { it.uppercaseChar() }}",
            "io.github.shijianjs:kmp-reflect-ksp:<version>")
    }
```

The processor itself is a JVM artifact: it runs inside the compiler on the build host, whichever
backend the compilation is for. `./gradlew :your-module:dependencies | grep ksp` lists the exact
configuration names of your project.

## What differs per backend

Class **identity** works everywhere. Names do not:

| Backend | `KClass` name available | Consequence |
| --- | --- | --- |
| JVM | qualified name | reified helpers are exact |
| Kotlin/Native | simple name only | `annotatedWith<A>()` and `implementing(K::class)` match by simple name; use the string overloads for exactness |
| Kotlin/JS, Kotlin/Wasm | none | reified lookups fall back to identity; look up by name |

The metadata itself always carries the fully qualified name, so `KmpClasses.byName("com.example.Person")`
answers identically everywhere. Only the reverse direction — from a `KClass` to metadata — changes shape,
and it degrades in a way that never returns wrong data: it returns emptier results, and
`docs/guide/metadata.md` shows the string-based form to use when that matters.

## Building this repository

```bash
# metadata for the host's targets, plus the reflection assertions on each of them
./gradlew :demo:allTests

# the browser bundle, then the Playwright suite that drives it
./gradlew :demo:jsBrowserDistribution :demo:e2eTest

# documentation site and API reference
./gradlew :docs:docusaurusBuild :kmp-reflect-core:dokkaHtml

# everything, into build/pages: docs at the root, demo at /demo, API at /api
./gradlew pagesSite
```

`pagesSite` is what the Pages workflow publishes. Per-target test tasks are the usual KMP names:
`jvmTest`, `jsNodeTest`, `jsBrowserTest`, `wasmJsNodeTest`, `mingwX64Test`, `linuxX64Test`.

The wasm target declares no linked binary, so building this repository never downloads binaryen (
a separate ~20 MB tool that Gradle fetches from GitHub releases the first time a wasm binary is optimized).
