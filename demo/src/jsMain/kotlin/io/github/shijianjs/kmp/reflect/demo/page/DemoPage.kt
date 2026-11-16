package io.github.shijianjs.kmp.reflect.demo.page

import web.html.HTMLElement

/**
 * The page: one header, eight sections, and the language switch that re-labels them in place.
 *
 * Every section is built once in its own class; switching languages walks the sections and calls
 * their `applyCopy`, so no node is recreated and no metadata is re-read. The page owns no rules of its
 * own beyond [PAGE_CLASS] (which `index.html` already puts on the mount point); mounting only adopts
 * the sections' sheets, once, for the whole document.
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
        installPageStyle(
            HEADER_SHEET,
            SECTION_SHEET,
            OUTPUT_SHEET,
            BROWSER_SHEET,
            RESOURCES_SHEET,
        )
        if (!host.classList.contains(PAGE_CLASS)) {
            host.classList.add(PAGE_CLASS)
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
