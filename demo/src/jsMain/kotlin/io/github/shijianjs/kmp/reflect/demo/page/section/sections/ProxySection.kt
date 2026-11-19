package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.model.Greeter
import io.github.shijianjs.kmp.reflect.demo.page.PageBlock
import io.github.shijianjs.kmp.reflect.demo.page.lang.Translation
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.section.OutputList
import io.github.shijianjs.kmp.reflect.demo.page.section.OUTPUT_SHEET
import io.github.shijianjs.kmp.reflect.demo.style.RESET
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
internal class ProxySection : CustomElementAdapter(), PageBlock {

    private val list = OutputList()

    private val proxyAnswer: String?

    init {
        val shadow = attachStyledShadow(RESET, OUTPUT_SHEET)
        shadow.appendChild(list.element)

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
        applyTranslation(english)
    }

    override fun applyTranslation(translation: Translation) {
        list.setLines(listOf(proxyAnswer?.let { answer -> "${translation.proxyResult}: $answer" } ?: translation.detailEmpty))
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
