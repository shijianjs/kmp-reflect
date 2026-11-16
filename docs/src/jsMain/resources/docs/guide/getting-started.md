---
title: Getting started
sidebar_position: 2
description: Gradle setup, KSP options, and the first reflected class.
---

# Getting started

## Gradle setup

The library is two artifacts: the runtime (`kmp-reflect-core`, a multiplatform library) and the
processor (`kmp-reflect-ksp`, a plain JVM artifact that KSP loads inside the compiler).

```kotlin title="build.gradle.kts"
plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

kotlin {
    jvm()
    js { nodejs() }

    sourceSets {
        commonMain.dependencies {
            implementation("io.github.shijianjs:kmp-reflect-core:<version>")
        }
    }
}

dependencies {
    // KSP runs once per target: register the processor for each target you build.
    add("kspJvm", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspJs", "io.github.shijianjs:kmp-reflect-ksp:<version>")
}
```

`<version>` is the release you want, for example `0.1.0`. The artifacts are published to Maven Central
under the `io.github.shijianjs` group.

## Opting a class in

Two ways, and they can be mixed:

```kotlin
@KmpReflect                       // annotation, with per-class options
class Person(val name: String)

object Toolbox : KmpReflectable   // marker interface, no options
```

Both are honoured only for `public` declarations (and, with `includeNonPublic`, also `internal` and
`protected`) in the current module. A nested class is reflected only when it is opted in itself; a
companion object is always pulled in with its class. `@KmpReflectIgnore` removes a single member — a
function or property — from an otherwise reflected class.

## Pointing the processor at a module object

The processor writes one metadata file per class plus one **module object** that lists them. Name it
with a KSP argument:

```kotlin
ksp {
    // where<package>:<ObjectName> — becomes `object DemoReflection : KmpReflectModule`
    arg("kmp.reflect.registry", "com.example.demo:DemoReflection")
    arg("kmp.reflect.module", "demo")
}
```

At start-up, register it once. Every lookup and scan afterwards only sees what was registered:

```kotlin
KmpReflection.register(DemoReflection)

val personClass = KmpClasses.of<Person>()
```

## Using it

```kotlin
val personClass = KmpClasses.of<Person>()

personClass.properties.map { it.name }              // [name, age, greeting]
personClass.function("greet", 1)?.isInvokable       // true

val instance = personClass.constructor(3)?.new("Ada", 36, "hidden")
personClass.function("greet", 1)?.invoke(instance, "Hi")   // "Hi, Ada!"
```

Arguments are passed as `Any?` and cast inside the generated lambda, so the declared types still apply:
passing a wrong type fails with a `ClassCastException` at the call site, exactly like `Method.invoke`.

## Other KSP options

| Option | Meaning |
| --- | --- |
| `kmp.reflect.registry` | `package:ObjectName` of the generated module object. Without it, no registry is written. |
| `kmp.reflect.module` | The name reported by `DemoReflection.module`. Defaults to the Kotlin module name. |
| `kmp.reflect.resources` | Directories to inline as resources, separated by commas, semicolons or the path separator. See [Inlined resources](resources.md). |
| `kmp.reflect.visibility` | `all` (default) or `public`: `public` drops everything that is not `public`, whatever the annotation says. |

A project with several modules registers several module objects, and each module carries its own
`kmp.reflect.registry`. Registration order does not matter; a later registration replaces an earlier
one with the same qualified name.
