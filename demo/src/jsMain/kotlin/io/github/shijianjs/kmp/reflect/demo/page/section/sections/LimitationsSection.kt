package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.kmp.reflect.demo.page.lang.Copy
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.section.OutputList
import io.github.shijianjs.kmp.reflect.demo.page.section.OUTPUT_SHEET
import io.github.shijianjs.kmp.reflect.demo.page.section.SECTION_SHEET
import io.github.shijianjs.kmp.reflect.demo.page.section.SectionShell
import io.github.shijianjs.kmp.reflect.demo.page.theme.RESET
import web.components.CustomElementReference
import web.dom.TagName

/** `<kmp-limitations>`: what the page itself demonstrates as a limit, one line per limitation. */
internal class LimitationsSection : CustomElementAdapter() {
    private val shell = SectionShell()

    private val list = OutputList()

    init {
        val shadow = attachStyledShadow(RESET, SECTION_SHEET, OUTPUT_SHEET)
        shell.nodes.forEach { node -> shadow.appendChild(node) }
        shell.body.appendChild(list.element)
        applyCopy(english)
    }

    fun applyCopy(copy: Copy) {
        shell.setCopy(copy.limitationsHeading, "")
        list.setLines(copy.limitations)
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
