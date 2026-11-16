---
title: 限制
description: v0.1 做不到什么，以及后续计划。
---

# 限制

下面每一条都是决策而非疏漏；每条都写明了后果，便于判断是否影响你。

## 模型

- **调用 `suspend` 函数必须在协程里。** suspend 函数会被描述，也能调用，但只能从协程内部通过
  `KmpFunction.invokeSuspend` 调用：生成的 lambda 带 `suspend` 修饰，给不出可交给调度器的同步入口。
  该 lambda 用的是标准库的 `suspend` 函数类型，不引入协程依赖。
- **不能泛型调用。** 签名含类型参数的函数或构造函数无法通过 `Any?` 实参调用，因为生成的调用点没有可
  转型的目标类型。它们会被描述。
- **没有扩展成员。** 扩展函数与扩展属性不参与反射；它们在字节码意义上也不是类的成员。
- **调用时不套默认值。** `invoke` 不会应用默认参数：每个参数都要传。
- **父类型链接止于模块边界。** `superclasses` 链接的是**同一模块内**参与反射的直接父类型，
  `allSuperclasses` 沿这条链上溯。来自其他产物的父类型只出现在 `supertypes` 里（那边的生成元数据是
  `internal`）；需要跨已注册类按名字匹配时用 `KmpClasses.implementing(name)`。
- **注解是快照。** 值在编译期从源码读取；实参来自别处的表达式只有在它是字面量时才会被原样记录。

## 代理

- 仅接口、仅函数、无 `suspend`、无类型参数；准确规则见[动态代理](proxy.md)。
- v0.1 不转发任何属性，具体属性也不行 —— 不猜测存储方式就无法让编译器满意。

## 资源

- MIME 来自内置扩展名表；未知扩展名回退 `application/octet-stream`，`text()` 始终按 UTF-8 解码。
- 资源目录尚未注册为增量输入，资源可能过期，需要 `--rerun-tasks`。

## 平台

- **reified 查找的效果取决于后端能提供多少 `KClass` 信息。** JVM 暴露全限定名，所以
  `KmpClasses.of<T>()`、`annotatedWith<A>()`、`implementing(K::class)` 在那里是精确的。Kotlin/Native
  只有简单名，这三个方法按简单名匹配；Kotlin/JS 与 Kotlin/Wasm 完全没有名字，于是按身份解析或返回空。
  元数据里始终带全限定名：需要精确时用字符串重载（`byName`、`annotatedWith(name)`、
  `implementing(name)`）。
- **native 目标按构建宿主声明。** 本仓库只构建宿主能链接、能运行的那一个 native 目标（`mingwX64`、
  `linuxX64` 或 Apple 的那些）。消费别人发布的其它 native 目标 klib 没问题；自己构建则需要匹配的宿主。
- **Android 由 JVM 产物覆盖。** 目前没有 `androidTarget` 发布：Android 应用消费 JVM 变体，这也是运行库
  不碰 JDK 专属 API 的原因之一。

## API 面

- 注册是全局的，同名类后写覆盖先写；没有作用域隔离，`clear()` 主要供测试使用。
- `KmpInvocationHandler` 只拿到位置实参；命名参数、默认值、vararg 语义由调用方自己负责。

## v0.2 候选

1. Gradle 插件：把资源目录声明为 KSP 输入，并按目标（含所有已声明目标）自动配置选项。
2. 代理支持具体属性（由调用方提供 map 存储）。
3. `androidTarget` 发布，让 Android 消费方拿到自己的变体，而不是回落到 JVM 变体。
