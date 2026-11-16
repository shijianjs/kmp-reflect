# 技术决策

本文件记录 v0.1 已经定下、后续上下文不要反复推翻的决定。实现细节留在源码的 KDoc 与文档站里。

## 1. 版本与坐标

- 单一项目版本：`gradle.properties` 的 `kmp-reflect.version`（`0.1.0`），工作树加 `-SNAPSHOT`，
  `-Pkmp.reflect.release=true` 去掉后缀。所有发布模块共用同一版本。
- 模板的「上游 npm 版本 + `-kt.N` 修订号」机制不再适用：本项目不包装 npm 包，也没有上游发版节奏。
  `wrapper-revisions.properties` 已删除。
- group `io.github.shijianjs`，模块名 `kmp-reflect-core`、`kmp-reflect-ksp`。

## 2. 目标平台

- 边界是「KSP 能生成到哪」，不是「历史上支持到哪」：**JVM、Kotlin/JS、Kotlin/Wasm、Kotlin/Native 全部支持**。
  KSP 2 会为每个配置了处理器的目标各建一个符号处理任务，生成的仍是普通 Kotlin 源码，所以后端只要能编译
  它，元数据就成立。
- native 目标**按构建宿主声明**（Windows `mingwX64`、Linux `linuxX64`、macOS `macosArm64`/`macosX64`）。
  理由不是「Native 不支持」，而是「Native 的 compilation 必须由能链接它的宿主来构建」：声明交叉目标只会让
  本机 `./gradlew build` 失败。发布侧的 klib 覆盖宿主能构建的那些，消费方按需解析变体。
- Android 现在由 JVM 产物覆盖（没有 `androidTarget` 发布），所以运行库不许碰 JDK 专属 API。
- core 仍是纯 Kotlin，唯一的平台差异在 `PlatformReflection`：JVM 返回全限定名、Native 返回简单名、
  JS/Wasm 返回 `null`。reified 查找据此退化，字符串查找在所有平台一致。

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
  使用方显式 `KmpReflection.register(DemoReflection)` 一次；`KmpClasses.of<T>()` 依赖这份注册。
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

- core：`commonTest` 覆盖模型与注册表语义，跑在宿主能执行的每个目标上（JVM、JS Node、Wasm Node、宿主 native）。
- 集成：`demo` 模块的 `commonMain` 放被标注的模型类，KSP 作用于每个声明了 `ksp<Target>` 的目标；
  `commonTest` 的 `ReflectionTest` 断言同一份生成结果在四个后端上一致（jvm → `jvmTest`/`reflectionTest`，
  js → `jsNodeTest`/`jsBrowserTest`，wasm → `wasmJsNodeTest`，native → `<target>Test`）。
- 页面：`e2eTest`（Playwright + JUnit 5）跑打包后的 demo 页面，只在 JVM 上。

## 9. 本地镜像与 CI

- 仓库与 CI 只用官方源；本机镜像走 `~/.gradle/init.d/aliyun-mirror.gradle` 与 `~/.npmrc`，不入库。
- 刷新 `kotlin-js-store/package-lock.json` 时必须先切回官方 npm 源，否则 CI 报 `Lock file was changed`。

## v0.2 候选（不在 v0.1 计分范围）

- Gradle 插件：自动按 target 配 KSP（含 native 的循环注册）、自动传资源目录与注册表选项。
- `androidTarget` 发布，让 Android 消费方拿到自己的变体。
- `suspend` 函数的调用入口。
- 接口属性、默认参数参与代理。
- 注解实例化（`KmpAnnotation` 直接返回注解实例）。
- 增量编译下的更细粒度失效（v0.1 一次全量生成，功能正确但粒度粗）。
- 交叉 native 目标（iOS 等）的发布矩阵：需要 macOS 构建机，属于发布流程而非库能力。
