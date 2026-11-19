package io.github.shijianjs.kmp.reflect.demo

import io.github.shijianjs.ktjs.demo.i18n.LanguageState
import io.github.shijianjs.ktjs.demo.layout.DemoLayout
import io.github.shijianjs.ktjs.demo.layout.LayoutConfig
import io.github.shijianjs.ktjs.demo.style.Theme
import io.github.shijianjs.kmp.reflect.KmpReflection
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.annotationSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.classBrowserTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.enumSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.invocationSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.limitationsSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.nestedSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.proxySectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.resourcesSectionTag
import web.dom.ElementId
import web.dom.document

/** Where this demo is published, next to its documentation and its API reference. */
private const val DOCS_URL: String = "https://shijianjs.github.io/kmp-reflect/"

private const val API_URL: String = "https://shijianjs.github.io/kmp-reflect/api/"

private const val SOURCE_URL: String = "https://github.com/shijianjs/kmp-reflect"

/**
 * Entry point: register the generated metadata, define the section tags, then mount the shell.
 *
 * The order matters. Metadata comes first of all: every section reads it while constructing. The
 * section tags are defined before the layout mounts, because the layout builds the first page eagerly
 * and a page's constructor creates its section — an undefined tag there would be an unknown element
 * that upgrades later, in an order nobody controls.
 */
public fun main() {
    KmpReflection.register(DemoReflection)

    classBrowserTag()
    invocationSectionTag()
    proxySectionTag()
    resourcesSectionTag()
    annotationSectionTag()
    enumSectionTag()
    nestedSectionTag()
    limitationsSectionTag()

    DemoLayout.define()

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
