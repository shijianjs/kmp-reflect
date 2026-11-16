package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.model.Greeter
import io.github.shijianjs.kmp.reflect.demo.page.lang.Copy
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.section.OutputList
import io.github.shijianjs.kmp.reflect.demo.page.section.OUTPUT_SHEET
import io.github.shijianjs.kmp.reflect.demo.page.section.SECTION_SHEET
import io.github.shijianjs.kmp.reflect.demo.page.section.SectionShell
import io.github.shijianjs.kmp.reflect.demo.page.theme.RESET
import io.github.shijianjs.kmp.reflect.proxy.KmpInvocationHandler
import web.components.CustomElementReference
import web.dom.TagName

/**
 * `<kmp-proxy>`: creates a proxy for `Greeter` and lets one call through it.
 *
 * The recorded answer is what the handler replied, so the section shows that the call reached the
 * handler rather than the interface. The answer is one line of the shared output list, so this
 * section needs no rules of its own.
 */
internal class ProxySection : CustomElementAdapter() {
    private val shell = SectionShell()

    private val list = OutputList()

    private val proxyAnswer: String?

    init {
        val shadow = attachStyledShadow(RESET, SECTION_SHEET, OUTPUT_SHEET)
        shell.nodes.forEach { node -> shadow.appendChild(node) }
        shell.body.appendChild(list.element)

        val greeterClass = KmpClasses.byNameOrNull("io.github.shijianjs.kmp.reflect.demo.model.Greeter")
        proxyAnswer = if (greeterClass == null) {
            null
        } else {
            var callCount = 0
            val handler = KmpInvocationHandler { _, function, args ->
                callCount += 1
                "proxy(${function.name}) -> ${args.joinToString()} #$callCount"
            }
            val greeter = greeterClass.newProxy(handler) as Greeter
            greeter.greet("world")
        }
        applyCopy(english)
    }

    fun applyCopy(copy: Copy) {
        shell.setCopy(copy.proxyHeading, copy.proxyHint)
        list.setLines(listOf(proxyAnswer?.let { answer -> "${copy.proxyResult}: $answer" } ?: copy.detailEmpty))
    }

    companion object : CustomElementReference<ProxySection>(ProxySection::class.js) {
        val NAME: TagName<ProxySection> = TagName("kmp-proxy")
    }
}

/** Defines `<kmp-proxy>` once and returns its tag. */
internal fun proxySectionTag(): TagName<ProxySection> {
    defineIfMissing(ProxySection.NAME, ProxySection.Companion)
    return ProxySection.NAME
}
