# kmp-reflect

<p align="center"><img src="docs/src/jsMain/resources/static/img/kmp-reflect-logo.png" width="120" alt="kmp-reflect"></p>

Compile-time reflection for Kotlin Multiplatform: a KSP processor writes class metadata, annotation
data, dynamic proxies and inlined resources as ordinary Kotlin source, so `commonMain` code gets an
API shaped like a typed subset of `java.lang.reflect` — on the JVM, in the browser and in Node.

**[Documentation](https://shijianjs.github.io/kmp-reflect/)** ·
**[Live demo](https://shijianjs.github.io/kmp-reflect/demo/)** ·
**[API reference](https://shijianjs.github.io/kmp-reflect/api/)** ·
**[中文说明](README.zh-CN.md)**

```kotlin
KmpReflection.register(DemoReflection)          // generated module object

val personClass = KmpClasses.of<Person>()
val instance = personClass.constructor(3)?.new("Ada", 36, "hidden")
personClass.function("greet", 1)?.invoke(instance, "Hello")   // "Hello, Ada!"
```

Nothing is loaded or scanned at runtime: the metadata is already in the binary, and every call it
describes is an inlined Kotlin call generated next to the class it belongs to.

## Modules

| Module | Published | Role |
| --- | --- | --- |
| [kmp-reflect-core](kmp-reflect-core) | yes | The runtime: `KmpClass`, `KmpClasses`, proxies, resources. `commonMain` only. |
| [kmp-reflect-ksp](kmp-reflect-ksp) | yes | The KSP processor: reads sources, writes metadata and module objects. Plain JVM. |
| [demo](demo) | no | Browser demo, and the integration test for both targets. |
| [docs](docs) | no | Docusaurus site: `/`, `/demo/` and `/api/` on GitHub Pages. |

## Usage

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

kotlin {
    jvm()
    js { nodejs() }
    sourceSets.commonMain.dependencies {
        implementation("io.github.shijianjs:kmp-reflect-core:<version>")
    }
}

dependencies {
    // One configuration per target; add the ones your project actually builds.
    add("kspJvm", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspJs", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspWasmJs", "io.github.shijianjs:kmp-reflect-ksp:<version>")
}

ksp {
    arg("kmp.reflect.registry", "com.example.demo:DemoReflection")
}
```

Annotate a class with `@KmpReflect` (or make it implement `KmpReflectable`), register the generated
module object once, then look classes up by name, by `KClass` or with a reified type. See
[Getting started](https://shijianjs.github.io/kmp-reflect/guide/getting-started).

## Features

- **Class metadata** — names, kind, visibility, modifiers, supertypes, type parameters, constructors,
  functions, properties, enum entries, companion and nested classes.
- **Annotation data and scanning** — annotation types and typed arguments, `annotatedWith(name)`,
  `implementing(name)`.
- **Invocation** — constructors, functions and accessors called through metadata; `suspend` and generic
  members are described but reported as not invokable.
- **Dynamic proxies** — a generated implementation per reflected interface, forwarding to a
  `KmpInvocationHandler`.
- **Inlined resources** — path, MIME type and Base64 bytes written into generated Kotlin, with a
  `KmpResources` registry.
- **No runtime machinery** — no class loading, no scanning, no reflection; works identically on JVM,
  Kotlin/JS browser and Node.js.

## Targets

Every backend KSP can generate Kotlin sources for — the runtime is `commonMain`, so nothing else has to
exist for a target:

| Target | Support |
| --- | --- |
| JVM (and Android applications, via the JVM artifact) | metadata, invocation, proxies, resources |
| Kotlin/JS (browser, Node) | metadata, invocation, proxies, resources; reified lookups fall back to identity |
| Kotlin/Wasm (`wasmJs`) | metadata, invocation, proxies, resources; reified lookups fall back to identity |
| Kotlin/Native | metadata, invocation, proxies, resources; reified lookups match by simple name |

KSP is configured per target (`kspJvm`, `kspJs`, `kspWasmJs`, `kspMingwX64`, …) — there is no catch-all
configuration in KSP 2. This repository declares one native target per build host (`mingwX64` on
Windows, `linuxX64` on Linux, the Apple ones on macOS), so `./gradlew build` never asks for an
executable the host cannot link.

## Building

```bash
./gradlew build                       # compile everything, detekt, tests on the host's targets
./gradlew :demo:allTests              # the metadata assertions on each target (JVM, JS, Wasm, Native)
./gradlew :demo:reflectionTest        # the same assertions, JVM only
./gradlew :demo:jsBrowserDistribution # the demo page bundle
./gradlew :demo:e2eTest               # Playwright suite over the bundle (Chromium)
./gradlew pagesSite                   # docs + demo + Dokka, assembled into build/pages
```

Nothing in the repository depends on a mirror registry, so CI resolves from the official ones; a
local machine may point Gradle and npm at mirrors without touching anything committed.

## Versioning

`gradle.properties` holds a single `kmp-reflect.version`; a working tree appends `-SNAPSHOT`, and the
release workflow passes `-Pkmp.reflect.release=true` to drop it. Every publishable module shares one
version — they are released together.

## License

MIT — see [LICENSE](LICENSE).
