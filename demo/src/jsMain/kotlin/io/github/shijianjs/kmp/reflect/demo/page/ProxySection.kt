package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.model.Greeter
import io.github.shijianjs.kmp.reflect.proxy.KmpInvocationHandler
import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.inlineStyle
import web.cssom.Margin
import web.cssom.number
import web.cssom.px
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * Creates a proxy for `Greeter` and lets one call through it.
 *
 * The recorded result is what the handler answered, so the section shows that the call reached the
 * handler rather than the interface.
 */
internal class ProxySection {
    private val shell = SectionShell()

    val root: HTMLElement = shell.root

    private val output = HtmlTagName.p.createElement {
        classList.add(OUTPUT_CLASS)
        inlineStyle.apply {
            margin = Margin(0.px, 0.px)
            fontSize = 13.px
            lineHeight = number(1.6)
        }
    }

    private val proxyAnswer: String?

    init {
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
        shell.body.appendChild(output)
        applyCopy(english)
    }

    fun applyCopy(copy: Copy) {
        shell.setCopy(copy.proxyHeading, copy.proxyHint)
        output.textContent = proxyAnswer?.let { answer -> "${copy.proxyResult}: $answer" } ?: copy.detailEmpty
    }
}
