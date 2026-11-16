---
title: Platforms
sidebar_position: 6
description: Which targets are supported, and why Native and Wasm are not.
---

# Platforms

## Targets

| Target | Metadata | Invocation | Proxy | Resources |
| --- | --- | --- | --- | --- |
| JVM | yes | yes | yes | yes |
| Kotlin/JS (browser, Node) | yes | yes | yes | yes |
| Kotlin/Native | not supported | — | — | — |
| Kotlin/Wasm | not supported | — | — | — |

The runtime library is `commonMain` only: no `expect`/`actual` except a single helper that reports a
class name, which exists on the JVM and returns `null` on Kotlin/JS. Everything the library answers
therefore comes from generated data, never from the platform.

## Why not Native or Wasm

KSP generates Kotlin **sources**, and a KSP task is wired to the compilations that can compile them.
Neither Kotlin/Native nor Kotlin/Wasm has a KSP task output that can be added to its compilation in a
way that is stable across versions, so those targets would have nothing to compile. Metadata still
describes them conceptually — the limitation is in the build, not in the model.

## Kotlin/JS specifics

- `KClass.qualifiedName` does not exist. The processor writes the names into the metadata, so lookups
 by name are exact; only the reified helpers that need a runtime name step cannot resolve on JS. Use
  `KmpClasses.byName("com.example.Person")` there.
- `KClass.isInstance` does exist, which is what `KmpTypeRef.isInstance` and `KmpClass.isInstance` use.
- A JS interface has no runtime class, so `KmpClasses.of(SomeInterface::class)` is best avoided; use
  the name. Classes resolve by identity.

## Building this repository

```bash
# metadata for both targets, plus the reflection assertions
./gradlew :demo:compileKotlinJs :demo:compileKotlinJvm :demo:reflectionTest

# the demo page bundle, then the browser suite that drives it
./gradlew :demo:jsBrowserDistribution :demo:e2eTest

# the documentation site and the API reference
./gradlew :docs:docusaurusBuild :kmp-reflect-core:dokkaHtml

# everything together, into build/pages: docs at the root, demo at /demo, API at /api
./gradlew pagesSite
```

`pagesSite` is what the Pages workflow publishes. See `docs/development/technical-decisions.md` in the
repository for the reasoning behind each choice.
