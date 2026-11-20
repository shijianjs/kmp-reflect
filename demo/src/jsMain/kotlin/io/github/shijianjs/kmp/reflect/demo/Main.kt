package io.github.shijianjs.kmp.reflect.demo

import io.github.shijianjs.ktjs.demo.i18n.LanguageState
import io.github.shijianjs.ktjs.demo.layout.DemoLayout
import io.github.shijianjs.ktjs.demo.layout.LayoutConfig
import io.github.shijianjs.ktjs.demo.style.Theme
import io.github.shijianjs.kmp.reflect.KmpReflection
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.AnnotationSection
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.ClassBrowser
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.EnumSection
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.InvocationSection
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.LimitationsSection
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.NestedSection
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.ProxySection
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.ResourcesSection
import web.dom.ElementId
import web.dom.document
import io.github.shijianjs.ktjs.dom.element.invoke

/** Where this demo is published, next to its documentation and its API reference. */
private const val DOCS_URL: String = "https://shijianjs.github.io/kmp-reflect/"

private const val API_URL: String = "https://shijianjs.github.io/kmp-reflect/api/"

private const val SOURCE_URL: String = "https://github.com/shijianjs/kmp-reflect"

/**
 * Entry point: register the generated metadata, let each section's companion register its tag, then
 * mount the shell.
 *
 * The order matters. Metadata comes first of all: every section reads it while constructing. Each
 * tag is registered before the layout mounts, because the layout builds the first page eagerly and
 * a page's constructor creates its section — an undefined tag there would be an unknown element that
 * upgrades later, in an order nobody controls.
 */
@Suppress("UNUSED_EXPRESSION")
public fun main() {
    KmpReflection.register(DemoReflection)

    // Reading `NAME` is what registers the tag: the companion's `init` defines it on first read, and
    // the browser upgrades the element the page already spells.
    ClassBrowser.NAME
    InvocationSection.NAME
    ProxySection.NAME
    ResourcesSection.NAME
    AnnotationSection.NAME
    EnumSection.NAME
    NestedSection.NAME
    LimitationsSection.NAME

    DemoLayout.NAME

    val language = LanguageState(storageKey = "kmp-reflect-language").also { it.init() }
    val theme = Theme(storageKey = "kmp-reflect-theme").also { it.applyStored() }

    val layout = document.getElementById(ElementId("demo")) as? DemoLayout ?: return
    layout.mount(
        LayoutConfig(
            menu = siteMenu(),
            language = language,
            theme = theme,
            texts = siteTexts,
            siteVersion = "Kotlin 2.4.21",
            docsUrl = DOCS_URL,
            apiUrl = API_URL,
            sourceUrl = SOURCE_URL,
            logoSrc = "img/kmp-reflect-logo.png",
        ),
    )
}
