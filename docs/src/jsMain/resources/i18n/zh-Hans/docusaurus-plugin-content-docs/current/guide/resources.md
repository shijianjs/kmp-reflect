---
title: 内联资源
description: 文件变成生成 Kotlin 里的 Base64 字面量，并带上路径与 MIME。
---

# 内联资源

运行时读文件意味着文件系统、打包约定或网络 —— 这些都不跨平台。因此资源在**编译期**被读取，写进生成的
Kotlin 代码。

## 配置

```kotlin
ksp {
    // 一个或多个目录，用 ','、';' 或路径分隔符分隔。
    arg("kmp.reflect.resources", layout.projectDirectory.dir("data").asFile.absolutePath)
}
```

目录下的每个文件对应一个 `KmpResource`，键是相对该目录的路径。于是 `data/config.json` 的路径是
`config.json`，`data/icons/star.svg` 是 `icons/star.svg`。两个目录出现同一路径时，目录顺序靠前的胜出。

MIME 由扩展名推断 —— `application/json`、`image/svg+xml`、`text/markdown`、`font/woff2` 等，
未知扩展名回退到 `application/octet-stream`，所以非常规文件也能被描述。

## 生成形态

```kotlin title="generated"
internal val kmpResource0: KmpResource =
    KmpResource(path = "config.json", mimeType = "application/json", size = 155) { kmpResourceBytes0 }

private val kmpResourceBytes0: ByteArray by lazy { Base64.decode("ewogICJ0aXRsZSI6…") }
```

用 Base64 而不是字节数组字面量：它是最短、又能安全穿过所有目标源码编码的文本形式。解码是 `lazy`
的，因此从未打开的资源除了字符串常量之外没有成本；模块对象会把它们全部列进 registry：

```kotlin
KmpResources.all().map { "${it.path} — ${it.mimeType} (${it.size} bytes)" }
```

## 读取

```kotlin
KmpResources.byPath("config.json").text()          // 按 UTF-8 解码
KmpResources.byPath("icons/star.svg").bytes        // 原始字节
KmpResources.allIn("icons")                        // 该目录下所有资源
KmpResources.directories()                         // 去重后的目录，已排序
```

`text()` 只是便捷方法，不做内容判断：它按 UTF-8 解码，不看 MIME。

## 在上面继续搭

加载器、键值门面、素材清单 —— 都能用 `path + mime + bytes` 自己搭；本库刻意止步于此。`KmpResource`
是纯值，需要缓存或解码器的工具类可以自带状态。

## 增量构建

资源目录没有被声明为 KSP 输入，因此已经生成过元数据的构建可能发现不了新增或修改的文件。资源看起来
没更新时：

```bash
./gradlew --rerun-tasks :your-module:kspKotlinJvm
```

这是 v0.1 的已知限制，也是本项目的 Gradle 插件第一个要解决的问题（把目录注册成任务输入）。
