# AGENTS.md

`kmp-reflect` 是 Kotlin Multiplatform 的编译期反射库。KSP 处理器扫描标了 `@KmpReflect` 注解或实现了
`KmpReflectable` 的类，把元数据、代理实现、内联资源写成普通 Kotlin 源码；运行时只有一张注册表，没有
加载、扫描或真正的反射。

## 模块

| 目录 | 作用 |
| --- | --- |
| `kmp-reflect-core/` | 运行时（`commonMain`）：`KmpClass`、`KmpClasses`、`KmpReflection`、`proxy/`、`resource/`。发布。 |
| `kmp-reflect-ksp/` | KSP 处理器（纯 JVM）：`KmpReflectProcessor` + `Model`/`SymbolConverter` + 三个 `*Writer`。发布。 |
| `demo/` | KMP（jvm + js executable）示例页面，同时是两个目标的端到端测试。不发布。 |
| `docs/` | Docusaurus 站点（Kotlin 写配置 + Markdown）。不发布。 |

`settings.gradle.kts` 必须 include 这四个；缺一个不影响其他模块，但 `pagesSite`、CI 会失败。

## 常用命令

```bash
./gradlew build                          # 编译 + detekt + 单元测试（jvmTest 已禁用，见下）
./gradlew :demo:reflectionTest           # JVM 目标上的 12 项元数据断言
./gradlew :demo:jsBrowserDistribution    # demo 页面 bundle → demo/build/dist/js/productionExecutable
./gradlew :demo:e2eTest                  # Playwright 驱动上面那个 bundle（需要 Chromium）
./gradlew :docs:docusaurusBuild          # 文档站（先跑一次 kotlinNpmInstall）
./gradlew pagesSite                      # 文档 + demo + Dokka 合成到 build/pages
```

单测过滤：两个测试任务都用 JUnit Platform，`./gradlew :demo:reflectionTest --tests '*PersonTest*'`。

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
- **运行时保持 `commonMain`**：除 `PlatformReflection.kt` 的一处 `expect`/`actual`（JVM 返回
  `qualifiedName`，JS 返回 `null`）之外，不许加平台特化，否则跨平台一致性就没了。
- **文本文件用 LF**，临时产物放 `target/` 或 `build/`，不要污染根目录。
- **`@PublishedApi internal`** 的成员是给内联函数用的，改签名前先 `grep` 调用点。

## 演示与文档

- demo 页面的 DOM 全部由 Kotlin 构建（`web.dom` / `web.html`），**不要拼 HTML 字符串**；两套文案在
  `Main.kt` 的 `Copy` 里，`render()` 整页重绘实现中英切换。
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
- 不支持 Native / Wasm：KSP 产物接不进那些 compilation。
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
- Kotlin/JS 没有 `KClass.qualifiedName`：需要类名的 reified API 在 JS 上解析失败，那里用
  `KmpClasses.byName(...)`。
- KotlinPoet 的 `%M` 渲染枚举常量会出乱码，用 `%T.%L`。
- 生成代码里空注解数组要写成 `emptyList<Any?>()`，写 `listOf()` 会类型推断失败。

## 长期任务

本仓库用 `.agents/skills/long-task-tracking` 管理跨上下文进度：

1. 读本文件 → 2. 读 `TODO.md` → 3. 读 `docs/development/tasks.md` 与相关专项文档 →
4. `git status` 核对工作树 → 5. 核对文档状态与代码真实状态 → 6. 直接继续待办事项。

完成后立即回填 `docs/development/tasks.md` 的勾选与统计，改完再更新 `TODO.md` 的总数
（总数 = 已完成 + 明确不做 + 待完成）。文档与代码不一致时以代码为准，并修正文档。
