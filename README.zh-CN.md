# kmp-reflect

<p align="center"><img src="docs/src/jsMain/resources/static/img/kmp-reflect-logo.png" width="120" alt="kmp-reflect"></p>

Kotlin Multiplatform 的编译期反射：KSP 处理器把类元数据、注解数据、动态代理与内联资源写成普通 Kotlin
源码，于是 `commonMain` 里可以用到一套形似 `java.lang.reflect` 类型化子集的 API —— JVM、浏览器、
Node 上表现一致。

**[文档](https://shijianjs.github.io/kmp-reflect/zh-Hans/)** ·
**[在线演示](https://shijianjs.github.io/kmp-reflect/demo/)** ·
**[API 文档](https://shijianjs.github.io/kmp-reflect/api/)** ·
**[English](README.md)**

```kotlin
KmpReflection.register(DemoReflection)          // 生成的模块对象

val personClass = KmpClasses.of<Person>()
val instance = personClass.constructor(3)?.new("Ada", 36, "hidden")
personClass.function("greet", 1)?.invoke(instance, "Hello")   // "Hello, Ada!"
```

运行时不加载、不扫描：元数据已经在产物里，每条“反射调用”都是紧挨着目标类生成的普通 Kotlin 调用。

## 模块

| 模块 | 是否发布 | 作用 |
| --- | --- | --- |
| [kmp-reflect-core](kmp-reflect-core) | 是 | 运行时：`KmpClass`、`KmpClasses`、代理、资源，全部在 `commonMain`。 |
| [kmp-reflect-ksp](kmp-reflect-ksp) | 是 | KSP 处理器：读源码，写元数据与模块对象。纯 JVM。 |
| [kmp-reflect-gradle-plugin](gradle/kmp-reflect-gradle-plugin) | 是 | Gradle 插件：给每个目标注册处理器并转发选项。 |
| [demo](demo) | 否 | 浏览器 demo，同时是两个目标的集成测试。 |
| [docs](docs) | 否 | Docusaurus 站点：在 GitHub Pages 上提供 `/`、`/demo/`、`/api/`。 |

## 使用

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("io.github.shijianjs.kmp-reflect")
}

kotlin {
    jvm()
    js { nodejs() }
    sourceSets.commonMain.dependencies {
        implementation("io.github.shijianjs:kmp-reflect-core:<version>")
    }
}

kmpReflect {
    // 每个已声明的目标都会拿到处理器，不用按平台重复写。
    registry.set("com.example.demo:DemoReflection")
}
```

给类加 `@KmpReflect` 注解（或让它实现 `KmpReflectable`），注册一次生成的模块对象，之后就能按名字、
按 `KClass` 或按 reified 类型查找。详见
[开始使用](https://shijianjs.github.io/kmp-reflect/zh-Hans/guide/getting-started)。

## 能力

- **类元数据** —— 名称、种类、可见性、修饰符、父类型、类型参数、构造函数、函数、属性、枚举项、
  伴生对象与嵌套类。
- **注解数据与扫描** —— 注解类型与类型化实参，`annotatedWith(name)`、`implementing(name)`。
- **调用** —— 构造函数、函数、访问器都能通过元数据执行；`suspend` 成员走 `invokeSuspend`；泛型成员
  会被描述，但标记为不可调用。
- **动态代理** —— 每个参与反射的接口生成一个实现类，转发给 `KmpInvocationHandler`。
- **内联资源** —— 路径、MIME 与 Base64 字节写进生成的 Kotlin，并提供 `KmpResources` 注册表。
- **没有运行时机制** —— 不加载类、不扫描、不反射；JVM、Kotlin/JS 浏览器、Node.js 行为一致。

## 平台

KSP 能为哪些后端生成 Kotlin 源码，本库就支持哪些 —— 运行库全在 `commonMain`，目标不需要额外产物：

| 目标 | 支持 |
| --- | --- |
| JVM（Android 应用经 JVM 产物消费） | 元数据、调用、代理、资源 |
| Kotlin/JS（浏览器、Node） | 元数据、调用、代理、资源；reified 查找退化为身份比较 |
| Kotlin/Wasm（`wasmJs`） | 元数据、调用、代理、资源；reified 查找退化为身份比较 |
| Kotlin/Native | 元数据、调用、代理、资源；reified 查找按简单名匹配 |

KSP 按目标配置（`kspJvm`、`kspJs`、`kspWasmJs`、`kspMingwX64` ……），KSP 2 没有 catch-all 配置——这正是
Gradle 插件补上的那一步。本仓库按构建宿主声明一个 native 目标（Windows 用 `mingwX64`、Linux 用
`linuxX64`、macOS 用 Apple 的），因此 `./gradlew build` 永远不会要求宿主编译它链接不了的可执行文件。

## 构建

```bash
./gradlew build                       # 全量编译、detekt、宿主各目标的测试
./gradlew :demo:allTests              # 每个目标上的元数据断言（JVM、JS、Wasm、Native）
./gradlew :demo:reflectionTest        # 同一批断言，只跑 JVM
./gradlew :demo:jsBrowserDistribution # demo 页面 bundle
./gradlew :demo:e2eTest               # 针对 bundle 的 Playwright 套件（Chromium）
./gradlew pagesSite                   # 文档 + demo + Dokka，合成到 build/pages
```

仓库里没有任何东西依赖镜像源，CI 始终走官方仓库；本机可以把 Gradle 与 npm 指向国内镜像，但不会
提交到仓库。

## 版本

`gradle.properties` 里只有一个 `kmp-reflect.version`；工作树构建会加 `-SNAPSHOT`，发布流程传
`-Pkmp.reflect.release=true` 去掉它。所有发布模块共用一个版本，一起发。

## 许可

MIT —— 见 [LICENSE](LICENSE)。
