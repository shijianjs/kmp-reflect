package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.kmp.reflect.demo.page.lang.Translation
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.section.OutputList
import io.github.shijianjs.kmp.reflect.demo.page.section.OUTPUT_SHEET
import io.github.shijianjs.kmp.reflect.demo.page.section.sectionShellTag
import io.github.shijianjs.kmp.reflect.demo.page.theme.RESET
import web.components.CustomElementReference
import web.dom.TagName

/** `<kmp-limitations>`: what the page itself demonstrates as a limit, one line per limitation. */
internal class LimitationsSection : CustomElementAdapter() {
    private val shell = sectionShellTag().createElement()

    private val list = OutputList()

    init {
        val shadow = attachStyledShadow(RESET, OUTPUT_SHEET)
        shadow.appendChild(shell)
        shell.appendChild(list.element)
        applyTranslation(english)
    }

    fun applyTranslation(translation: Translation) {
        shell.setText(translation.limitationsHeading, "")
        list.setLines(translation.limitations)
    }

    companion object : CustomElementReference<LimitationsSection>(LimitationsSection::class.js) {
        val NAME: TagName<LimitationsSection> = TagName("kmp-limitations")
    }
}

/** Defines `<kmp-limitations>` once and returns its tag. */
internal fun limitationsSectionTag(): TagName<LimitationsSection> {
    defineIfMissing(LimitationsSection.NAME, LimitationsSection.Companion)
    return LimitationsSection.NAME
}
