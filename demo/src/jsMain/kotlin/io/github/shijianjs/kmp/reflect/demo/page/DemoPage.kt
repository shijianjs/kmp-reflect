package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.css.cssSheet
import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.kmp.reflect.demo.page.header.headerBarTag
import io.github.shijianjs.kmp.reflect.demo.page.lang.Translation
import io.github.shijianjs.kmp.reflect.demo.page.lang.Lang
import io.github.shijianjs.kmp.reflect.demo.page.lang.translationOf
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.annotationSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.classBrowserTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.enumSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.invocationSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.limitationsSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.nestedSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.proxySectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.resourcesSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.theme.RESET
import io.github.shijianjs.kmp.reflect.demo.page.theme.bodyText
import io.github.shijianjs.kmp.reflect.demo.page.theme.pageBackground
import web.components.CustomElementReference
import web.cssom.Auto
import web.cssom.CSSStyleSheet
import web.cssom.Display
import web.cssom.FontFamily
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.number
import web.cssom.px
import web.cssom.rem
import web.dom.TagName
import web.html.HTMLElement

/**
 * A block the page mounts and, on a language switch, re-labels in place.
 *
 * Every implementation is a custom element, so the block *is* its node: [node] saves calling sites
 * from holding a second, parallel list of elements. Implementations only have to say how they change
 * their own text — the page decides when.
 */
internal interface PageBlock {
    /** The block as the node the page appends; a component is its own element. */
    val node: HTMLElement get() = this as HTMLElement

    fun applyTranslation(translation: Translation)
}

/**
 * `<kmp-demo-page>`: one header, eight sections, and the language switch that re-labels them in place.
 *
 * Every section is its own custom element with its own shadow root and its own sheet; this element
 * only composes them, owns the current language, and walks [blocks] with `applyTranslation` when it
 * changes. No node is recreated and no metadata is re-read on a switch.
 */
internal class DemoPage : CustomElementAdapter() {
    private val header = headerBarTag().createElement()

    private val browser = classBrowserTag().createElement()

    private val invocation = invocationSectionTag().createElement()

    private val proxy = proxySectionTag().createElement()

    private val resources = resourcesSectionTag().createElement()

    private val annotations = annotationSectionTag().createElement()

    private val enums = enumSectionTag().createElement()

    private val nested = nestedSectionTag().createElement()

    private val limitations = limitationsSectionTag().createElement()

    /**
     * The blocks in render order.
     *
     * One list, not one list of elements plus one of callbacks: a block is mounted and re-labelled
     * through the same contract, and adding the next section is a single line here.
     */
    private val blocks: List<PageBlock> = listOf(
        header,
        browser,
        invocation,
        proxy,
        resources,
        annotations,
        enums,
        nested,
        limitations,
    )

    /** The page's language; named `currentLang` because `HTMLElement` already owns `lang`. */
    private var currentLang: Lang = Lang.EN

    init {
        // The header reports the click; the page owns what it means. Handing over the callback keeps
        // the component unaware of its parent.
        header.onSwitch = ::toggleLanguage

        // The host is the page: its own sheet carries the page box, the reset gives it a block display.
        val shadow = attachStyledShadow(RESET, PAGE_SHEET)
        blocks.forEach { block -> shadow.appendChild(block.node) }
    }

    override fun connectedCallback() {
        applyTranslation(translationOf(currentLang))
    }

    private fun toggleLanguage() {
        currentLang = if (currentLang == Lang.EN) Lang.ZH else Lang.EN
        applyTranslation(translationOf(currentLang))
    }

    private fun applyTranslation(translation: Translation) {
        blocks.forEach { block -> block.applyTranslation(translation) }
    }

    companion object : CustomElementReference<DemoPage>(DemoPage::class.js) {
        val NAME: TagName<DemoPage> = TagName("kmp-demo-page")

        /**
         * The page's own rules.
         *
         * Everything here is on `:host`, because a shadow tree is not laid out by the page's
         * light-DOM rules: the host is the box the document sees, and the inherited properties
         * (`color`, font, size) reach into the shadow trees from it.
         */
        private val PAGE_SHEET: CSSStyleSheet = cssSheet {
            ":host" {
                display = Display.block
                maxWidth = 62.rem
                margin = Margin(0.px, Auto.auto)
                padding = Padding(32.px, 24.px, 64.px)
                backgroundColor = pageBackground
                color = bodyText
                fontFamily = FontFamily.systemUi
                fontSize = 15.px
                lineHeight = number(1.55)
            }
        }
    }
}

/** Defines `<kmp-demo-page>` once and returns its tag. */
internal fun demoPageTag(): TagName<DemoPage> {
    defineIfMissing(DemoPage.NAME, DemoPage.Companion)
    return DemoPage.NAME
}
