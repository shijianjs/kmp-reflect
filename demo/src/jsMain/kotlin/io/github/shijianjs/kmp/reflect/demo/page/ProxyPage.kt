package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.ProxySection

/** Proxies: what `kmp-reflect` hands back for a class it cannot see at compile time. */
internal class ProxyPage : SectionPage() {
    init {
        show(ProxySection.NAME.createElement(), { it.proxyHeading }, { it.proxyHint })
    }

    companion object : CustomElementReferenceAdapter<ProxyPage>(ProxyPage::class.js, "reflect-proxy-page")
}
