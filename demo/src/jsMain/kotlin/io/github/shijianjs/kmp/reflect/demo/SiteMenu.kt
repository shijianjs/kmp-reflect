package io.github.shijianjs.kmp.reflect.demo

import io.github.shijianjs.ktjs.demo.nav.DemoMenu
import io.github.shijianjs.ktjs.demo.nav.Entry
import io.github.shijianjs.ktjs.demo.nav.Group
import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.kmp.reflect.demo.page.sectionPage
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
            sectionPage({ it.classesHeading }, { it.classesHint }, classBrowserTag().createElement())
        },
        Entry("invocation", REFLECTION, "lucide:play") {
            sectionPage({ it.invocationHeading }, { it.invocationHint }, invocationSectionTag().createElement())
        },
        Entry("proxy", REFLECTION, "lucide:waypoints") {
            sectionPage({ it.proxyHeading }, { it.proxyHint }, proxySectionTag().createElement())
        },
        Entry("resources", COMPILER, "lucide:package") {
            sectionPage({ it.resourcesHeading }, { it.resourcesHint }, resourcesSectionTag().createElement())
        },
        Entry("annotations", COMPILER, "lucide:tag") {
            sectionPage({ it.annotationHeading }, { it.annotationHint }, annotationSectionTag().createElement())
        },
        Entry("enums", COMPILER, "lucide:list") {
            sectionPage({ it.enumHeading }, { it.enumHint }, enumSectionTag().createElement())
        },
        Entry("nested", COMPILER, "lucide:folder-tree") {
            sectionPage({ it.nestedHeading }, { it.nestedHint }, nestedSectionTag().createElement())
        },
        Entry("limits", COMPILER, "lucide:triangle-alert") {
            sectionPage({ it.limitationsHeading }, { "" }, limitationsSectionTag().createElement())
        },
    ),
)
