package io.github.shijianjs.kmp.reflect.demo.page

import web.html.HTMLElement

/** What the page itself demonstrates as a limit, one line per limitation. */
internal class LimitationsSection {
    private val shell = SectionShell()

    val root: HTMLElement = shell.root

    private val list = OutputList()

    init {
        shell.body.appendChild(list.element)
        applyCopy(english)
    }

    fun applyCopy(copy: Copy) {
        shell.setCopy(copy.limitationsHeading, "")
        list.setLines(copy.limitations)
    }
}
