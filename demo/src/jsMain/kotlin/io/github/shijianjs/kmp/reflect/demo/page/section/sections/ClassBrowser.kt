package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.css.cssSheet
import io.github.shijianjs.ktjs.dom.css.dsl.Pseudos
import io.github.shijianjs.ktjs.dom.css.dsl.invoke
import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.kmp.reflect.KmpClass
import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.page.lang.Copy
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.section.SECTION_SHEET
import io.github.shijianjs.kmp.reflect.demo.page.section.SectionShell
import io.github.shijianjs.kmp.reflect.demo.page.theme.BADGE_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.BADGE_MUTED_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.BADGE_OK_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.CELL_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.CODE_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.DETAIL_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.DETAIL_HEADING_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.DETAIL_HINT_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.HEAD_CELL_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.HINT_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.MEMBER_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.MEMBER_LIST_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.RESET
import io.github.shijianjs.kmp.reflect.demo.page.theme.ROW_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.SELECTED_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.TABLE_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.hairline
import io.github.shijianjs.kmp.reflect.demo.page.theme.mutedFill
import io.github.shijianjs.kmp.reflect.demo.page.theme.mutedText
import io.github.shijianjs.kmp.reflect.demo.page.theme.okFill
import io.github.shijianjs.kmp.reflect.demo.page.theme.okText
import io.github.shijianjs.kmp.reflect.demo.page.theme.panel
import io.github.shijianjs.kmp.reflect.demo.page.theme.panelStrong
import io.github.shijianjs.kmp.reflect.demo.page.theme.selectedFill
import web.components.CustomElementReference
import web.cssom.Border
import web.cssom.BorderCollapse
import web.cssom.CSSStyleSheet
import web.cssom.Cursor
import web.cssom.FontFamily
import web.cssom.FontWeight
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.Overflow
import web.cssom.Padding
import web.cssom.TextAlign
import web.cssom.VerticalAlign
import web.cssom.integer
import web.cssom.number
import web.cssom.pct
import web.cssom.px
import web.dom.TagName
import web.events.EventHandler
import web.html.HTMLDivElement
import web.html.HTMLHeadingElement
import web.html.HTMLTableElement
import web.html.HTMLTableCellElement
import web.html.HTMLTableRowElement
import web.html.HTMLTableSectionElement
import web.html.HTMLElement
import web.html.HtmlTagName

/** One reflected class in the table, with the two cells whose text is language-dependent. */
private class ClassRow(
    val kmpClass: KmpClass,
    val rowElement: HTMLTableRowElement,
    val membersCell: HTMLTableCellElement,
    val annotationsCell: HTMLTableCellElement,
)

/**
 * `<kmp-classes>`: the class table and the member panel under it.
 *
 * Rows are built once from the registered metadata; a click toggles the selected class, which repaints
 * two rows and the detail panel — nothing else is rebuilt, and switching languages only re-labels the
 * header and re-draws the detail. Rules live in this element's own sheet, adopted by its shadow root,
 * so they cannot reach any other component.
 */
internal class ClassBrowser : CustomElementAdapter() {
    private val shell = SectionShell()

    private val headRow: HTMLTableRowElement = HtmlTagName.tr.createElement()

    private val rows = mutableListOf<ClassRow>()

    private val bodyRows: HTMLTableSectionElement = HtmlTagName.tbody.createElement()

    private val table: HTMLTableElement = HtmlTagName.table.createElement {
        classList.add(TABLE_CLASS)
        appendChild(HtmlTagName.thead.createElement { appendChild(headRow) })
        appendChild(bodyRows)
    }

    private val heading: HTMLHeadingElement = HtmlTagName.h3.createElement {
        classList.add(DETAIL_HEADING_CLASS)
    }

    private val detailBody: HTMLDivElement = HtmlTagName.div.createElement()

    private val detailPanel: HTMLDivElement = HtmlTagName.div.createElement {
        classList.add(DETAIL_CLASS)
        appendChild(heading)
        appendChild(detailBody)
    }

    private var copy: Copy = english

    private var selected: String? = null

    init {
        val shadow = attachStyledShadow(RESET, SECTION_SHEET, BROWSER_SHEET)
        shell.nodes.forEach { node -> shadow.appendChild(node) }

        KmpClasses.all().forEach { kmpClass ->
            val members = cell()
            val annotations = cell()
            val row = HtmlTagName.tr.createElement {
                classList.add(ROW_CLASS)
                appendChild(cell(kmpClass.qualifiedName, monospace = true))
                appendChild(cell(kmpClass.kind.name.lowercase()))
                appendChild(members)
                appendChild(annotations)
                onclick = EventHandler { toggleSelection(kmpClass.qualifiedName) }
            }
            rows += ClassRow(kmpClass, row, members, annotations)
            bodyRows.appendChild(row)
        }

        shell.body.appendChild(table)
        shell.body.appendChild(detailPanel)
        applyCopy(english)
    }

    /** Heading, hint, column labels and the language-dependent cell text. */
    fun applyCopy(copy: Copy) {
        this.copy = copy
        shell.setCopy(copy.classesHeading, copy.classesHint)
        headRow.replaceChildren(
            headerCell(copy.columnName),
            headerCell(copy.columnKind),
            headerCell(copy.columnMembers),
            headerCell(copy.columnAnnotations),
        )
        rows.forEach { row ->
            row.membersCell.textContent = copy.membersSummary(
                row.kmpClass.constructors.size,
                row.kmpClass.functions.size,
                row.kmpClass.properties.size,
            )
            row.annotationsCell.textContent = row.kmpClass.annotations
                .joinToString { annotation -> annotation.type.simpleName }
                .ifEmpty { copy.noAnnotation }
        }
        rebuildDetail()
    }

    private fun toggleSelection(qualifiedName: String) {
        selected = if (selected == qualifiedName) null else qualifiedName
        rows.forEach { row ->
            if (row.kmpClass.qualifiedName == selected) {
                row.rowElement.classList.add(SELECTED_CLASS)
            } else {
                row.rowElement.classList.remove(SELECTED_CLASS)
            }
        }
        rebuildDetail()
    }

    /** Local collection rebuild: the panel is one node, and the selection is what changes it. */
    private fun rebuildDetail() {
        heading.textContent = copy.detailHeading
        val kmpClass = selected?.let { name -> KmpClasses.byNameOrNull(name) }
        if (kmpClass == null) {
            detailBody.replaceChildren(
                HtmlTagName.p.createElement {
                    classList.add(HINT_CLASS)
                    classList.add(DETAIL_HINT_CLASS)
                    textContent = copy.detailEmpty
                },
            )
            return
        }

        val list = HtmlTagName.ul.createElement {
            classList.add(MEMBER_LIST_CLASS)
        }
        kmpClass.functions.forEach { function ->
            list.appendChild(
                memberLine(
                    "fun ${function.name}(${function.parameters.joinToString { parameter -> "${parameter.name}: ${parameter.type.simpleName}" }}): ${function.returnType.simpleName}",
                    function.isInvokable,
                ),
            )
        }
        kmpClass.properties.forEach { property ->
            list.appendChild(
                memberLine(
                    "${if (property.isMutable) "var" else "val"} ${property.name}: ${property.type.simpleName}",
                    property.isReadable,
                ),
            )
        }
        kmpClass.constructors.forEach { constructor ->
            list.appendChild(
                memberLine(
                    "constructor(${constructor.parameters.joinToString { parameter -> "${parameter.name}: ${parameter.type.simpleName}" }})",
                    constructor.isInvokable,
                ),
            )
        }
        detailBody.replaceChildren(list)
    }

    private fun memberLine(label: String, invokable: Boolean): HTMLElement =
        HtmlTagName.li.createElement {
            classList.add(MEMBER_CLASS)
            classList.add(CODE_CLASS)
            textContent = label
            appendChild(
                HtmlTagName.span.createElement {
                    classList.add(BADGE_CLASS)
                    // Two states, two classes: the colour is static, so nothing here is inline.
                    classList.add(if (invokable) BADGE_OK_CLASS else BADGE_MUTED_CLASS)
                    textContent = if (invokable) copy.invokable else copy.notInvokable
                },
            )
        }

    private fun headerCell(label: String): HTMLTableCellElement =
        HtmlTagName.th.createElement {
            classList.add(CELL_CLASS)
            classList.add(HEAD_CELL_CLASS)
            textContent = label
        }

    private fun cell(value: String = "", monospace: Boolean = false): HTMLTableCellElement =
        HtmlTagName.td.createElement {
            classList.add(CELL_CLASS)
            if (monospace) {
                classList.add(CODE_CLASS)
            }
            textContent = value
        }

    companion object : CustomElementReference<ClassBrowser>(ClassBrowser::class.js) {
        val NAME: TagName<ClassBrowser> = TagName("kmp-classes")

        /**
         * The browser's own rules: one rule per node kind, so the table costs four rules whether it
         * holds nine classes or ninety.
         *
         * `.row.is-selected` is the one compound selector in the page: CSS has no typed way to join
         * two `ClassName`s, so it is spelled once here, next to the two tokens it is made of. It comes
         * after `.row:hover` so a selected row keeps its fill while the pointer is over it.
         */
        private val BROWSER_SHEET: CSSStyleSheet = cssSheet {
            TABLE_CLASS {
                width = 100.pct
                borderCollapse = BorderCollapse.collapse
                backgroundColor = panel
                border = Border(1.px, LineStyle.solid, hairline)
                borderRadius = 10.px
                overflow = Overflow.hidden
                fontSize = 13.px
            }

            CELL_CLASS {
                textAlign = TextAlign.left
                verticalAlign = VerticalAlign.top
                padding = Padding(9.px, 12.px)
                borderBottomWidth = 1.px
                borderBottomStyle = LineStyle.solid
                borderBottomColor = hairline
                lineHeight = number(1.5)
            }

            HEAD_CELL_CLASS {
                backgroundColor = panelStrong
                color = mutedText
                fontWeight = integer(600)
            }

            CODE_CLASS {
                fontFamily = FontFamily.monospace
            }

            ROW_CLASS {
                cursor = Cursor.pointer

                Pseudos.hover { backgroundColor = panelStrong }
            }

            ".row.is-selected" { backgroundColor = selectedFill }

            DETAIL_CLASS {
                marginTop = 14.px
                padding = Padding(14.px, 16.px)
                backgroundColor = panel
                border = Border(1.px, LineStyle.solid, hairline)
                borderRadius = 10.px
            }

            DETAIL_HEADING_CLASS {
                margin = Margin(0.px, 0.px, 8.px)
                fontSize = 15.px
                fontWeight = integer(600)
                color = mutedText
            }

            MEMBER_LIST_CLASS {
                margin = Margin(0.px, 0.px)
                paddingLeft = 18.px
                fontSize = 13.px
            }

            MEMBER_CLASS {
                marginBottom = 2.px
            }

            BADGE_CLASS {
                marginLeft = 8.px
                padding = Padding(1.px, 7.px)
                borderRadius = 999.px
                fontSize = 11.px
            }

            BADGE_OK_CLASS {
                backgroundColor = okFill
                color = okText
            }

            BADGE_MUTED_CLASS {
                backgroundColor = mutedFill
                color = mutedText
            }
        }
    }
}

/** Defines `<kmp-classes>` once and returns its tag. */
internal fun classBrowserTag(): TagName<ClassBrowser> {
    defineIfMissing(ClassBrowser.NAME, ClassBrowser.Companion)
    return ClassBrowser.NAME
}
