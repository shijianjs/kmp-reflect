---
title: 平台
sidebar_position: 6
description: KSP 能生成代码的每个 Kotlin 后端，以及各后端之间的差异。
---

# 平台

## 目标

| 目标 | 元数据 | 调用 | 代理 | 资源 | 测试任务 |
| --- | --- | --- | --- | --- | --- |
| JVM（Android 应用同样可用） | 支持 | 支持 | 支持 | 支持 | `jvmTest` / `reflectionTest` |
| Kotlin/JS（浏览器、Node） | 支持 | 支持 | 支持 | 支持 | `jsNodeTest` / `jsBrowserTest` |
| Kotlin/Wasm（`wasmJs`） | 支持 | 支持 | 支持 | 支持 | `wasmJsNodeTest` |
| Kotlin/Native | 支持 | 支持 | 支持 | 支持 | `<nativeTarget>Test` |

运行库只有 `commonMain`：一处 `expect`/`actual` 负责报告类名，其余答案全部来自生成数据。
**KSP 生成的是 Kotlin 源码，所以 KSP 能喂进去的每个后端，本库都成立** —— 生成的代码是普通 Kotlin，
只用到 `kotlin.io.encoding.Base64` 与普通调用。

## Native 目标按宿主声明

Kotlin/Native 的 compilation 永远是为「跑构建的这台机器」构建的，因此本仓库只声明一个 native 目标：

| 构建宿主 | native 目标 |
| --- | --- |
| Windows | `mingwX64` |
| Linux（CI） | `linuxX64` |
| macOS | `macosArm64`、`macosX64` |

这样 `./gradlew build` 永远是诚实的 —— 不会要求宿主编译或运行它做不到的可执行文件；而这并不限制
发布内容：声明的目标各自产出 klib，消费方按需解析变体。如果确实需要 iOS / Android Native 的 klib，
从能构建它们的宿主发布（Apple 目标需要 macOS）；声明一个自己链接不了的交叉目标，只会让自己这边
`build` 失败。

demo 用同一条规则：`commonTest` 里的元数据断言在宿主声明的那个 native 目标上运行，于是 Windows
工作树通过 `mingwX64Test` 就能证明 Native 可用。

## 按目标接入 KSP

KSP 2 没有 catch-all 配置：处理器按目标注册，KSP 为每个配了处理器的目标各建一个任务。

```kotlin
dependencies {
    add("kspJvm", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspJs", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspWasmJs", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspMingwX64", "io.github.shijianjs:kmp-reflect-ksp:<version>")
    add("kspLinuxX64", "io.github.shijianjs:kmp-reflect-ksp:<version>")
}
```

native 目标很多时直接循环：

```kotlin
kotlin.targets
    .filter { it.name != "metadata" }
    .forEach { target ->
        add("ksp${target.name.replaceFirstChar { it.uppercaseChar() }}",
            "io.github.shijianjs:kmp-reflect-ksp:<version>")
    }
```

处理器本身是纯 JVM 产物：无论 compilation 面向哪个后端，它都在构建宿主的编译器进程里运行。
`./gradlew :your-module:dependencies | grep ksp` 能列出本项目准确的配置名。

## 各后端的差异

类的**身份**在所有后端都可用，名字则不然：

| 后端 | 可用的 `KClass` 名字 | 影响 |
| --- | --- | --- |
| JVM | 全限定名 | reified 辅助方法精确 |
| Kotlin/Native | 只有简单名 | `annotatedWith<A>()`、`implementing(K::class)` 按简单名匹配；需要精确时改用字符串重载 |
| Kotlin/JS、Kotlin/Wasm | 没有 | reified 查找退化为身份比较；请按名字查找 |

元数据里始终带全限定名，所以 `KmpClasses.byName("com.example.Person")` 在所有平台结果一致。只有反方向
（从 `KClass` 找到元数据）会因平台而变，而且退化方式不会给出错误数据：结果更空。需要精确时用
`docs/guide/metadata.md` 里的字符串写法。

## 构建本仓库

```bash
# 宿主所有目标的元数据，以及每个目标上的反射断言
./gradlew :demo:allTests

# 浏览器 bundle，以及驱动它的 Playwright 套件
./gradlew :demo:jsBrowserDistribution :demo:e2eTest

# 文档站与 API 文档
./gradlew :docs:docusaurusBuild :kmp-reflect-core:dokkaHtml

# 全部合成到 build/pages：文档在根，demo 在 /demo，API 在 /api
./gradlew pagesSite
```

Pages 工作流发布的就是 `pagesSite` 的产物。各目标的测试任务就是 KMP 的常规名字：`jvmTest`、
`jsNodeTest`、`jsBrowserTest`、`wasmJsNodeTest`、`mingwX64Test`、`linuxX64Test`。

Wasm 目标不声明链接产物，所以构建本仓库不会下载 binaryen（一个约 20 MB 的独立工具，Gradle 只在
第一次优化 wasm 二进制时从 GitHub releases 拉它）。
