# kmp-reflect v0.1 任务清单

## 范围与口径

- 任务来源：本轮用户需求（编译期反射、动态代理、类/注解扫描、classpath 资源内联、demo + 双语文档、
  Pages 发布）。
- 统计口径：一个事项 = 一项可直接定位到源码或文档的能力/产物。
- 状态：`待完成` / `已完成` / `明确不做`，`总数 = 已完成 + 明确不做 + 待完成`。
- 核对日期：2026-11-16。

## 统计

| 状态 | 数量 |
|---|---:|
| 总计 | 83 |
| 已完成 | 82 |
| 明确不做 | 1 |
| 待完成 | 0 |

已完成事项不再逐条列出；签名、验证依据见下方各节末尾的「验证」注记。
共有三批：A–I 是 v0.1 的库本体与站点，J 是 2026-11-16 追加的平台扩展（KSP 2 已支持 KMP 的 Native 与
Wasm，原先「只做 JVM + JS」的边界因此作废），其中 75–78 是同一轮之后的逐次推进（suspend、三宿主 CI、
父类型链接、Gradle 插件）；K 是 demo 页面重构与文档站配置还原。

## 已完成

### A. 工程与构建（8）

1 `settings.gradle.kts` 改名 `kmp-reflect`，纳入 core / ksp / demo / docs
2 版本策略统一为 `gradle.properties` 的 `kmp-reflect.version`
3 移除 npm 版本映射与 `wrapper-revisions.properties`
4 根构建脚本声明 KSP 插件
5 Detekt 配置迁移到本项目（`detekt.yml` 由根脚本应用到四个子模块）
6 Dokka 聚合改名与模块名（`kmp-reflect`，输出 `build/dokka/html`）
7 发布配置（group / POM / scm / hub-staging）改名
8 `kotlin-js-store/package-lock.json` 刷新（官方源重新生成）

验证：`./gradlew build`、`./gradlew detekt`、`./gradlew pagesSite`、`./gradlew kotlinUpgradePackageLock` 均通过。

### B. core API（12）

9 `@KmpReflect`、`@KmpReflectIgnore` 注解与 `KmpReflectable` 标记接口
10 `KmpKind` / `KmpVisibility` / `KmpModifier` / `KmpTypeKind` 枚举
11 `KmpTypeRef`
12 `KmpAnnotation`
13 `KmpParameter`
14 `KmpFunction`
15 `KmpProperty`
16 `KmpConstructor`
17 `KmpEnumEntry`
18 `KmpClass`
19 `KmpClasses` 注册表（注册 / 按名查询 / 扫描）
20 `KmpReflectModule` SPI 与 `KmpReflectException`

### C. 动态代理（4）

21 `KmpInvocationHandler` 接口
22 `KmpProxy.create` 与 `KmpClass.newProxy`
23 KSP 侧代理类代码生成
24 代理行为测试（`reflect.proxy` 断言：`KmpClasses.of<Greeter>().newProxy(...)` 转发并回传返回值）

### D. 资源内联（4）

25 `KmpResource` / `KmpResources`
26 KSP 资源目录扫描与 MIME 推断
27 Base64 内联代码生成
28 资源测试（路径查询、文本与二进制、MIME）

### E. KSP processor（9）

29 `SymbolProcessorProvider` 注册（`META-INF/services`）
30 注解类发现（含嵌套类：嵌套类需自己标注解）
31 标记接口 `KmpReflectable` 发现（扫描全部文件）
32 成员过滤（可见性 / `@KmpReflectIgnore`）
33 类型、修饰符、注解参数转换
34 `KmpClass` 元数据代码生成
35 模块注册表对象生成（类 + 资源）
36 诊断信息（不可生成的签名给出明确错误/警告：不可调用的函数、不可代理的接口都会打印原因）
38 处理器选项解析（`kmp.reflect.registry` / `kmp.reflect.module` / `kmp.reflect.resources` /
`kmp.reflect.visibility`）

### F. 测试（6）

39 core `commonTest`：模型查询与注册表语义（`KmpClassesTest`，11 个用例，JVM 与 Node 双跑）
40 demo JVM 集成测试：构造 / 调用 / 属性读写
41 demo JVM 集成测试：注解扫描与类扫描
42 demo JVM 集成测试：动态代理
43 demo JVM 集成测试：内联资源
44 Pages 页面 e2e 测试（Playwright，6 个用例）

验证：`./gradlew build`（含 `:kmp-reflect-core:jvmTest` + `jsNodeTest`）、`./gradlew :demo:reflectionTest`
（12 项）、`./gradlew :demo:e2eTest`（18 项，含 12 反射 + 6 浏览器）。

### G. demo（8）

45 demo 页面骨架（类型化 DOM 组装，不拼 HTML 字符串）
46 类列表与元数据展示
47 构造与函数调用交互
48 注解扫描展示
49 动态代理展示
50 内联资源展示（含 SVG data URI 预览）
51 中英文切换
52 demo 样式

验证：`./gradlew :demo:jsBrowserDistribution` 产出 `demo/build/dist/js/productionExecutable/`，
`:demo:e2eTest` 驱动该产物。

### H. 文档站（8，每页中英文双语）

53 站点配置（标题 / baseUrl / logo / locale）
54 侧边栏与导航（Demo、API 跳转）
55 `intro` 页
56 `getting-started` 页（KSP 接入步骤）
57 注解内容（并入 `metadata` 的「注解」一节，未单开页）
58 `metadata` 页（API 与生成代码结构）
59 `proxy` 页
60 `scanning` + `resources` 页（扫描并入 `metadata`，资源单开 `resources`）
61 `limitations` 页（Native/Wasm、泛型、suspend、vararg 等）

另有清单外新增页 `platforms`（目标支持矩阵与构建命令），中英文各一份。

验证：`./gradlew :docs:docusaurusBuild` 通过，en 与 zh-Hans 两套静态文件均生成，Docusaurus 链接检查无断链。

### I. CI 与发布（5）

62 `ci.yml` 适配（PR + dispatch，JDK 21，`build` + `pagesSite` + `:demo:e2eTest`）
63 `pages.yml` 适配（push main + dispatch；用 gh 开启 Pages，`build_type=workflow`）
64 `dependabot.yml` 适配（npm 分组改 `@docusaurus/*`）
65 `release.yml` 适配（`-Pkmp.reflect.release=true`，去掉 revision 语义）
66 根 README（en / zh-CN）

### J. 平台扩展（8，2026-11-16 追加）

67 core 增加 `wasmJs` 目标，并补 `PlatformReflection` 的 wasm actual（`null`：Wasm 不暴露类名）
68 core 按构建宿主声明 native 目标（`mingwX64` / `linuxX64` / Apple），并补 native actual（`simpleName`）
69 `KClass` 名称能力分级：`KmpTypeRef.matches`、`annotatedWith(name)`、`implementing(name)` 同时接受
全限定名与简单名，reified 查找在三个后端上都不再静默失败
70 demo 接入 `kspWasmJs` 与宿主 native 的 `ksp<Target>`，元数据在四个后端各生成一次
71 反射断言从 `jvmTest` 迁到 `commonTest`：同一份 12 项断言在 JVM、JS、Wasm、Native 上各跑一遍
72 `kotlin-js-store/wasm/package-lock.json` 纳入仓库（Wasm 目标有独立的 npm 锁）
73 文档同步新事实：`platforms` 页重写、`limitations` 改成平台差异表、`getting-started` 给按目标注册与循环
写法、`intro` / README ×2 / AGENTS 一并更新
74 CI 增加 `~/.konan` 缓存（Linux runner 现在要构建 `linuxX64`，没有缓存每次都要下 native 工具链）
75 `suspend` 函数可调用：处理器为它生成 `suspend` lambda，运行时新增 `KmpFunction.invokeSuspend`
76 CI 改成三宿主矩阵（ubuntu / macos / windows 各链接自己的 native 目标），浏览器与站点单独一个 job
77 同模块内父类型递归反射：`KmpClass.superclasses` 直接链接到父类元数据，`allSuperclasses` 沿链上溯
78 Gradle 插件 `io.github.shijianjs.kmp-reflect`：按 `ksp<Target>` 配置名给每个目标注册处理器，
转发 `kmp.reflect.*` 选项，并把资源目录声明成 KSP 任务输入（改动即重跑）

验证：`:demo:reflectionTest`（JVM）、`:demo:jsBrowserTest`、`:demo:wasmJsNodeTest`、`:demo:mingwX64Test`
四套断言全绿，`:demo:e2eTest` 18 项全绿；`./gradlew build` 通过（含 detekt）。
CI（Linux runner）首跑实测：`jsBrowserTest`、`linuxX64Test`、`wasmJsNodeTest` 均通过，`build` 2m10s，
`pagesSite` 27s，`:demo:e2eTest` 15s；konan 缓存首次为冷未命中（符合预期，第二次起命中）。
75 的验证：`Calculator.slowAdd` 在四个后端都由 `commonTest` 调用成功（`runSuspend` 用标准库
`startCoroutine`，不引入协程依赖），`invoke` 对它抛 `KmpReflectException`。
76 的验证：三个 `Verify on …` job 全绿，各自跑出 `linuxX64Test` / `macosArm64Test` / `mingwX64Test`。
77 的验证：demo 里 `Calculator : Machine()`，四个后端都断言 `superclasses` / `allSuperclasses` 指向
`Machine` 的元数据并能调用它的 `power()`；跨模块父类型仍只在 `supertypes` 里。本机与三宿主 CI 均通过。
78 的验证：`:demo` 改为 `kmpReflect { … }`，删掉手写的四条 `add("ksp<Target>")` 与 `ksp { arg }`；
`:demo:dependencies --configuration ksp{Jvm,Js,WasmJs,MingwX64}` 各解析到处理器、`kspJvmTest` 为空；
在 `demo/data` 加一个文件后 `:demo:kspKotlinJvm` 从 UP-TO-DATE 变为执行并把它内联进生成代码，删掉后
再执行一次即消失；`./gradlew build` 通过（四个后端的断言全部重跑）。CI run `43848505244` 四个 job 全绿，
三台宿主各自冷缓存编译了 `:kmp-reflect-gradle-plugin:compileKotlin`（Linux 1m21s、macOS 2m48s、
Windows 5m45s），并跑出 `linuxX64Test` / `macosArm64Test` / `mingwX64Test`。

### K. demo 页面重构与文档站配置还原（5，2026-11-16 追加）

79 demo 页面接入 `ktjs-dom-kit`：`TagName<T>.createElement { }` 按标签收窄建节点、`inlineStyle` 走
csstype 类型化样式，删掉 `document.createElement("ul")` 这类魔法字符串、`as HTMLElement` 强转与
`unsafeCast<dynamic>(this).onclick`
80 页面按业务拆包到 `page/`（`HeaderBar`、`ClassBrowser`、`InvocationSection` … `LimitationsSection`、
`SectionShell`、`OutputList`、`Copy`、`Theme`），保留模式：字段持节点、只有命名 setter
（`applyCopy` / `toggleSelection`）、仅局部集合用 `replaceChildren`；语言切换不再整页重建
81 样式从 `demo.css` 迁出：只有内联表达不了的规则（`tr.row:hover`、`.row.is-selected`、
`.lang-switch:hover`、`box-sizing`）进 `Theme.kt` 的 `PAGE_SHEET`，`installPageStyle()` 用
`document.adoptedStyleSheets` 挂一次；`index.html` 去掉样式表链接，`demo.css` 删除
82 docs 还原模板的全功能形态：`docs/build.gradle.kts` 补 `kitBundle` + `extractKitBundle`（解 klib 的
`ktjs-client/**` 到 `build/generated/kit-bundle`）、`clientModuleFunction`、`browserBundle(-Files)`，
新增 `ClientModule.kt`（`onRouteDidUpdate` 动态插 `<baseUrl>ktjs-client/index.mjs`），`Config.kt` 补
`clientModules`（含 `d3-transition`）/ `themes`（`@docusaurus/theme-mermaid`）/ `markdown.mermaid` /
`themeConfig.mermaid`，`gradle.properties` 补 `ktjs-docusaurus-kit.version=0.1.0`
83 首页改 kit 版（`<ktjs-hero>` + `<ktjs-action>` + `<ktjs-features>` + `<ktjs-next-steps>`，中英各一份），
`guide/platforms`（中英）加一张 mermaid 流程图，让 mermaid / d3-transition 这两项配置有真实用途

验证：`:demo:compileKotlinJs`、`:demo:jsBrowserDistribution` 通过；`:demo:e2eTest` 6 个用例、18 项断言全绿
（`h1.title`、`tr.row`、`.detail .member`、`.output`、`img.thumb`、`.lang-switch`、`.page` 与新页面对得上，
`fun greet(` / `val greeting` / `Hello, Ada!` / `proxy(greet) -> world` / `name=person` / `config.json`
逐项命中）；`./gradlew build` 通过（含 detekt）。
83 的验证：`:docs:docusaurusBuild` 两种 locale 均生成；`pagesSite` 产出 `/`、`/zh-Hans/`、`/demo/`、
`/api/` 与 `/ktjs-client/`（zh-Hans 下同样有）。Playwright 实测组装后的站点：`<ktjs-hero>` /
`<ktjs-features>` / `<ktjs-next-steps>` 都在 hydration 后升级出 shadow root，`guide/platforms` 的 mermaid
在浏览器里渲染成 SVG（SSG HTML 里为空，是 Docusaurus mermaid 的正常行为），两种语言均无 console error；
demo 首屏、选中 `Person` 后的成员面板、以及切换语言后就地更新都截图核对过。
79 的实现注记：`document.adoptedStyleSheets` 的类型是 `ReadonlyArray<CSSStyleSheet>`，Kotlin 侧没有
`push`，只能整体赋值 `arrayOf(*document.adoptedStyleSheets, PAGE_SHEET)`；`Color` / `FontFamily` 都没有
字符串工厂，调色板改用 `rgb(...)`，字体只能取 `FontFamily.systemUi` / `FontFamily.monospace` 这类常量，
原 CSS 里的自定义字体栈因此丢失（用系统字体替代，视觉上等价）。

## 明确不做

| 名称 | 排除理由 |
|---|---|
| 37 多轮（multi-round）处理 | 处理器只处理源码：生成的元数据/代理/注册表里没有任何需要再被注解处理的东西，`deferred` 无实际用途。若将来引入“生成的类也要参与反射”的能力再纳入。 |

## 备注

- 清单之外的后续工作（`androidTarget` 发布、注解实例化、代理属性转发、交叉 native 发布矩阵、插件自身的
  发布 marker 验证）记在 [technical-decisions.md](technical-decisions.md) 的「v0.2 候选」一节。
- 发布流程（`release.yml`）只在 `workflow_dispatch` 手动触发时执行，本地不冒用签名密钥，因此
  “配置已适配”不等于“已验证上传”，验证归属 hub 仓库。
- 交叉 native 目标（iOS 等）不在本机验证范围：Apple 目标只能在 macOS 宿主上构建与运行。
