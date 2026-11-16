# AGENTS.md

`kmp-reflect` 是 Kotlin Multiplatform 的编译期反射库。KSP 处理器扫描标了 `@KmpReflect` 注解或实现了
`KmpReflectable` 的类，把元数据、代理实现、内联资源写成普通 Kotlin 源码；运行时只有一张注册表，没有
加载、扫描或真正的反射。

## 模块

| 目录 | 作用 |
| --- | --- |
| `kmp-reflect-core/` | 运行时（`commonMain`）：`KmpClass`、`KmpClasses`、`KmpReflection`、`proxy/`、`resource/`。发布。 |
| `kmp-reflect-ksp/` | KSP 处理器（纯 JVM）：`KmpReflectProcessor` + `Model`/`SymbolConverter` + 三个 `*Writer`。发布。 |
| `gradle/kmp-reflect-gradle-plugin/` | Gradle 插件（included build，独立构建）：按目标注册处理器、转发 `kmp.reflect.*` 选项、把资源目录声明成 KSP 任务输入。发布。 |
| `demo/` | KMP（jvm + js executable）示例页面，同时是各目标的端到端测试。不发布。 |
| `docs/` | Docusaurus 站点（Kotlin 写配置 + Markdown）。不发布。 |

`settings.gradle.kts` include 了 `:kmp-reflect-core`、`:kmp-reflect-ksp`、`:demo`、`:docs` 四个子工程，
另在 `pluginManagement` 里 `includeBuild("gradle/kmp-reflect-gradle-plugin")`——它在独立构建里才能被
`:demo` 以 `id("io.github.shijianjs.kmp-reflect")` 应用。缺子工程会让 `pagesSite`、CI 失败；插件构建的
版本来自根 `gradle.properties`（读文件，不读 `version` 对象）。

## 常用命令

```bash
./gradlew build                          # 编译 + detekt + 宿主各目标的测试（jvmTest 已禁用，见下）
./gradlew :demo:allTests                 # 元数据断言在每个目标上跑（JVM / JS / Wasm / Native）
./gradlew :demo:reflectionTest           # 同一批断言，只跑 JVM
./gradlew :demo:jsBrowserDistribution    # demo 页面 bundle → demo/build/dist/js/productionExecutable
./gradlew :demo:e2eTest                  # Playwright 驱动上面那个 bundle（需要 Chromium）
./gradlew :docs:docusaurusBuild          # 文档站（先跑一次 kotlinNpmInstall）
./gradlew pagesSite                      # 文档 + demo + Dokka 合成到 build/pages
```

单测过滤：两个测试任务都用 JUnit Platform，`./gradlew :demo:reflectionTest --tests '*Reflection*'`。

浏览器测试在本地首次运行前需要：

```powershell
$env:PLAYWRIGHT_DOWNLOAD_HOST = "https://npmmirror.com/mirrors/playwright/"
```

否则浏览器下载会卡住（只影响本机，见「依赖源」）。

## 代码约定

- **公开 API 一律 `Kmp` 前缀**：本库在 JVM 上也能用，那里 `java.lang.reflect` 的类型满地都是，
  不加前缀会撞名，也看不出代码里混用了两套。
- **生成的代码不是源码**：改生成逻辑要改 `kmp-reflect-ksp` 里的 Writer，不要去 `build/generated` 里
  手改，也不要把生成物提交进仓库。
- **运行时保持 `commonMain`**：目前只有一个 `expect`/`actual`（`PlatformReflection.kt`，JVM 返回全限定名、
  Native 返回简单名、JS/Wasm 返回 `null`）。新增平台支持时优先改这里，不要在 core 里加平台特化。
- **目标按「KSP 能生成到哪」决定，不按「以前支持到哪」决定**：core 声明 jvm / js / wasmJs，native 按
  构建宿主三选一（`mingwX64` / `linuxX64` / Apple）。加新目标 = 声明目标 + 一个 `PlatformReflection`
  actual，**不要**再手写 `add("ksp<Target>")`：`kmp-reflect-gradle-plugin` 按配置名自动给每个目标注册
  处理器（跳过 `Test` / `Metadata`），也不许为了省事把 target 从 `build` 里剔掉。
- **文本文件用 LF**，临时产物放 `target/` 或 `build/`，不要污染根目录。
- **`@PublishedApi internal`** 的成员是给内联函数用的，改签名前先 `grep` 调用点。

## 演示与文档

- demo 页面在 `demo/src/jsMain/kotlin/.../demo/page/`，按区块一个类（`HeaderBar`、`ClassBrowser`、
  `InvocationSection` …），`DemoPage` 只负责挂载；`Main.kt` 只注册元数据并把宿主元素交给 `DemoPage`。
- 页面写法按 `.agents/skills/ui-code-style`：节点用 `TagName<T>.createElement { }` 建、用字段持有，
  状态变化只改相关节点（`applyCopy` / `toggleSelection`），**不要**引入 `render()` 式整页重绘；
  语言切换走各区块的 `applyCopy`，不重建 DOM。
- **静态样式进 sheet，内联只留给运行时值**：每个区块自己有一份 `X_SHEET`（`HEADER_SHEET`、
  `SECTION_SHEET`、`OUTPUT_SHEET`、`BROWSER_SHEET`、`RESOURCES_SHEET`），节点只带 class；
  `installPageStyle(HEADER_SHEET, …)` 一次性 adopt 到 `document.adoptedStyleSheets`（`PAGE_SHEET` 只留
  `box-sizing` reset 和 `.page` 本身）。`inlineStyle` 只用于运行时才知道、会频繁变的那一两个属性。
  理由是性能：样式与 DOM 实例是一对多，一条 class 规则覆盖所有同类节点，内联则是 N 份文本 + N 次解析。
- 每个 class 标记都是 `Theme.kt` 里的一条 `internal val X_CLASS: ClassName`，调用点写
  `classList.add(X_CLASS)`。**不要**加 `addClass("row")` 这类把字符串透传一层的包装（`classList.add`
  本来就只吃 `ClassName`，包一层反而让拼错的类名照样编译）。
- 会随 `:hover` 变的属性（背景、边框色、文字色）必须在 sheet 里（内联优先级更高，会盖掉 hover 规则）。
  `<button>` 这类 UA 自带 `buttontext` 颜色的控件必须显式给 `color`，否则在深色底上看不见。
- 有限枚举的状态（选中行、invokable 徽标）用 class 切换（`.row.is-selected`、`.badge-ok`），不要内联颜色；
  `:hover` 与选中态同时存在时，选中规则写在 hover 规则**之后**，靠同特异性下的先后顺序生效。
- **不要**再新增 `demo.css` 之类的集中式样式表：规则跟着组件走，sheet 只是同一文件里的一个常量。
- 文档站点的“全功能”配置是一套，别砍：`docs/build.gradle.kts` 的 `kitBundle` / `extractKitBundle` /
  `clientModuleFunction` / `browserBundle(-Files)`、`docs/.../docs/ClientModule.kt`、
  `Config.kt` 里的 `clientModules` / `themes` / `markdown.mermaid` / `themeConfig.mermaid`。首页是
  `pages/index.mdx` 里的 `<ktjs-hero>` / `<ktjs-features>` / `<ktjs-next-steps>`（中英各一份），
  `guide/platforms` 有一张 mermaid 图。改配置前先确认它确实跑不了，去掉功能不是解决方式。
- 文档英文源在 `docs/src/jsMain/resources/docs/`，中文镜像在
  `i18n/zh-Hans/docusaurus-plugin-content-docs/current/`。**改一页就要同步另一页**，否则站点两种语言
  内容不一致。
- 站点布局：文档在 `/`，demo 在 `/demo/`，Dokka 在 `/api/`。`Config.kt` 里的 `SITE_BASE_URL`、
  navbar 里的两个外链、`pagesSite` 的目录结构三者必须一致。
- 中文页内链写 `to="/guide/xxx"` 即可，Docusaurus 会自己补 locale 前缀；手写 `/zh-Hans/...` 会变成
  `/zh-Hans/zh-Hans/...`。

## 技术决策（别推翻）

`docs/development/technical-decisions.md` 记录了 9 条决策，摘要：

- 资源**内联进生成的 Kotlin**（Base64），不读运行时 classpath —— 否则目标平台要么没有文件系统、
  要么没有统一的资源约定。
- 扫描只覆盖**已注册的类**，并且只有标了注解/实现接口的类才会被生成 —— 不遍历 classpath。
- 目标覆盖「KSP 能生成到哪」：JVM、JS、Wasm、Native 全部支持；native 只声明宿主这一种，避免
  `build` 要求宿主链接它链接不了的东西。
- 版本号单一来源：`gradle.properties` 的 `kmp-reflect.version`，工作树加 `-SNAPSHOT`，
  发布传 `-Pkmp.reflect.release=true`。

## 依赖源

仓库、CI 里**只用官方源**（Maven Central / npmjs / Playwright 官方）。本机拉不动时可以在
`~/.gradle/init.d/` 或环境变量里指镜像，但这些配置**不进仓库**，也不许写进 `gradle.properties`。
改 `package-lock.json` 前先 `$env:npm_config_registry = "https://registry.npmjs.org/"` 再
`./gradlew kotlinUpgradePackageLock`。

## 已知坑

- KSP 生成目录被别的进程占用时删不掉：先 `./gradlew --stop`，把 shell 的 cwd 移出
  `build/generated/**` 再重跑。
- Kotlin/JS 与 Wasm 没有 `KClass.qualifiedName`，Native 只有 `simpleName`：需要类名的 reified API 在这些
  平台上退化，那里用 `KmpClasses.byName(...)` 这类字符串重载。
- **Kotlin/Native 不允许测试函数名里出现 `,`、`'` 等特殊字符**（`Name contains illegal characters`）。
  `commonTest` 的方法名必须全平台可编译，所以别用标点，用空格分词。
- KotlinPoet 的 `%M` 渲染枚举常量会出乱码，用 `%T.%L`。
- 生成代码里空注解数组要写成 `emptyList<Any?>()`，写 `listOf()` 会类型推断失败。
- `commonTest` 的 `kotlin("test")` 与 jvmTest 的 `kotlin("test-junit5")` 会在
  `kotlin-test-framework-impl` capability 上冲突；demo 的 `build.gradle.kts` 里用
  `capabilitiesResolution` 指定 JVM 侧选 junit5，别删那段。
- Wasm 目标有自己的 npm 锁：`kotlin-js-store/wasm/package-lock.json`，加 Wasm 依赖时要一起刷新并提交。

## 长期任务

本仓库用 `.agents/skills/long-task-tracking` 管理跨上下文进度：

1. 读本文件 → 2. 读 `TODO.md` → 3. 读 `docs/development/tasks.md` 与相关专项文档 →
4. `git status` 核对工作树 → 5. 核对文档状态与代码真实状态 → 6. 直接继续待办事项。

完成后立即回填 `docs/development/tasks.md` 的勾选与统计，改完再更新 `TODO.md` 的总数
（总数 = 已完成 + 明确不做 + 待完成）。文档与代码不一致时以代码为准，并修正文档。
