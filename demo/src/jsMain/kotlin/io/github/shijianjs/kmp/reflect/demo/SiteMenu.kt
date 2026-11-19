package io.github.shijianjs.kmp.reflect.demo

import io.github.shijianjs.ktjs.demo.nav.DemoMenu
import io.github.shijianjs.ktjs.demo.nav.Entry
import io.github.shijianjs.ktjs.demo.nav.Group
import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.kmp.reflect.demo.page.newAnnotationsPage
import io.github.shijianjs.kmp.reflect.demo.page.newClassesPage
import io.github.shijianjs.kmp.reflect.demo.page.newEnumsPage
import io.github.shijianjs.kmp.reflect.demo.page.newInvocationPage
import io.github.shijianjs.kmp.reflect.demo.page.newLimitsPage
import io.github.shijianjs.kmp.reflect.demo.page.newNestedPage
import io.github.shijianjs.kmp.reflect.demo.page.newProxyPage
import io.github.shijianjs.kmp.reflect.demo.page.newResourcesPage
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.annotationSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.classBrowserTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.enumSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.invocationSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.limitationsSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.nestedSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.proxySectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.resourcesSectionTag

/**
 * The demo's pages: two groups, eight routes, one per capability.
 *
 * The route ids double as the keys of `SiteTexts.pageTitles`, so a page's menu label and its heading
 * come from one place. Icons are Iconify names drawn in the sidebar; the sections themselves add no
 * icon, which keeps the navigation the only source of them.
 */
private val REFLECTION: Group = Group("reflection", "lucide:boxes")

private val COMPILER: Group = Group("compiler", "lucide:file-code")

/** The demo's navigation, in the order the pages are shown. */
internal fun siteMenu(): DemoMenu = DemoMenu(
    listOf(
        Entry("classes", REFLECTION, "lucide:table") {
            newClassesPage()
        },
        Entry("invocation", REFLECTION, "lucide:play") {
            newInvocationPage()
        },
        Entry("proxy", REFLECTION, "lucide:waypoints") {
            newProxyPage()
        },
        Entry("resources", COMPILER, "lucide:package") {
            newResourcesPage()
        },
        Entry("annotations", COMPILER, "lucide:tag") {
            newAnnotationsPage()
        },
        Entry("enums", COMPILER, "lucide:list") {
            newEnumsPage()
        },
        Entry("nested", COMPILER, "lucide:folder-tree") {
            newNestedPage()
        },
        Entry("limits", COMPILER, "lucide:triangle-alert") {
            newLimitsPage()
        },
    ),
)

