---
title: 简介
description: 用 KSP 在编译期为 Kotlin Multiplatform 生成接近 JVM 的反射能力。
---

# kmp-reflect

Kotlin Multiplatform 没有反射：`commonMain` 无法查询类的名字、成员或注解，Kotlin/JS 更是完全没有。
`kmp-reflect` 把这件活在编译期完成：KSP 处理器遍历你声明参与反射的类，把元数据写成普通 Kotlin
源码，然后提供一套形体接近 `java.lang.reflect` 子集、但类型安全的 API。

```kotlin
KmpReflection.register(DemoReflection)                       // 生成的模块对象

val person = KmpClasses.of<Person>()
    .constructor(3)
    ?.new("Ada", 36, "hidden") as? Person

KmpClasses.of<Person>().function("greet", 1)?.invoke(person, "Hello")  // "Hello, Ada!"
```

运行时不加载、不扫描、不额外分配：元数据已经在代码里，每条“反射调用”都是紧挨着目标类生成的普通
Kotlin 调用。

## 能得到什么

- **类元数据** —— 名称、包名、种类、可见性、修饰符、父类型、类型参数、构造函数、函数、属性、
  枚举项、伴生对象与嵌套类。
- **注解数据** —— 注解类型与实参，不依赖平台注解支持即可读取；扫描之所以能成立，是因为注解本身被
  写进了元数据。
- **调用能力** —— 构造函数、函数与属性访问器都能真正执行。签名无法表达的成员（`suspend`、泛型）
  仍然会被描述，只是标记为不可调用。
- **动态代理** —— 每个参与反射的接口生成一个实现类，把所有调用转发给你的 handler；这就是脱离 JVM
  的 `java.lang.reflect.Proxy`。
- **内联 classpath 资源** —— 路径、MIME 与字节直接编进生成的代码，任何目标都不需要文件系统或网络。

以上全部生成为普通 Kotlin 源码，所以 **KSP 能喂进去的每个后端都成立**：JVM、Kotlin/JS、Kotlin/Wasm、
Kotlin/Native。见[平台](guide/platforms.md)。

## 命名约定

所有公开类型都带 `Kmp` 前缀（`KmpClass`、`KmpFunction`、`KmpClasses`）：本库在 JVM 上同样可用，那里
`java.lang.reflect` 的类型无处不在。`KmpClass` 不会与 `Class` 冲突，也让两者混用的代码一眼可辨。

## 接下来

- [开始使用](guide/getting-started.md) —— 配置 Gradle 并反射第一个类。
- [元数据](guide/metadata.md) —— `KmpClass` 暴露了什么。
- [动态代理](guide/proxy.md)、[内联资源](guide/resources.md)。
- [平台](guide/platforms.md) 与 [限制](guide/limitations.md) —— 哪些目标支持到什么程度。
- [demo](https://shijianjs.github.io/kmp-reflect/demo/) 在真实浏览器里跑这些元数据，
  [API 文档](https://shijianjs.github.io/kmp-reflect/api/) 由 Dokka 生成。
