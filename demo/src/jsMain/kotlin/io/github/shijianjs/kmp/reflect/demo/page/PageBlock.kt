package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.kmp.reflect.demo.page.lang.Translation
import web.html.HTMLElement

/**
 * A block the demo can mount on a page and re-label when the language changes.
 *
 * Every implementation is a custom element, so the block *is* its node: [node] saves call sites from
 * holding a second, parallel list of elements. Implementations only say how their own text changes —
 * the page decides when, and the shell decides what the page is called.
 */
internal interface PageBlock {
    /** The block as the node the page appends; a component is its own element. */
    val node: HTMLElement get() = this as HTMLElement

    fun applyTranslation(translation: Translation)
}
