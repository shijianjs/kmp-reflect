# 技术决策

本文件记录 v0.1 已经定下、后续上下文不要反复推翻的决定。实现细节留在源码的 KDoc 与文档站里。

## 1. 版本与坐标

- 单一项目版本：`gradle.properties` 的 `kmp-reflect.version`（`0.1.0`），工作树加 `-SNAPSHOT`，
  `-Pkmp.reflect.release=true` 去掉后缀。所有发布模块共用同一版本。
- 模板的「上游 npm 版本 + `-kt.N` 修订号」机制不再适用：本项目不包装 npm 包，也没有上游发版节奏。
  `wrapper-revisions.properties` 已删除。
- group `io.github.shijianjs`，模块名 `kmp-reflect-core`、`kmp-reflect-ksp`。

## 2. 目标平台

- core 与 ksp 只做 `jvm` + `js(IR)`。
- **明确不做 Kotlin/Native 与 Wasm**：KSP 不生成到这些目标的编译产物，本项目的全部价值来自 KSP 生成代码，
  在这两类目标上提供 core 产物只会给用户一个「能编译、但没有元数据」的空壳。等 KSP 支持后再评估。
- core 是纯 Kotlin，理论上可加 native 目标；但那样会对外承诺一个用不了的 API，故不加。

## 3. 元数据形态：编译期内联

- 每个被标注的类生成一段 `KmpClass` 构造代码（`private val ... by lazy { KmpClass(...) }`），
  成员调用能力以 lambda 形式内联（`invoke = { receiver, args -> (receiver as Foo).bar(args[0] as Int) }`）。
- 不做运行时类加载、不读资源文件、不做失败重试：生成的是普通 Kotlin 源码，跨平台一致，
  消费方编译不过就是编译不过，不会留到运行时才炸。
- 类型信息用自建的 `KmpTypeRef` 表达（`qualifiedName` / `arguments` / `nullable` / `runtime: KClass<*>?`），
  不依赖 `kotlin-reflect`（JVM-only）。

## 4. 命名

- 公开 API 一律带 `Kmp` 前缀或位于 `io.github.shijianjs.kmp.reflect` 包下：
  `KmpClass`、`KmpFunction`、`KmpProperty`、`KmpConstructor`、`KmpParameter`、`KmpAnnotation`、
  `KmpTypeRef`、`KmpClasses`、`KmpProxy`、`KmpResources`、`KmpReflectModule`。
- 明确不叫 `KClass` / `KFunction` / `KProperty` / `Class`：KMP 在 JVM 上也能跑，同名会让 JVM 反射与
  本项目 API 在同一文件里无法区分。

## 5. 注册与扫描

- 元数据按类生成，聚合由一个「模块对象」完成：`kmp.reflect.registry=<包名>:<对象名>` 选项开启，
  生成 `object <对象名> : KmpReflectModule`，内含 `classes` 与 `resources`。
- 不做自动注册（没有 `init` 钩子可依赖，JVM 的 `ServiceLoader` 只在 JVM 上存在）。
  使用方显式 `KmpClasses.register(DemoReflection)` 一次；`KmpClasses.of<T>()` 依赖这份注册。
- 扫描（类扫描、注解扫描）只在已注册的模块范围内进行，这正是「只有标记了反射的类才支持扫描」的实现方式。

## 6. 动态代理

- 不用 `java.lang.reflect.Proxy`（JVM-only）。KSP 为每个被标注的接口生成一个代理类，
  每个成员直接调用 `KmpInvocationHandler`，因此代理在 JS 上同样可用。
- v0.1 代理只覆盖函数；接口里的属性、`suspend` 函数、带默认值的参数不生成代理成员（见 limitations）。

## 7. classpath 资源

- `kmp.reflect.resources=<目录列表>` 指向资源根目录，KSP 在编译期读出文件内容，
  以 Base64 字符串内联进生成的 `KmpResource(path, mimeType, size) { Base64.decode(...) }`。
- MIME 由扩展名推断（内置小表），未知扩展名回落 `application/octet-stream`。
- 路径一律相对资源根，`/` 分隔；不做运行时 I/O，不做 classpath 探测。

## 8. 测试策略

- core：`commonTest` 覆盖模型与注册表语义（jvm + js 双目标）。
- 集成：`demo` 模块的 `commonMain` 放被标注的模型类，KSP 同时作用于 jvm 与 js 目标；
  `jvmTest` 直接断言生成结果（构造、调用、属性、注解扫描、代理、资源）。
- 页面：`e2eTest`（Playwright + JUnit 5）跑打包后的 demo 页面。

## 9. 本地镜像与 CI

- 仓库与 CI 只用官方源；本机镜像走 `~/.gradle/init.d/aliyun-mirror.gradle` 与 `~/.npmrc`，不入库。
- 刷新 `kotlin-js-store/package-lock.json` 时必须先切回官方 npm 源，否则 CI 报 `Lock file was changed`。

## v0.2 候选（不在 v0.1 计分范围）

- Gradle 插件：自动按 target 配 KSP、自动传资源目录与注册表选项。
- Kotlin/Native / Wasm 目标（取决于 KSP）。
- `suspend` 函数的调用入口。
- 接口属性、默认参数参与代理。
- 注解实例化（`KmpAnnotation` 直接返回注解实例）。
- 增量编译下的更细粒度失效（v0.1 一次全量生成，功能正确但粒度粗）。
