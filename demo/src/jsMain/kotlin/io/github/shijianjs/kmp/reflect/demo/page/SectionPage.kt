package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.demo.i18n.Lang
import io.github.shijianjs.ktjs.demo.i18n.Langs
import io.github.shijianjs.ktjs.demo.nav.Page
import io.github.shijianjs.ktjs.demo.page.newPage
import io.github.shijianjs.kmp.reflect.demo.page.lang.Translation
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.lang.translationOf

/**
 * One demo page: the shell's title and summary for the route, then the block that demonstrates it.
 *
 * The eight sections used to share one long page, each carrying its own heading. They are one page
 * each now, so the heading moved to the shell's page header — [heading] and [hint] are what the
 * navigation and the page intro read, and the section only renders its content. That keeps one
 * heading per page instead of an `h1` repeating what the `h2` below says.
 *
 * The block is built once, when the route is first shown; the shell caches the page, so a trip to
 * another route and back finds the same component with its state (a selected row, an opened detail)
 * intact.
 */
internal fun sectionPage(
    heading: (Translation) -> String,
    hint: (Translation) -> String,
    block: PageBlock,
): Page {
    val page = newPage()
    page.body.append(block.node)

    fun apply(translation: Translation) {
        page.setTitle(heading(translation))
        page.setSummary(hint(translation))
        block.applyTranslation(translation)
    }

    apply(english)

    return object : Page {
        override val node get() = page

        override fun applyTranslation(lang: Lang) {
            apply(translationOf(lang))
        }
    }
}
