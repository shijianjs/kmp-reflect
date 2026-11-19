package io.github.shijianjs.kmp.reflect.demo.page.lang

import io.github.shijianjs.ktjs.demo.i18n.Lang
import io.github.shijianjs.ktjs.demo.i18n.Langs

/**
 * Every string the demo's sections show, plus the heading each page is titled with.
 *
 * One instance per language; every section copies the strings it is responsible for, and switching
 * languages is a walk over the pages, not a re-render. Data that a section derives from metadata
 * (a member signature, a resource path) never comes from here.
 *
 * The shell's own strings (site title, menu labels) live in `SiteTexts.kt` — this object is about
 * content, that one is about the frame.
 */
internal class Translation(
    val classesHeading: String,
    val classesHint: String,
    val columnName: String,
    val columnKind: String,
    val columnMembers: String,
    val columnAnnotations: String,
    val detailHeading: String,
    val detailEmpty: String,
    val noAnnotation: String,
    val invocationHeading: String,
    val invocationHint: String,
    val constructed: String,
    val called: String,
    val propertyValue: String,
    val proxyHeading: String,
    val proxyHint: String,
    val proxyResult: String,
    val resourcesHeading: String,
    val resourcesHint: String,
    val resourceBytes: String,
    val annotationHeading: String,
    val annotationHint: String,
    val limitationsHeading: String,
    val limitations: List<String>,
    val invokable: String,
    val notInvokable: String,
    val enumHeading: String,
    val enumHint: String,
    val nestedHeading: String,
    val nestedHint: String,
    val membersSummary: (constructors: Int, functions: Int, properties: Int) -> String,
)

internal val english: Translation = Translation(
    classesHeading = "Reflected classes",
    classesHint = "Metadata generated for the classes this module opted in. Click a row for its members.",
    columnName = "class",
    columnKind = "kind",
    columnMembers = "members",
    columnAnnotations = "annotations",
    detailHeading = "Members",
    detailEmpty = "Pick a class above.",
    noAnnotation = "(none)",
    invocationHeading = "Constructing and invoking",
    invocationHint = "The constructor and functions below are called through metadata, not through direct Kotlin calls.",
    constructed = "constructed",
    called = "call result",
    propertyValue = "property",
    proxyHeading = "Dynamic proxy",
    proxyHint = "A generated proxy class forwards every call to a KmpInvocationHandler.",
    proxyResult = "proxy call result",
    resourcesHeading = "Inlined resources",
    resourcesHint = "Read from demo/data at compile time, stored as Base64 in generated Kotlin, decoded lazily.",
    resourceBytes = "bytes",
    annotationHeading = "Annotation scan",
    annotationHint = "Annotations are matched by qualified name, so scanning works without JVM reflection.",
    limitationsHeading = "Limits you can see here",
    limitations = listOf(
        "generic functions are recorded but not invokable - look for the badge in the member list.",
        "suspend functions are callable, but only through invokeSuspend from a coroutine.",
        "proxies need a function-only interface; abstract properties are not forwarded in v0.1.",
        "Kotlin/JS exposes no runtime class names, so reified lookups rely on the generated metadata.",
    ),
    invokable = "invokable",
    notInvokable = "metadata only",
    enumHeading = "Enum entries",
    enumHint = "Entries carry their ordinal and annotations, and the instance itself.",
    nestedHeading = "Nested classes",
    nestedHint = "A nested class is reflected separately, and its outer class lists it.",
    membersSummary = { constructors, functions, properties -> "$constructors ctor / $functions fun / $properties prop" },
)

internal val chinese: Translation = Translation(
    classesHeading = "已反射的类",
    classesHint = "本模块声明参与反射的类会生成元数据。点击一行查看成员。",
    columnName = "类",
    columnKind = "种类",
    columnMembers = "成员",
    columnAnnotations = "注解",
    detailHeading = "成员",
    detailEmpty = "请先在上面选择一个类。",
    noAnnotation = "（无）",
    invocationHeading = "构造与调用",
    invocationHint = "下面的构造与函数调用都经由元数据完成，不是直接的 Kotlin 调用。",
    constructed = "构造结果",
    called = "调用结果",
    propertyValue = "属性",
    proxyHeading = "动态代理",
    proxyHint = "编译期生成的代理类把所有调用转发给 KmpInvocationHandler。",
    proxyResult = "代理调用结果",
    resourcesHeading = "内联资源",
    resourcesHint = "编译期读取 demo/data，以 Base64 写入生成的 Kotlin，运行时惰性解码。",
    resourceBytes = "字节",
    annotationHeading = "注解扫描",
    annotationHint = "注解按全限定名匹配，因此扫描不需要 JVM 反射。",
    limitationsHeading = "这里能看到的限制",
    limitations = listOf(
        "泛型函数会被记录，但不可调用——看成员列表里的标记。",
        "suspend 函数可调用，但只能从协程里通过 invokeSuspend 调。",
        "代理要求接口只含函数；v0.1 不转发抽象属性。",
        "Kotlin/JS 不提供运行时类名，reified 查找依赖生成的元数据。",
    ),
    invokable = "可调用",
    notInvokable = "仅元数据",
    enumHeading = "枚举项",
    enumHint = "枚举项带有序号、注解以及实例本身。",
    nestedHeading = "嵌套类",
    nestedHint = "嵌套类单独反射，外层类会列出它。",
    membersSummary = { constructors, functions, properties -> "$constructors 构造 / $functions 函数 / $properties 属性" },
)

/** The content strings for a shell language. */
internal fun translationOf(lang: Lang): Translation = if (lang == Langs.ZH_HANS) chinese else english
