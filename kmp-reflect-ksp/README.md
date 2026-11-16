# kmp-reflect-ksp

The KSP processor half of [kmp-reflect](../README.md): it reads annotated sources at build time and
writes the metadata that `kmp-reflect-core` exposes.

A plain JVM module — KSP runs inside the Kotlin compiler on the build machine, never on a target
platform — registered per target, because KSP 2 has no catch-all configuration:

```kotlin
dependencies {
    add("kspJvm", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspJs", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspWasmJs", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspMingwX64", "io.github.shijianjs:kmp-reflect-ksp:<version>")
}
```

Works for every backend KSP can generate into: JVM, Kotlin/JS, Kotlin/Wasm and Kotlin/Native.

Supported options: `kmp.reflect.registry`, `kmp.reflect.module`, `kmp.reflect.resources`,
`kmp.reflect.visibility`. See
[Getting started](https://shijianjs.github.io/kmp-reflect/guide/getting-started).

Layout: [`KmpReflectProcessor`](src/main/kotlin/io/github/shijianjs/kmp/reflect/ksp/KmpReflectProcessor.kt)
collects declarations, [`SymbolConverter`](src/main/kotlin/io/github/shijianjs/kmp/reflect/ksp/SymbolConverter.kt)
turns symbols into [`Model`](src/main/kotlin/io/github/shijianjs/kmp/reflect/ksp/Model.kt), and
`MetadataWriter`, `ProxyWriter` and `RegistryWriter` emit the Kotlin sources.
