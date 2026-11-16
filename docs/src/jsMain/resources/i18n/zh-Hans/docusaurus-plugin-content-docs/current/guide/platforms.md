---
title: 平台
description: 支持哪些目标，以及为什么不支持 Native 与 Wasm。
---

# 平台

## 目标

| 目标 | 元数据 | 调用 | 代理 | 资源 |
| --- | --- | --- | --- | --- |
| JVM | 支持 | 支持 | 支持 | 支持 |
| Kotlin/JS（浏览器、Node） | 支持 | 支持 | 支持 | 支持 |
| Kotlin/Native | 不支持 | — | — | — |
| Kotlin/Wasm | 不支持 | — | — | — |

运行库只有 `commonMain`：除“取类名”这一个 `expect`/`actual` 之外没有平台特化，而它在 JVM 上返回真实
名称、在 Kotlin/JS 上返回 `null`。因此本库给出的答案全部来自生成数据，而不是平台。

## 为什么没有 Native 与 Wasm

KSP 生成的是 Kotlin **源码**，而 KSP 任务要挂到能编译这些源码的 compilation 上。Native 与 Wasm 目前
没有可稳定接入的 KSP 产物落点，所以这些目标没有东西可编译。元数据模型本身并不排斥它们，限制在构建
这一侧。

## Kotlin/JS 细节

- `KClass.qualifiedName` 不存在。处理器把名字写进了元数据，所以按名查找是精确的；只有需要运行时类名
  的 reified 辅助方法在 JS 上解析不了。那里请用 `KmpClasses.byName("com.example.Person")`。
- `KClass.isInstance` 存在，`KmpTypeRef.isInstance` 与 `KmpClass.isInstance` 正是用它。
- JS 接口没有运行时类，因此 `KmpClasses.of(SomeInterface::class)` 最好避免，改用名字。类则按身份
  （identity）解析。

## 构建本仓库

```bash
# 两个目标的元数据，以及反射断言
./gradlew :demo:compileKotlinJs :demo:compileKotlinJvm :demo:reflectionTest

# demo 页面 bundle，以及驱动它的浏览器测试
./gradlew :demo:jsBrowserDistribution :demo:e2eTest

# 文档站与 API 文档
./gradlew :docs:docusaurusBuild :kmp-reflect-core:dokkaHtml

# 全部合成到 build/pages：文档在根，demo 在 /demo，API 在 /api
./gradlew pagesSite
```

Pages 工作流发布的就是 `pagesSite` 的产物。每条决策的理由见仓库里的
`docs/development/technical-decisions.md`。
