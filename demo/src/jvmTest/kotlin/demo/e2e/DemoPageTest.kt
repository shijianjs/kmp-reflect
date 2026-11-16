package demo.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.junit.UsePlaywright
import com.microsoft.playwright.options.WaitUntilState
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

/**
 * Smoke checks for the demo page: the bundle loads, the metadata sections render, the language
 * switch re-renders, and nothing logs an error or fails a request.
 *
 * The page reads only generated metadata, so a green run here means the processor also ran for the
 * JS target — that is the whole point of running the browser suite in CI.
 */
@UsePlaywright(DemoOptions::class)
class DemoPageTest {
    @Test
    fun `page renders the heading and stylesheet`(page: Page) {
        openPage(page)

        assertTrue(page.title().contains("kmp-reflect"), "title: ${page.title()}")
        assertEquals("kmp-reflect", page.locator("h1.title").textContent())

        val display = page.locator(".page").evaluate("(node) => getComputedStyle(node).display") as String
        assertTrue(display != "contents", "the stylesheet applies to .page (display = $display)")
    }

    @Test
    fun `reflected classes are listed and selectable`(page: Page) {
        openPage(page)

        val personRow = page.locator("tr.row", Page.LocatorOptions().setHasText("demo.model.Person"))
        assertTrue(personRow.count() > 0, "Person is listed among the reflected classes")

        page.locator("tr.row", Page.LocatorOptions().setHasText("Person"))
            .first()
            .click()

        val members = page.locator(".detail .member").allTextContents()
        assertTrue(members.any { it.contains("fun greet(") }, "members: $members")
        assertTrue(members.any { it.contains("val greeting") }, "properties are listed too")
    }

    @Test
    fun `metadata drives construction, invocation and the proxy`(page: Page) {
        openPage(page)

        val output = page.locator(".output").allTextContents().joinToString(" | ")
        assertTrue(output.contains("Hello, Ada!"), "invocation output: $output")
        assertTrue(output.contains("proxy(greet) -> world"), "proxy output: $output")
    }

    @Test
    fun `annotation scan and resources are shown`(page: Page) {
        openPage(page)

        val text = page.locator(".output").allTextContents().joinToString(" | ")
        assertTrue(text.contains("name=person"), "annotation arguments: $text")
        assertTrue(text.contains("config.json"), "resource paths: $text")
        assertTrue(page.locator("img.thumb").count() == 1, "the SVG resource is rendered from its bytes")
    }

    @Test
    fun `language switch re-renders in Chinese`(page: Page) {
        openPage(page)

        page.locator(".lang-switch").click()

        assertEquals("EN", page.locator(".lang-switch").textContent())
        assertTrue(page.locator("h2").first().textContent()!!.contains("已反射的类"))
        assertTrue(page.locator("h2").allTextContents().any { it.contains("动态代理") })
    }

    @Test
    fun `page logs no console errors or failed requests`(page: Page) {
        val consoleErrors = mutableListOf<String>()
        val failedRequests = mutableListOf<String>()
        page.onConsoleMessage { message ->
            if (message.type() == "error") consoleErrors += message.text()
        }
        page.onRequestFailed { request ->
            failedRequests += "${request.url()} ${request.failure()}"
        }

        openPage(page)

        assertTrue(consoleErrors.isEmpty(), "console errors: $consoleErrors")
        assertTrue(failedRequests.isEmpty(), "failed requests: $failedRequests")
    }

    private fun openPage(page: Page) {
        page.navigate("/", Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))
    }
}
