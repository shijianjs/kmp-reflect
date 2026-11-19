package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.demo.i18n.Lang
import io.github.shijianjs.ktjs.demo.page.DemoPage
import io.github.shijianjs.kmp.reflect.demo.page.lang.Translation
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.lang.translationOf

/**
 * Base of the eight one-section pages: the shell's title and summary for the route, then the block
 * that demonstrates it.
 *
 * The eight sections used to share one long page, each carrying its own heading. They are one page
 * each now, so the heading lives in the page header — a subclass hands in its strings and its block
 * from `init`, and [applyTranslation] keeps the two revised together.
 *
 * The block is built once, when the route is first shown; the shell caches the page, so a trip to
 * another route and back finds the same component with its state (a selected row, an opened detail)
 * intact.
 */
internal open class SectionPage : DemoPage() {
    private var block: PageBlock? = null

    private var heading: ((Translation) -> String)? = null

    private var hint: ((Translation) -> String)? = null

    /** Hands the page its content: one block, plus the two strings the header reads. */
    protected fun show(
        block: PageBlock,
        heading: (Translation) -> String,
        hint: (Translation) -> String,
    ) {
        this.block = block
        this.heading = heading
        this.hint = hint
        body.append(block.node)
        apply(english)
    }

    override fun applyTranslation(lang: Lang) {
        apply(translationOf(lang))
    }

    private fun apply(translation: Translation) {
        val heading = heading ?: return
        setTitle(heading(translation))
        setSummary(hint?.invoke(translation) ?: "")
        block?.applyTranslation(translation)
    }
}
