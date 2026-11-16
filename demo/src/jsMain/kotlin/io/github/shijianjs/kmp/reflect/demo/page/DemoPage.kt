package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.inlineStyle
import web.cssom.Auto
import web.cssom.FontFamily
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.number
import web.cssom.px
import web.cssom.rem
import web.html.HTMLElement

/**
 * The page: one header, eight sections, and the language switch that re-labels them in place.
 *
 * Every section is built once in its own class; switching languages walks the sections and calls
 * their `applyCopy`, so no node is recreated and no metadata is re-read.
 */
internal class DemoPage(private val host: HTMLElement) {
    private val header = HeaderBar(onSwitch = ::toggleLanguage)

    private val browser = ClassBrowser()

    private val invocation = InvocationSection()

    private val proxy = ProxySection()

    private val resources = ResourcesSection()

    private val annotations = AnnotationSection()

    private val enums = EnumSection()

    private val nested = NestedSection()

    private val limitations = LimitationsSection()

    private var lang: Lang = Lang.EN

    fun mount() {
        installPageStyle()
        host.inlineStyle.apply {
            backgroundColor = pageBackground
            color = bodyText
            fontFamily = FontFamily.systemUi
            fontSize = 15.px
            lineHeight = number(1.55)
            maxWidth = 62.rem
            margin = Margin(0.px, Auto.auto)
            padding = Padding(32.px, 24.px, 64.px)
        }
        host.replaceChildren(
            header.root,
            browser.root,
            invocation.root,
            proxy.root,
            resources.root,
            annotations.root,
            enums.root,
            nested.root,
            limitations.root,
        )
        applyCopy(copyOf(lang))
    }

    private fun toggleLanguage() {
        lang = if (lang == Lang.EN) Lang.ZH else Lang.EN
        applyCopy(copyOf(lang))
    }

    private fun applyCopy(copy: Copy) {
        header.applyCopy(copy)
        browser.applyCopy(copy)
        invocation.applyCopy(copy)
        proxy.applyCopy(copy)
        resources.applyCopy(copy)
        annotations.applyCopy(copy)
        enums.applyCopy(copy)
        nested.applyCopy(copy)
        limitations.applyCopy(copy)
    }
}
