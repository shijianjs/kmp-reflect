package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.model.Greeter
import io.github.shijianjs.kmp.reflect.proxy.KmpInvocationHandler
import web.html.HTMLElement

/**
 * Creates a proxy for `Greeter` and lets one call through it.
 *
 * The recorded result is what the handler answered, so the section shows that the call reached the
 * handler rather than the interface. The answer is one line of the shared output list, so this
 * section needs no rules of its own.
 */
internal class ProxySection {
    private val shell = SectionShell()

    val root: HTMLElement = shell.root

    private val list = OutputList()

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
        shell.body.appendChild(list.element)
        applyCopy(english)
    }

    fun applyCopy(copy: Copy) {
        shell.setCopy(copy.proxyHeading, copy.proxyHint)
        list.setLines(listOf(proxyAnswer?.let { answer -> "${copy.proxyResult}: $answer" } ?: copy.detailEmpty))
    }
}
