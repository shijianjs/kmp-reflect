package io.github.shijianjs.kmp.reflect.demo

import io.github.shijianjs.kmp.reflect.KmpClass
import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.KmpReflection
import io.github.shijianjs.kmp.reflect.demo.model.Greeter
import io.github.shijianjs.kmp.reflect.demo.model.Library
import io.github.shijianjs.kmp.reflect.demo.model.Person
import io.github.shijianjs.kmp.reflect.demo.model.Shape
import io.github.shijianjs.kmp.reflect.proxy.KmpInvocationHandler
import io.github.shijianjs.kmp.reflect.resource.KmpResource
import io.github.shijianjs.kmp.reflect.resource.KmpResources
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import js.reflect.unsafeCast
import web.dom.ElementId
import web.dom.Node
import web.dom.document
import web.html.HTMLImageElement
import web.html.HTMLElement

/**
 * The demo page.
 *
 * Everything is built through the typed DOM (`kotlin-browser`'s `web.*`), never by concatenating
 * markup, and every string comes from one of the two [Copy] instances so the language switch is a
 * plain re-render. The page only reads the generated metadata — nothing here inspects a real class.
 */
private enum class Lang { EN, ZH }

private class Copy(
    val langLabel: String,
    val switchLabel: String,
    val title: String,
    val subtitle: String,
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
)

private val english = Copy(
    langLabel = "EN",
    switchLabel = "中文",
    title = "kmp-reflect",
    subtitle = "JVM-like reflection for Kotlin Multiplatform, generated at compile time by KSP.",
    classesHeading = "1. Reflected classes",
    classesHint = "Metadata generated for the classes this module opted in. Click a row for its members.",
    columnName = "class",
    columnKind = "kind",
    columnMembers = "members",
    columnAnnotations = "annotations",
    detailHeading = "Members",
    detailEmpty = "Pick a class above.",
    noAnnotation = "(none)",
    invocationHeading = "2. Constructing and invoking",
    invocationHint = "The constructor and functions below are called through metadata, not through direct Kotlin calls.",
    constructed = "constructed",
    called = "call result",
    propertyValue = "property",
    proxyHeading = "3. Dynamic proxy",
    proxyHint = "A generated proxy class forwards every call to a KmpInvocationHandler.",
    proxyResult = "proxy call result",
    resourcesHeading = "4. Inlined resources",
    resourcesHint = "Read from demo/data at compile time, stored as Base64 in generated Kotlin, decoded lazily.",
    resourceBytes = "bytes",
    annotationHeading = "5. Annotation scan",
    annotationHint = "Annotations are matched by qualified name, so scanning works without JVM reflection.",
    limitationsHeading = "6. Limits you can see here",
    limitations = listOf(
        "generic functions are recorded but not invokable - look for the badge in the member list.",
        "suspend functions are callable, but only through invokeSuspend from a coroutine.",
        "proxies need a function-only interface; abstract properties are not forwarded in v0.1.",
        "Kotlin/JS exposes no runtime class names, so reified lookups rely on the generated metadata.",
    ),
    invokable = "invokable",
    notInvokable = "metadata only",
    enumHeading = "7. Enum entries",
    enumHint = "Entries carry their ordinal and annotations, and the instance itself.",
    nestedHeading = "8. Nested classes",
)

private val chinese = Copy(
    langLabel = "中文",
    switchLabel = "EN",
    title = "kmp-reflect",
    subtitle = "为 Kotlin Multiplatform 提供接近 JVM 的反射体验，元数据由 KSP 在编译期生成。",
    classesHeading = "1. 已反射的类",
    classesHint = "本模块声明参与反射的类会生成元数据。点击一行查看成员。",
    columnName = "类",
    columnKind = "种类",
    columnMembers = "成员",
    columnAnnotations = "注解",
    detailHeading = "成员",
    detailEmpty = "请先在上面选择一个类。",
    noAnnotation = "（无）",
    invocationHeading = "2. 构造与调用",
    invocationHint = "下面的构造与函数调用都经由元数据完成，不是直接的 Kotlin 调用。",
    constructed = "构造结果",
    called = "调用结果",
    propertyValue = "属性",
    proxyHeading = "3. 动态代理",
    proxyHint = "编译期生成的代理类把所有调用转发给 KmpInvocationHandler。",
    proxyResult = "代理调用结果",
    resourcesHeading = "4. 内联资源",
    resourcesHint = "编译期读取 demo/data，以 Base64 写入生成的 Kotlin，运行时惰性解码。",
    resourceBytes = "字节",
    annotationHeading = "5. 注解扫描",
    annotationHint = "注解按全限定名匹配，因此扫描不需要 JVM 反射。",
    limitationsHeading = "6. 这里能看到的限制",
    limitations = listOf(
        "泛型函数会被记录，但不可调用——看成员列表里的标记。",
        "suspend 函数可调用，但只能从协程里通过 invokeSuspend 调。",
        "代理要求接口只含函数；v0.1 不转发抽象属性。",
        "Kotlin/JS 不提供运行时类名，reified 查找依赖生成的元数据。",
    ),
    invokable = "可调用",
    notInvokable = "仅元数据",
    enumHeading = "7. 枚举项",
    enumHint = "枚举项带有序号、注解以及实例本身。",
    nestedHeading = "8. 嵌套类",
)

private var lang: Lang = Lang.EN
private var copy: Copy = english
private var selectedClass: String? = null

/** Entry point: register the generated module, then draw the page. */
public fun main() {
    KmpReflection.register(DemoReflection)
    render()
}

private fun render() {
    val root = document.getElementById(ElementId("demo")) ?: return
    root.textContent = ""
    copy = if (lang == Lang.EN) english else chinese
    root.appendChild(header())
    root.appendChild(classesSection())
    root.appendChild(invocationSection())
    root.appendChild(proxySection())
    root.appendChild(resourceSection())
    root.appendChild(annotationSection())
    root.appendChild(enumSection())
    root.appendChild(nestedSection())
    root.appendChild(limitationsSection())
}

private fun header(): HTMLElement {
    val bar = div("header")
    val titles = div("titles")
    titles.appendChild(text("h1", "title", copy.title))
    titles.appendChild(text("p", "subtitle", copy.subtitle))
    bar.appendChild(titles)
    bar.appendChild(
        button(copy.switchLabel, "lang-switch") {
            lang = if (lang == Lang.EN) Lang.ZH else Lang.EN
            render()
        },
    )
    return bar
}

private fun classesSection(): HTMLElement {
    val section = div("section")
    section.appendChild(text("h2", null, copy.classesHeading))
    section.appendChild(text("p", "hint", copy.classesHint))

    val table = text("table", null, "")
    val head = document.createElement("tr")
    listOf(copy.columnName, copy.columnKind, copy.columnMembers, copy.columnAnnotations).forEach { label ->
        head.appendChild(text("th", null, label))
    }
    table.appendChild(head)

    KmpClasses.all().forEach { kmpClass ->
        val row = document.createElement("tr")
        css(row, if (kmpClass.qualifiedName == selectedClass) "row selected" else "row")
        row.appendChild(text("td", "name", kmpClass.qualifiedName))
        row.appendChild(text("td", null, kmpClass.kind.name.lowercase()))
        row.appendChild(text("td", null, members(kmpClass)))
        row.appendChild(text("td", null, kmpClass.annotations.joinToString { it.type.simpleName }.ifEmpty { copy.noAnnotation }))
        row.onClick {
            selectedClass = if (selectedClass == kmpClass.qualifiedName) null else kmpClass.qualifiedName
            render()
        }
        table.appendChild(row)
    }
    section.appendChild(table)
    section.appendChild(detail())
    return section
}

private fun detail(): HTMLElement {
    val panel = div("detail")
    panel.appendChild(text("h3", null, copy.detailHeading))
    val name = selectedClass
    if (name == null) {
        panel.appendChild(text("p", "hint", copy.detailEmpty))
        return panel
    }
    val kmpClass = KmpClasses.byNameOrNull(name)
    if (kmpClass == null) {
        panel.appendChild(text("p", "hint", copy.detailEmpty))
        return panel
    }
    val list = text("ul", "members", "")
    kmpClass.functions.forEach { function ->
        list.appendChild(memberLine("fun ${function.name}(${function.parameters.joinToString { "${it.name}: ${it.type.simpleName}" }}): ${function.returnType.simpleName}", function.isInvokable))
    }
    kmpClass.properties.forEach { property ->
        list.appendChild(memberLine("${if (property.isMutable) "var" else "val"} ${property.name}: ${property.type.simpleName}", property.isReadable))
    }
    kmpClass.constructors.forEach { constructor ->
        list.appendChild(memberLine("constructor(${constructor.parameters.joinToString { "${it.name}: ${it.type.simpleName}" }})", constructor.isInvokable))
    }
    panel.appendChild(list)
    return panel
}

private fun memberLine(label: String, invokable: Boolean): HTMLElement {
    val item = text("li", "member", label)
    item.appendChild(text("span", if (invokable) "badge ok" else "badge muted", if (invokable) copy.invokable else copy.notInvokable))
    return item
}

private fun invocationSection(): HTMLElement {
    val section = div("section")
    section.appendChild(text("h2", null, copy.invocationHeading))
    section.appendChild(text("p", "hint", copy.invocationHint))

    val personClass: KmpClass = KmpClasses.byName("io.github.shijianjs.kmp.reflect.demo.model.Person")
    val constructor = personClass.constructor(3)
    val person = constructor?.new("Ada", 36, "hidden") as? Person
    val lines = text("ul", "output", "")
    if (person != null) {
        lines.appendChild(text("li", null, "${copy.constructed}: ${person.name} (${person.age})"))
        val greet = personClass.function("greet", 1)?.invoke(person, "Hello")
        lines.appendChild(text("li", null, "${copy.called}: ${greet ?: "-"}"))
        val birthday = personClass.function("birthday", 0)?.invoke(person)
        lines.appendChild(text("li", null, "${copy.propertyValue} age: ${personClass.property("age")?.get(person)} (birthday -> $birthday)"))
    } else {
        lines.appendChild(text("li", null, copy.detailEmpty))
    }
    section.appendChild(lines)
    return section
}

private fun proxySection(): HTMLElement {
    val section = div("section")
    section.appendChild(text("h2", null, copy.proxyHeading))
    section.appendChild(text("p", "hint", copy.proxyHint))

    var callCount = 0
    val greeterClass = KmpClasses.byNameOrNull("io.github.shijianjs.kmp.reflect.demo.model.Greeter")
    val output = text("p", "output", "")
    if (greeterClass == null) {
        output.textContent = copy.detailEmpty
    } else {
        val handler = KmpInvocationHandler { _, function, args ->
            callCount += 1
            "proxy(${function.name}) -> ${args.joinToString()} #$callCount"
        }
        val greeter = greeterClass.newProxy(handler) as Greeter
        output.textContent = "${copy.proxyResult}: ${greeter.greet("world")}"
    }
    section.appendChild(output)
    return section
}

@OptIn(ExperimentalEncodingApi::class)
private fun resourceSection(): HTMLElement {
    val section = div("section")
    section.appendChild(text("h2", null, copy.resourcesHeading))
    section.appendChild(text("p", "hint", copy.resourcesHint))
    val list = text("ul", "output", "")
    KmpResources.all().sortedBy { it.path }.forEach { resource ->
        list.appendChild(resourceLine(resource))
    }
    section.appendChild(list)
    return section
}

@OptIn(ExperimentalEncodingApi::class)
private fun resourceLine(resource: KmpResource): HTMLElement {
    val item = text("li", "resource", "${resource.path} — ${resource.mimeType}, ${resource.size} ${copy.resourceBytes}")
    if (resource.mimeType.startsWith("image/")) {
        val image = document.createElement("img") as HTMLImageElement
        css(image, "thumb")
        image.src = "data:${resource.mimeType};base64,${Base64.encode(resource.bytes)}"
        item.appendChild(image)
    } else {
        val preview = resource.text().lines().take(3).joinToString(" / ").trim()
        item.appendChild(text("p", "preview", preview))
    }
    return item
}

private fun annotationSection(): HTMLElement {
    val section = div("section")
    section.appendChild(text("h2", null, copy.annotationHeading))
    section.appendChild(text("p", "hint", copy.annotationHint))
    val list = text("ul", "output", "")
    val annotated = KmpClasses.annotatedWith("io.github.shijianjs.kmp.reflect.demo.model.KmpRecord")
    annotated.forEach { kmpClass ->
        val annotation = kmpClass.annotation("io.github.shijianjs.kmp.reflect.demo.model.KmpRecord")
        val detailText = annotation?.arguments?.entries?.joinToString { (name, value) -> "$name=$value" }.orEmpty()
        list.appendChild(text("li", null, "${kmpClass.simpleName}: $detailText"))
    }
    if (annotated.isEmpty()) list.appendChild(text("li", null, copy.noAnnotation))
    section.appendChild(list)
    return section
}

private fun enumSection(): HTMLElement {
    val section = div("section")
    section.appendChild(text("h2", null, copy.enumHeading))
    section.appendChild(text("p", "hint", copy.enumHint))
    val list = text("ul", "output", "")
    val shapeClass = KmpClasses.byNameOrNull("io.github.shijianjs.kmp.reflect.demo.model.Shape")
    shapeClass?.enumEntries?.forEach { entry ->
        list.appendChild(text("li", null, "${entry.name} (${entry.ordinal}) = ${(entry.instance as Shape).sides} sides"))
    }
    section.appendChild(list)
    return section
}

private fun nestedSection(): HTMLElement {
    val section = div("section")
    section.appendChild(text("h2", null, copy.nestedHeading))
    val list = text("ul", "output", "")
    val library = KmpClasses.byNameOrNull("io.github.shijianjs.kmp.reflect.demo.model.Library")
    library?.nestedClasses?.forEach { nested ->
        list.appendChild(text("li", null, "${library.simpleName} -> ${nested.qualifiedName}"))
    }
    section.appendChild(list)
    return section
}

private fun limitationsSection(): HTMLElement {
    val section = div("section")
    section.appendChild(text("h2", null, copy.limitationsHeading))
    val list = text("ul", "output", "")
    copy.limitations.forEach { limitation -> list.appendChild(text("li", null, limitation)) }
    section.appendChild(list)
    return section
}

private fun members(kmpClass: KmpClass): String =
    "${kmpClass.constructors.size} ctor / ${kmpClass.functions.size} fun / ${kmpClass.properties.size} prop"

private fun div(className: String): HTMLElement {
    val element = document.createElement("div") as HTMLElement
    css(element, className)
    return element
}

private fun text(tag: String, className: String?, value: String): HTMLElement {
    val element = document.createElement(tag) as HTMLElement
    if (className != null) css(element, className)
    element.textContent = value
    return element
}

private fun button(label: String, className: String, onClick: () -> Unit): HTMLElement {
    val element = document.createElement("button") as HTMLElement
    css(element, className)
    element.textContent = label
    element.onClick { onClick() }
    return element
}

/** Class names go through the attribute API, which takes a plain string. */
private fun css(element: HTMLElement, value: String) {
    element.setAttribute("class", value)
}

/** A single click listener. `onclick`'s branded event-handler type is not worth importing here. */
private fun HTMLElement.onClick(block: () -> Unit) {
    unsafeCast<dynamic>(this).onclick = { _: dynamic -> block() }
}