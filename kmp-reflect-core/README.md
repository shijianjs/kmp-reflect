# kmp-reflect-core

The runtime half of [kmp-reflect](../README.md): the metadata model, the registry, proxies and
inlined resources.

`commonMain` only — no `expect`/`actual` beyond one helper that reports a class name (real on the
JVM, `null` on Kotlin/JS), and no dependency beyond the Kotlin standard library.

```kotlin
implementation("io.github.shijianjs:kmp-reflect-core:<version>")
```

Everything here is used through generated code; a project normally never constructs these types
itself. Start at [`KmpClasses`](src/commonMain/kotlin/io/github/shijianjs/kmp/reflect/KmpClasses.kt)
for lookups, [`KmpClass`](src/commonMain/kotlin/io/github/shijianjs/kmp/reflect/KmpClass.kt) for the
metadata model, and `proxy/` and `resource/` for the two extension APIs.

Documentation: <https://shijianjs.github.io/kmp-reflect/>.
