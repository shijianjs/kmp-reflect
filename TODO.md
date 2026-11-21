# TODO

## kmp-reflect v0.1：编译期反射生成

用 KSP 在编译期把 KMP 类的元数据生成为 Kotlin 代码（内联，无运行时扫描、无资源加载），使被注解或实现
`KmpReflectable` 的类获得接近 JVM 反射的编程体验，命名统一带 `Kmp` 前缀以和 JVM 反射区分。
目标覆盖「KSP 能生成到哪」：JVM、Kotlin/JS、Kotlin/Wasm、Kotlin/Native。

- 总计：91
- 已完成：90
- 明确不做：1
- 待完成：0

详细清单：[docs/development/tasks.md](docs/development/tasks.md)
技术决策：[docs/development/technical-decisions.md](docs/development/technical-decisions.md)
工程说明：[AGENTS.md](AGENTS.md)

### 当前待办

91 项已全部收敛（90 项完成、1 项明确不做）。剩下的都是 v0.2 候选，见
[technical-decisions.md](docs/development/technical-decisions.md) 末节：

- [ ] `androidTarget` 发布，让 Android 消费方拿到自己的变体
- [ ] Gradle 插件随主仓库发布到 Central 并验证 plugin marker（插件本身已落地，见第 78 项）
- [ ] 代理支持具体属性（调用方提供存储）
- [ ] 交叉 native 目标（iOS 等）的发布矩阵

### 注意事项

- 源仓库是 `ktjs-wrapper-template` 的克隆，npm wrapper 相关产物已全部移除；版本改用
  `gradle.properties` 的 `kmp-reflect.version`，工作树加 `-SNAPSHOT`。
- 演示页的外壳已换成 `ktjs-demo-kit`（`ktjs-demo-kit.version=0.1.0-SNAPSHOT`，由 `ktjs-dom-kit` 工作树
  `publishToMavenLocal` 提供）：八个区块各占一条路由，标题与说明由 `page/lang/Translation.kt` 提供，颜色一律走
  Lumo token，所以跟随外壳的亮暗主题。原来那层 `<kmp-section>` 与手写 header 已删除。
- 反射元数据一律编译期内联成 Kotlin 代码，不做运行时类加载、不做资源文件读取。
- 公开 API 一律带 `Kmp` 前缀，包名 `io.github.shijianjs.kmp.reflect`。
- Native 目标按构建宿主声明（Windows `mingwX64` / Linux `linuxX64` / macOS Apple），交叉目标只在能
  链接它的宿主上构建；Wasm 目标不声明链接产物，`build` 不需要 binaryen。
- 本地依赖可走国内镜像，仓库与 CI 只用官方源。
- 提交信息用中文简述；每个阶段完成后提交一次。

## kmp-reflect v0.2：审查修正

2026-11-22 对代码审查结论的复核与补强，逐条明细在
[docs/development/tasks.md](docs/development/tasks.md) 的 S 节。

- 总计：9
- 已完成：8
- 明确不做：1
- 待完成：0

### 当前待办

无。S 批的 92–99 全部落地，100 明确不做；四个后端 `:demo:allTests` 各 26 项全绿。

### 注意事项

- `private` / `protected` 成员与构造器不进元数据：生成文件在类外，调用它们无法编译。
- 注册表是惰性的：`KmpReflection.register` 只记名字，模块的 `classes` 由 `classNames + load` 派生；
  不要把 `classes` 改回 `listOf(…)`，那会让生成的 `by lazy` 全部提前求值。
- `vararg` 成员的调用必须走文件私有桥函数：Kotlin/Wasm 在 lowering 阶段处理「函数值里 spread 一个 cast
  结果」时会内部报错，`*(args[0] as Array<T>)` 只能出现在普通函数体里。
- `kotlin.jvm.*` 注解（`@JvmInline` 等）不要写 `runtime = X::class`：非 JVM 目标上是 optional expectation，
  Native 链接会把整个初始化器报成 `IrConstantObjectImpl`；commonMain 里用 `@JvmInline` 记得显式 import。
- 泛型类的接收者一律写 `Box<*>`（`ClassModel.ownerType()`），不要再退化成裸 `Box`。
- 数据类合成成员保留现状（`copy` / `componentN` 照常反射），过滤选项明确不做，理由见 tasks.md。
