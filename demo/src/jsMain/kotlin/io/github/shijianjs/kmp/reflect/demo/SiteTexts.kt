package io.github.shijianjs.kmp.reflect.demo

import io.github.shijianjs.ktjs.demo.i18n.Langs
import io.github.shijianjs.ktjs.demo.i18n.SiteTexts
import io.github.shijianjs.ktjs.demo.i18n.Translations
import io.github.shijianjs.ktjs.demo.i18n.texts

/**
 * Every string the layout and the navigation show, in English.
 *
 * The page content has its own translations (`page/lang/Translation.kt`); this object is about the
 * frame — the site name, the menu buttons, and the title each route shows in the sidebar and the
 * page header.
 */
private val englishSite: SiteTexts = SiteTexts(
    siteTitle = "kmp-reflect",
    langLabel = "English",
    languageLabel = "Language",
    themeLabel = "Theme",
    docsLabel = "Docs",
    siteDocsLabel = "Documentation",
    apiLabel = "API reference",
    sourceLabel = "GitHub",
    lightLabel = "Light",
    darkLabel = "Dark",
    systemLabel = "Follow browser",
    groupTitles = mapOf(
        "reflection" to "Reflection",
        "compiler" to "Compiler output",
    ),
    pageTitles = mapOf(
        "classes" to "Reflected classes",
        "invocation" to "Constructing and invoking",
        "proxy" to "Dynamic proxy",
        "resources" to "Inlined resources",
        "annotations" to "Annotation scan",
        "enums" to "Enum entries",
        "nested" to "Nested classes",
        "limits" to "Limits you can see here",
    ),
)

private val chineseSite: SiteTexts = SiteTexts(
    siteTitle = "kmp-reflect",
    langLabel = "中文",
    languageLabel = "语言",
    themeLabel = "外观",
    docsLabel = "文档",
    siteDocsLabel = "使用文档",
    apiLabel = "API 文档",
    sourceLabel = "GitHub 仓库",
    lightLabel = "亮色",
    darkLabel = "暗色",
    systemLabel = "跟随浏览器",
    groupTitles = mapOf(
        "reflection" to "反射能力",
        "compiler" to "编译期产物",
    ),
    pageTitles = mapOf(
        "classes" to "已反射的类",
        "invocation" to "构造与调用",
        "proxy" to "动态代理",
        "resources" to "内联资源",
        "annotations" to "注解扫描",
        "enums" to "枚举项",
        "nested" to "嵌套类",
        "limits" to "这里能看到的限制",
    ),
)

/** The layout's strings, one entry per registered language. */
val siteTexts: Translations<SiteTexts> = texts(
    Langs.EN to englishSite,
    Langs.ZH_HANS to chineseSite,
)
