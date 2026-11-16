# TODO

## kmp-reflect v0.1：编译期反射生成

用 KSP 在编译期把 KMP 类的元数据生成为 Kotlin 代码（内联，无运行时扫描、无资源加载），使被注解或实现
`KmpReflectable` 的类获得接近 JVM 反射的编程体验，命名统一带 `Kmp` 前缀以和 JVM 反射区分。

- 总计：66
- 已完成：65
- 明确不做：1
- 待完成：0

详细清单：[docs/development/tasks.md](docs/development/tasks.md)
技术决策：[docs/development/technical-decisions.md](docs/development/technical-decisions.md)
工程说明：[AGENTS.md](AGENTS.md)

### 当前待办

v0.1 的 66 项已全部收敛（65 项完成、1 项明确不做）。剩下的都是 v0.2 候选，见
[technical-decisions.md](docs/development/technical-decisions.md) 末节：

- [ ] Gradle 插件：把资源目录声明为 KSP 增量输入，按目标自动配置插件与选项
- [ ] 代理支持具体属性（调用方提供存储）
- [ ] 带调用方 `CoroutineScope` 的 `suspend` 调用
- [ ] 同模块内父类型的递归反射

### 注意事项

- 源仓库是 `ktjs-wrapper-template` 的克隆，npm wrapper 相关产物已全部移除；版本改用
  `gradle.properties` 的 `kmp-reflect.version`，工作树加 `-SNAPSHOT`。
- 反射元数据一律编译期内联成 Kotlin 代码，不做运行时类加载、不做资源文件读取。
- 公开 API 一律带 `Kmp` 前缀，包名 `io.github.shijianjs.kmp.reflect`。
- Kotlin/Native 与 Wasm 不在 v0.1 范围（KSP 生成物接不进那些 compilation），文档里已写明。
- 本地依赖可走国内镜像，仓库与 CI 只用官方源。
- 提交信息用中文简述；每个阶段完成后提交一次。
