---
title: Inlined resources
sidebar_position: 5
description: Files become Base64 literals in generated Kotlin, with path and MIME type.
---

# Inlined resources

Reading a file at runtime means a file system, a bundler convention, or a network — none of which is
portable across every Kotlin target. So resources are read **at compile time** and written into
generated Kotlin.

## Configuration

```kotlin
kmpReflect {
    // One or more directories; the plugin passes them to the processor and declares them as inputs
    // of the KSP tasks, so editing a resource reruns processing.
    resources.from(layout.projectDirectory.dir("data"))
}
```

Without the plugin, the same thing goes to a KSP argument — and the directories then have to be
declared as task inputs by hand:

```kotlin
ksp {
    // One or more directories, separated by ',', ';' or the path separator.
    arg("kmp.reflect.resources", layout.projectDirectory.dir("data").asFile.absolutePath)
}
```

Each file below those directories becomes one `KmpResource`, keyed by its path **relative to that
directory**. `data/config.json` therefore appears as `config.json`; `data/icons/star.svg` as
`icons/star.svg`. Directory order decides which copy wins when two directories contain the same path.

The MIME type comes from the extension — `application/json`, `image/svg+xml`, `text/markdown`,
`font/woff2`, and so on — with `application/octet-stream` as the fallback, so non-standard files are
still describable.

## The generated shape

```kotlin title="generated"
internal val kmpResource0: KmpResource =
    KmpResource(path = "config.json", mimeType = "application/json", size = 155) { kmpResourceBytes0 }

private val kmpResourceBytes0: ByteArray by lazy { Base64.decode("ewogICJ0aXRsZSI6…") }
```

Base64 rather than a byte-array literal: it is the shortest portable text form that survives every
target's source encoding. Decoding is `lazy`, so a resource that is never opened costs nothing beyond
the string constant, and the module object lists them all for the registry:

```kotlin
KmpResources.all().map { "${it.path} — ${it.mimeType} (${it.size} bytes)" }
```

## Reading one

```kotlin
KmpResources.byPath("config.json").text()          // decoded as UTF-8
KmpResources.byPath("icons/star.svg").bytes        // raw bytes
KmpResources.allIn("icons")                        // every resource in that directory
KmpResources.directories()                         // distinct directories, sorted
```

`text()` is a convenience, not a decoder: it assumes UTF-8 and does not inspect the MIME type.

## Working with a bundle

Everything else — a loader, a key-value facade, an asset manifest — can be built on top of
`path + mime + bytes`; the library deliberately stops there. A `KmpResource` is a value, so a helper
that needs a cache or a decoder can keep its own.

## Incremental builds

The resource directories are not declared as KSP inputs, so a build that already generated the
metadata may not notice an added or edited file. When a resource looks stale:

```bash
./gradlew --rerun-tasks :your-module:kspKotlinJvm
```

This is a known limitation of v0.1 and the first thing a Gradle plugin for this project would fix by
registering the directories as task inputs.
