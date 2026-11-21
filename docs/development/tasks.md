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
| 总计 | 91 |
| 已完成 | 90 |
| 明确不做 | 1 |
| 待完成 | 0 |

以上是 v0.1 的口径。2026-11-22 追加的 v0.2 审查修正批次（S 节）单独统计：

| 状态 | 数量 |
|---|---:|
| 总计 | 9 |
| 已完成 | 3 |
| 明确不做 | 1 |
| 待完成 | 5 |

核对日期：2026-11-22。

已完成事项不再逐条列出；签名、验证依据见下方各节末尾的「验证」注记。
共有三批：A–I 是 v0.1 的库本体与站点，J 是 2026-11-16 追加的平台扩展（KSP 2 已支持 KMP 的 Native 与
Wasm，原先「只做 JVM + JS」的边界因此作废），其中 75–78 是同一轮之后的逐次推进（suspend、三宿主 CI、
父类型链接、Gradle 插件）；K 是 demo 页面重构与文档站配置还原；L–R 是页面口径的逐次收敛（样式改静态
sheet + class、标准 Web Component 与分层包、外壳容器化、class 常量就近、`PageBlock` 契约、要求沉淀进 skill
与模板、模板 demo 按同一口径重写）。

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

### L. 样式规范改口径并回填 demo（2，2026-11-16 追加）

84 `dev-agent-skills` 的 `ui-code-style` 由「优先内联」改成「静态 adopted sheet + class 为主、内联只给运行时
值」，并写明这是为性能妥协：`SKILL.md` 的 description、主线第 3 条、判定表第 2 行、第三节整节（三层表 +
一对多论证 + 9 条纪律）、第四节「内联 vs sheet：边界与例外」（4 条硬约束，禁 `cssText`）、第七节对照表
拆「静态规则（默认）/ 动态值（例外）」、第八节清单 4 条、第九节新增两条禁止；`references/styling.md` 第 1/2/
3/4/6 节重写（第 2 节补「无 shadow root 的页面」`installPageStyle(vararg)` 示例、第 4 节改成 7 步迁移）；
`references/elements.md` 样式行拆静态/动态两行；`README.md` 技能表同步
85 kmp-reflect demo 应用新口径：`Theme.kt` 重写为「12 个调色板 + 30 个 class 常量 + 只剩 reset 与 `.page`
的 `PAGE_SHEET` + `installPageStyle(vararg CSSStyleSheet)`」，静态规则按区块落到各自的 `HEADER_SHEET` /
`SECTION_SHEET` / `OUTPUT_SHEET` / `BROWSER_SHEET` / `RESOURCES_SHEET`，节点只带 class；两态改 class 切换
（`.row.is-selected`、`.badge-ok` / `.badge-muted`）；`ProxySection` 的 `<p class="output">` 改用
`OutputList` 统一成 `ul.output`；`DemoPage.mount()` 一次性 adopt 五份 sheet。`.agents/skills/ui-code-style`
副本与源仓库同步，`AGENTS.md` 的「演示与文档」三条约定按新口径重写

### M. demo 改成标准 Web Component + 层级包（1，2026-11-17 追加）

86 85 的全局 adopt 只是「把样式表挂到 document」，作用域其实还是整页，和没 adopt 一样；改成标准自定义元素
组织：每个组件一个 `CustomElementAdapter` 子类 + 自己的 shadow root + 自己的 sheet（`attachStyledShadow`
只装进自己的树，`document.adoptedStyleSheets` 保持 0 份），`theme/Reset.kt` 的 `RESET` 是唯一共享的那份；
十个标签 `<kmp-demo-page>` / `<kmp-demo-header>` / `<kmp-classes>` / `<kmp-invocation>` / `<kmp-proxy>` /
`<kmp-resources>` / `<kmp-annotations>` / `<kmp-enums>` / `<kmp-nested>` / `<kmp-limitations>`，`index.html`
直接用标记实例化页面，`Main.kt` 按「子元素在前、页面最后」define。目录按归属分层：`page/`（`DemoPage` +
`theme/` 调色板与 class 常量 + `lang/` 文案）、`page/header/`、`page/section/`（`SectionShell` +
`OutputList` 这两个一对多里的「一」）、`page/section/sections/`（八个区块即「多」）；`SectionShell` 仍是被
组合的部件而不是基类。同时给 JS 目标补 `compilerOptions { target = "es2015" }`——ES5 下 Kotlin 发射
`HTMLElement.call(this)`，DOM 构造器只能 `new`，浏览器抛 "Please use the 'new' operator"、元素不升级

验证：`:demo:compileKotlinJs`、`:demo:jsBrowserDistribution` 通过；`:demo:e2eTest` 6 个用例、18 项断言全绿
（`h1.title`、`tr.row`、`.detail .member`、`.output`、`img.thumb`、`.lang-switch`、`.page` 与新页面对得上，
`fun greet(` / `val greeting` / `Hello, Ada!` / `proxy(greet) -> world` / `name=person` / `config.json`
逐项命中）；`./gradlew build` 通过（含 detekt）。
### N. 区块外壳做成真正的容器 + 文案类改叫 Translation（1，2026-11-17 追加）

87 86 的 `SectionShell` 只是把 `h2` / `hint` / 空 `div` 三个节点交给区块自己塞进 shadow root，区块的
盒子其实还是区块宿主本身——看着不像「Card 装着内容」。改成 `<kmp-section>` 组件：自己的 shadow root 里
是 `h2` + `p` + `<slot>`，`:host` 承担 36px 区块间距；区块把正文 append 到 `<kmp-section>` 上、经 slot 显示，
正文因此仍在区块自己的 shadow 树里、由区块的 sheet 上色（没有跨树失效）。区块不再 adopt 共享的
`SECTION_SHEET`（那份跟着外壳走，改名 `SHELL_SHEET`，只有外壳自己 adopt），只留 `RESET` + 自己的 sheet。
同时把 `Copy` 这个容易和「复制」混淆的名字改成 `Translation`：`lang/Translation.kt`、`english` / `chinese` /
`translationOf(lang)`、各处 `applyTranslation(translation)`，外壳的 `setCopy` 改成 `setText`

验证：`:demo:compileKotlinJs`、`:demo:jsBrowserDistribution`、`:demo:e2eTest`（6 用例 / 18 断言）、
`:demo:detekt`（0 findings）、`./gradlew build` 均通过。产物实测：每个区块的 shadow root 里都有一个
`<kmp-section>`，外壳 shadow 是 `h2` + `p` + `slot` 三段、adopt 2 份 sheet；`<table class="class-table">`
位于外壳的 light DOM（`slot` 之前），`td` padding `9px 12px`、`th` 字重 600、`h2` 19px、外壳 margin-top 36px、
9 行表格、7 个 `.output`、`document.adoptedStyleSheets` 仍为 0、`[style]` 只有 `BODY`，中英截图正常、无报错。
上线后：Pages run `43923255387`、CI run `43923293242` 均成功；线上
`https://shijianjs.github.io/kmp-reflect/demo/` 复测结果与本地一致（八个区块各有一个 `<kmp-section>`、
外壳 2 份 sheet、正文在外壳 light DOM、`td` padding / `h2` 19px / 外壳 36px 间距 / 9 行 / 7 个 `.output`、
无 console error）。
### O. 就近原则：单文件 class 常量下放 + 页面挂载/换语言改列表循环（1，2026-11-17 追加）

88 87 之后 `theme/Classes.kt` 仍把 25 条 class 常量挤在一处，但其中 21 条只在一个文件里出现，等于把
「同一文件的两侧」（规则与节点）拆到了公共文件。按就近原则下放：`TITLE_CLASS` / `SUBTITLE_CLASS` /
`LANG_CLASS` 进 `HeaderBar.kt`，`SECTION_HEADING_CLASS` / `SECTION_HINT_CLASS` 进 `SectionShell.kt`，
`OUTPUT_CLASS` 进 `OutputList.kt`，表格与成员面板那 12 条进 `ClassBrowser.kt`，`RESOURCE_CLASS` /
`PREVIEW_CLASS` / `THUMB_CLASS` 进 `ResourcesSection.kt`，一律写成文件私有的 `private val`；`Classes.kt`
只留真正跨文件的 `HINT_CLASS`、`DETAIL_HINT_CLASS`、`OUTPUT_ITEM_CLASS`、`CODE_CLASS`。同时 `DemoPage`
的 `init` 与 `applyTranslation` 不再逐行重复：改成一个 `blocks: List<Block>`（元素 + `::applyTranslation`
方法引用），挂载与换语言都走循环，新增区块只加一行。验证：`:demo:compileKotlinJs`、`:demo:jsBrowserDistribution`、
`:demo:e2eTest`（6 用例 / 18 断言）、`:demo:detekt`（0 findings）通过；产物实测与第 87 项一致
（外壳 36px 间距、`td` padding `9px 12px`、`h2` 19px、9 行、7 个 `.output`、`document.adoptedStyleSheets` 0、
`[style]` 只有 `BODY`、无 console error），说明常量下放与列表循环没有改变外观或行为。上线后：
Pages run `44017208903`、CI run `44017218472` 成功（push 前先 rebase 了 Dependabot 的
`actions/cache` 5→6 提交），线上 `/demo/` 复测同一组数值全部命中，无 console error
### P. `applyTranslation` 抽成 `PageBlock` 契约（1，2026-11-17 追加）

第 88 项把重复调用收成 `blocks: List<Block>`，但 `Block` 里仍是「元素 + `::applyTranslation`」的配对，
契约还落在页面这边、组件自己并不声明「我能被换语言」。改为接口：`page/DemoPage.kt` 里
`internal interface PageBlock` 声明 `val node: HTMLElement get() = this as HTMLElement`（实现者都是自定义
元素，默认实现里一次 cast，九个组件零样板）与 `fun applyTranslation(translation: Translation)`；header 与
八个区块改成 `: CustomElementAdapter(), PageBlock` 并给该方法加 `override`，`DemoPage` 只持有一个
`blocks: List<PageBlock>`，`init` 与 `applyTranslation` 各自一句 `forEach`（`block.node` / `block.applyTranslation`）。
`Block` 类、方法引用与两处 lambda 配对一并删掉。验证：`:demo:compileKotlinJs`、`:demo:jsBrowserDistribution`、
`:demo:detekt`（0 findings）、`:demo:e2eTest`（6 用例 / 18 断言）通过；产物实测与第 88 项一致（外壳 36px
间距、`td` padding `9px 12px`、`h2` 19px、9 行、7 个 `.output`、`document.adoptedStyleSheets` 0、`[style]`
只有 `BODY`、九个组件各自 2–3 份 sheet、无 console error）。上线后：Pages run `44019108963`、CI run
`44019110194` 成功；线上 `/demo/` 复测同一组数值全部命中，无 console error
### Q. demo / docs 要求沉淀进 skill 与模板（1，2026-11-17 追加）

第 84–89 项的口径只落在本项目，而同类的 demo + 文档站要求（标准 Web Component、目录分层、外壳容器化、
class 常量就近、换语言契约、es2015、文档站配置别砍、双语与 Pages 触发）在别的 wrapper 项目里还会被重提。
把这些收成清单并回灌：`ui-code-style` 新增第九节「页面组合：外壳是容器，页面用契约串起来」，第八节清单补
6 条（每个组件自己的 shadow 与 `document.adoptedStyleSheets` 0 份、外壳与使用者分两级、单文件 class 常量
就地 `private val`、`blocks` + 契约、深色底控件的常态色、浏览器实测），第十节补 2 条禁止（把组件 sheet 挂到
document、外壳做基类或与使用者平铺），description 与 `README.md` 技能表同步；`references/packages.md` 增
「`section/` 与 `section/sections/`」真实对照；`references/elements.md` 增第 7 节「组合式外壳与页面契约」
（`<kmp-section>` 的 slot 结构、`PageBlock` 写法、三种反例排序）。`ktjs-wrapper-template/AGENTS.md` 把
「样式默认私有」改写成「静态进组件 sheet + class、内联只留给运行时值」（含 class 常量就近与禁止透传包装），
新增「页面组合：槽位外壳 + 契约接口」与「目录按层级：一对多分两级」，把「默认 Shadow DOM」改成「作用域
不外溢」（补 `document.adoptedStyleSheets` 0 份、es2015 与产物 grep 自查、深色底常态色），并在「演示页面与
GitHub Pages」下加 demo / docs 对齐清单（含「模板 docs 配置调优过，不要先砍再用」、双语与 Pages 触发）。
kmp-reflect 的 skill 副本（`.agents/skills/ui-code-style/`）同步覆盖
83 的验证：`:docs:docusaurusBuild` 两种 locale 均生成；`pagesSite` 产出 `/`、`/zh-Hans/`、`/demo/`、
`/api/` 与 `/ktjs-client/`（zh-Hans 下同样有）。Playwright 实测组装后的站点：`<ktjs-hero>` /
`<ktjs-features>` / `<ktjs-next-steps>` 都在 hydration 后升级出 shadow root，`guide/platforms` 的 mermaid
在浏览器里渲染成 SVG（SSG HTML 里为空，是 Docusaurus mermaid 的正常行为），两种语言均无 console error；
demo 首屏、选中 `Person` 后的成员面板、以及切换语言后就地更新都截图核对过。
79 的实现注记：`document.adoptedStyleSheets` 的类型是 `ReadonlyArray<CSSStyleSheet>`，Kotlin 侧没有
`push`，只能整体赋值 `arrayOf(*document.adoptedStyleSheets, PAGE_SHEET)`；`Color` / `FontFamily` 都没有
字符串工厂，调色板改用 `rgb(...)`，字体只能取 `FontFamily.systemUi` / `FontFamily.monospace` 这类常量，
原 CSS 里的自定义字体栈因此丢失（用系统字体替代，视觉上等价）。
同类修正（同一轮后补）：所有 class 标记收进 `Theme.kt` 的 `X_CLASS: ClassName` 常量、调用点直接
`classList.add(X_CLASS)`，删掉把字符串透传一层的 `addClass(String)`（`classList.add` 只吃 `ClassName`，
包一层等于放弃检查）；语言切换按钮原本用 UA 的 `buttontext` 黑色，深色底上看不见，改为在 `PAGE_SHEET`
里给 `color` / `backgroundColor` / `borderColor` 基础值（连同 hover 一起留在 sheet，避免内联压过
`:hover`）。实测按钮常态 `rgb(154,163,175)`、hover 边框与文字都变 `rgb(110,168,254)`。
85 的验证：`:demo:compileKotlinJs`、`:demo:detekt`（0 findings）、`:demo:jsBrowserDistribution` 通过；`:demo:e2eTest` 6 个用例、18 项断言全绿（选择器契约与文案全部保留，`ProxySection` 从 `p.output` 换成
`ul.output` 后 `.output` 断言仍然命中）。Playwright 实测生产产物：`document.querySelectorAll("[style]")`
只剩 1 个节点（`<body>` 的首屏底色，单节点、不是 N 份重复），`table.outerHTML` 已是
`<table class="class-table">` + `<th class="cell cell-head">`；`document.adoptedStyleSheets` 共 6 份
（PAGE + 五个区块），规则数与源码逐条对应；`.lang-switch` 常态 `rgb(154,163,175)`、hover 文字与边框
`rgb(110,168,254)`；选中行背景 `rgba(110,168,254,0.12)`、徽标 `rgba(74,222,128,0.12)`；`td` padding
`9px 12px`、`th` 字重 `600`、`h2` 19px、`.section-hint` 的 `margin-bottom` 14px（`.hint` 与
`.section-hint` 同特异性靠后声明覆盖的顺序假设成立）；中英首屏与选中态截图核对，无 console error、
无失败请求。上线后 CI run `43901635441`（四个 job）与 Pages run `43901620856` 全绿；线上
`/demo/` 实测 `[style]` 只有 `BODY`、6 份 adopted sheet、`<table class="class-table">` /
`<th class="cell cell-head">`、9 行、按钮 `rgb(154,163,175)`、点行后 `.detail .member` 正常、无 console error。
86 的验证：修正前的产物里 `<kmp-demo-page>` 只被 define、没有 shadow root，浏览器抛`Failed to construct 'HTMLElement': Please use the 'new' operator`；补 `target = "es2015"` 后同一探针显示
`created.shadow = true`、`hostIsUpgraded = true`。生产产物实测：`document.adoptedStyleSheets.length = 0`，
每个组件各自的 shadow root 分别 adopt 2/3/4 份 sheet（`kmp-demo-header` 2 份、`kmp-classes` 3 份共 14 条
规则、`kmp-resources` 4 份），全页仍只有 `BODY` 一个 `style` 属性；`td` padding `9px 12px`、`th` 字重 600、
`h2` 19px、`.section-hint` margin 14px、按钮常态 `rgb(154,163,175)` / hover `rgb(110,168,254)`、选中行
`rgba(110,168,254,0.12)` 与 `.detail .member` 文本均与改写前一致，9 行表格、7 个 `.output` 列表都在。
`:demo:e2eTest` 6 个用例、18 项断言全绿（Playwright 的 CSS 选择器穿透 open shadow root，原有选择器契约
不需要改）；`:demo:detekt` 0 findings；`./gradlew build` 通过。中英首屏与选中态截图核对，无 console error。
上线后：Pages run `43917615626`（push `074c02c`）与 CI run `43917633208` 均成功；线上
`https://shijianjs.github.io/kmp-reflect/demo/` 复测 `document.adoptedStyleSheets` 为 0、九个组件的 shadow
root 各自 2–4 份 sheet、`[style]` 只有 `BODY`、9 行表格、`h1.title` 正常、无 console error。

### R. 模板 demo 改成本项目风格（1，2026-11-17 追加）

`ktjs-wrapper-template` 的 demo 原先只把 `<vaadin-button>` 挂到 `document.body`（来自 ktjs-vaadin 包装项目
的样例，太特殊、不算数）。按本项目这一轮定下的口径重写：页面 `<ktjs-demo-page>` 自己定义元素，header
`<ktjs-demo-header>`、外壳 `<ktjs-section>`、区块 `<ktjs-button-demo>` / `<ktjs-api>` / `<ktjs-conventions>`
各自一个 `CustomElementAdapter` 子类、一份 `attachStyledShadow(RESET, 自己的 sheet)`；目录分层为
`page/`、`page/header/`、`page/lang/`、`page/theme/`、`page/section/` + `page/section/sections/`；节点与样式
走 `ktjs-dom-kit`（`TagName<T>.createElement`、`cssSheet`），另加 `gradle.properties` 的
`ktjs-dom-kit.version` 与 `demo/build.gradle.kts` 的 `jsMain` 依赖；demo 自己定义元素，因此补
`compilerOptions { target = "es2015" }`。上游按钮只被包在区块里，样式作用在它的宿主上，不伸进它的 shadow。
中英切换沿用 `lang/Translation.kt` + `PageBlock.applyTranslation` 契约。`AGENTS.md` 的 demo 清单同步补上
「自己定义元素」「用 ktjs-dom-kit」「换语言走契约」，es2015 那条从「demo 没开」改成「已开、不能删」；
`ui-code-style` 第九节补「被封装的上游元素要装进自己的组件」，模板仓库提交 `071b517`。

验证：`:demo:compileKotlinJs`、`kotlinUpgradePackageLock`（package-lock 随 npm 依赖刷新）、
`:demo:jsBrowserDistribution`、`:demo:detekt`（0 findings）、`./gradlew build` 均通过；`:demo:e2eTest` 6 个
用例全绿（标题与 `h1.title`、`img.logo`、`.page` 的 display、页面自带 shadow root、`document.adoptedStyleSheets`
为 0、`[style]` 节点只有 `BODY`、每个 `ktjs-section` 都有 shadow root、按钮点击计数、`disabled` 属性透传、
语言切换、无 console error / 无失败请求）。生产 bundle 自查 `grep 'HTMLElement.call'` 为 0。Playwright 实测
本地产物：`document.adoptedStyleSheets` 0 份、页面 shadow 2 份 sheet、两个 `<vaadin-button>` 宿主
background `rgb(37, 99, 235)` / color `rgb(255, 255, 255)` / padding `9px 18px` / radius `8px`、状态行
`Clicks: 0`；切语言后区块标题变「1. 被封装出来的元素」、状态行变「点击次数: 0」、按钮文案变 EN，两种语言
截图核对无 console error。上线后 CI run `44031362095` 与 Pages run `44031368233` 均成功；线上
`https://shijianjs.github.io/ktjs-wrapper-template/demo/` 复测同一组数值（0 份 document sheet、
`[style]` 只有 `BODY`、页面 shadow 2 份、两个 `<vaadin-button>` background `rgb(37, 99, 235)`、状态行
`Clicks: 0`），切语言后就地变成「1. 被封装出来的元素 / 点击次数: 0 / EN」，无 console error、无失败请求。

### S. v0.2 审查修正（9，2026-11-22 追加）

来源：2026-11-22 对项目代码的一轮审查（另一 AI 的评价 + 逐条核对源码）。核对结论：确认真问题 6 项，
已覆盖 1 项（`KmpTypeRef` 早已保留 `arguments` / `variance` / star / type parameter，运行时只用于
`isInstance` 且文档写明擦除），误判 1 项（`by lazy` 的延迟被注册表的全量求值抵消，见第 95 项）。

- 92 **`private` / `protected` 成员与构造器不进元数据**：`SymbolConverter.memberVisible` 同时排除两者
  （原实现只排 `private`，而 `includeNonPublic` 默认 `true` 会把 protected 成员写进生成代码，
  顶层文件里调用 protected 成员会直接编译失败）；属性可读/可写改按 getter / setter 自身可见性判定；
  protected 嵌套类同样跳过。顺带修掉泛型类的接收者写法：新增 `ClassModel.ownerType()`，生成
  `(receiver as Box<*>)`（原来写成 `(receiver as Box)`，泛型类一被反射就编译失败，此前没有泛型类用例）。
- 93 **成员可见性进元数据**：`KmpFunction` / `KmpProperty` / `KmpConstructor` 增加 `visibility`；
  生成代码只在非 `PUBLIC` 时写该参数。
- 94 **默认参数：尾部省略调用**：`MetadataWriter.argumentDispatch` 按「尾部连续默认值」算出允许的实参个数
  区间，生成一个 `when (args.size)`，每个档位一条按位置调用（`Person(args[0])`、
  `Person(args[0], args[1])`、全参）；没有默认值时仍生成原来的单条 lambda，不改变既有生成物形态。
  运行时把 `KmpFunction` / `KmpConstructor` 的实参校验从「个数必须相等」改成「落在允许区间」，
  错误信息写成 `1..3 arguments`。中间默认不缩短区间（`Formatter` 仍要求三个），命名参数式跳过不建模。
- 95 注册表按需解析，`by lazy` 真正生效（待完成）。
- 96 `KmpClasses.of(KClass)` 索引（待完成）。
- 97 层级语义：`superclasses` → `reflectedSupertypes` + `isSubclassOf` / `allFunctions`（待完成）。
- 98 文档与文案对齐（待完成）。
- 99 边界覆盖用例：`Scoped` / `Box` / `OuterWork`(+`Inner`) / `Meters` / `Speaker` / `Formatter` /
  `Specimen`（待完成）。
- 100 数据类合成成员的过滤选项（明确不做，理由见「明确不做」）。

验证（92/93）：`:demo:reflectionTest` 23 项全绿（新增 protected/private 缺席、internal 可见性、
vararg、泛型类只描述、`value class`、`inner`、接口默认实现、注解四类参数、中间默认不可省共 10 项）；
`:demo:compileKotlinJvm` 通过（KSP 生成物里 `Scoped` 只剩 1 个构造器、`internalThing` 带
`visibility = KmpVisibility.INTERNAL`、`counter` 可读不可写、`Box<*>` 接收者）。

验证（94）：`:demo:reflectionTest` 24 项全绿。生成物实测：`Person` 构造器 `when (args.size)` 为
1/2/3 三档、`greet` 为 0/1 两档、`copy` 为 0..3 四档；`constructor.new("Ada")` 得到 `age = 0`、
`greet()` 得到 `"Hello, Ada!"`、`copy("Grace")` 只改姓名，`new()`（低于下限）仍抛
`KmpReflectException`；`Formatter.format` 因中间默认仍只有全参一档，传两个参数照样抛错。

## 明确不做

| 名称 | 排除理由 |
|---|---|
| 37 多轮（multi-round）处理 | 处理器只处理源码：生成的元数据/代理/注册表里没有任何需要再被注解处理的东西，`deferred` 无实际用途。若将来引入“生成的类也要参与反射”的能力再纳入。 |
| 100 数据类合成成员（`copy` / `componentN`）的过滤选项 | KSP 不暴露 “synthetic” 标志，按名字推断会在普通类上误判；保留现状（它们照常参与元数据，`copy` 因此也能带默认参数调用），不对称性记入 limitations。 |

## 备注

- 清单之外的后续工作（`androidTarget` 发布、注解实例化、代理属性转发、交叉 native 发布矩阵、插件自身的
  发布 marker 验证）记在 [technical-decisions.md](technical-decisions.md) 的「v0.2 候选」一节。
- 发布流程（`release.yml`）只在 `workflow_dispatch` 手动触发时执行，本地不冒用签名密钥，因此
  “配置已适配”不等于“已验证上传”，验证归属 hub 仓库。
- 交叉 native 目标（iOS 等）不在本机验证范围：Apple 目标只能在 macOS 宿主上构建与运行。
