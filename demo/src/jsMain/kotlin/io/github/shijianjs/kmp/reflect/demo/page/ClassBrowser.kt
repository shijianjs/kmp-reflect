package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.kmp.reflect.KmpClass
import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.inlineStyle
import web.cssom.BorderCollapse
import web.cssom.Cursor
import web.cssom.FontFamily
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
 * The class table and the member panel under it.
 *
 * Rows are built once from the registered metadata; a click toggles the selected class, which repaints
 * two rows and the detail panel — nothing else on the page is rebuilt, and switching languages only
 * re-labels the header and re-draws the detail.
 */
internal class ClassBrowser {
    private val shell = SectionShell()

    val root: HTMLElement = shell.root

    private val headRow: HTMLTableRowElement = HtmlTagName.tr.createElement()

    private val rows = mutableListOf<ClassRow>()

    private val bodyRows: HTMLTableSectionElement = HtmlTagName.tbody.createElement()

    private val table: HTMLTableElement = HtmlTagName.table.createElement {
        inlineStyle.apply {
            width = 100.pct
            borderCollapse = BorderCollapse.collapse
            backgroundColor = panel
            borderWidth = 1.px
            borderStyle = LineStyle.solid
            borderColor = hairline
            borderRadius = 10.px
            overflow = Overflow.hidden
            fontSize = 13.px
        }
        appendChild(HtmlTagName.thead.createElement { appendChild(headRow) })
        appendChild(bodyRows)
    }

    private val heading: HTMLHeadingElement = HtmlTagName.h3.createElement {
        inlineStyle.apply {
            margin = Margin(0.px, 0.px, 8.px)
            fontSize = 15.px
            fontWeight = integer(600)
            color = mutedText
        }
    }

    private val detailBody: HTMLDivElement = HtmlTagName.div.createElement()

    private val detailPanel: HTMLDivElement = HtmlTagName.div.createElement {
        addClass("detail")
        inlineStyle.apply {
            marginTop = 14.px
            padding = Padding(14.px, 16.px)
            backgroundColor = panel
            borderWidth = 1.px
            borderStyle = LineStyle.solid
            borderColor = hairline
            borderRadius = 10.px
        }
        appendChild(heading)
        appendChild(detailBody)
    }

    private var copy: Copy = english

    private var selected: String? = null

    init {
        KmpClasses.all().forEach { kmpClass ->
            val members = cell()
            val annotations = cell()
            val row = HtmlTagName.tr.createElement {
                classList.add(ROW_CLASS)
                inlineStyle.cursor = Cursor.pointer
                appendChild(cell(kmpClass.qualifiedName, monospace = true))
                appendChild(cell(kmpClass.kind.name.lowercase()))
                appendChild(members)
                appendChild(annotations)
                onclick = web.events.EventHandler { toggleSelection(kmpClass.qualifiedName) }
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

    /** Local collection rebuild: the panel is one node, its content is what the selection changes. */
    private fun rebuildDetail() {
        heading.textContent = copy.detailHeading
        val kmpClass = selected?.let { KmpClasses.byNameOrNull(it) }
        if (kmpClass == null) {
            detailBody.replaceChildren(
                HtmlTagName.p.createElement {
                    addClass("hint")
                    inlineStyle.color = mutedText
                    textContent = copy.detailEmpty
                },
            )
            return
        }

        val list = HtmlTagName.ul.createElement {
            addClass("member")
            inlineStyle.apply {
                margin = Margin(0.px, 0.px)
                paddingLeft = 18.px
                fontSize = 13.px
            }
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
                memberLine("${if (property.isMutable) "var" else "val"} ${property.name}: ${property.type.simpleName}", property.isReadable),
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
            addClass("member")
            inlineStyle.apply {
                fontFamily = codeFamily
                marginBottom = 2.px
            }
            textContent = label
            appendChild(
                HtmlTagName.span.createElement {
                    inlineStyle.apply {
                        marginLeft = 8.px
                        padding = Padding(1.px, 7.px)
                        borderRadius = 999.px
                        fontSize = 11.px
                        backgroundColor = if (invokable) okFill else mutedFill
                        color = if (invokable) okText else mutedText
                    }
                    textContent = if (invokable) copy.invokable else copy.notInvokable
                },
            )
        }

    private fun headerCell(label: String): HTMLTableCellElement =
        HtmlTagName.th.createElement {
            styleCell()
            inlineStyle.apply {
                backgroundColor = panelStrong
                color = mutedText
                fontWeight = integer(600)
            }
            textContent = label
        }

    private fun cell(value: String = "", monospace: Boolean = false): HTMLTableCellElement =
        HtmlTagName.td.createElement {
            styleCell()
            if (monospace) {
                inlineStyle.fontFamily = codeFamily
            }
            textContent = value
        }

    private fun HTMLTableCellElement.styleCell() {
        inlineStyle.apply {
            textAlign = TextAlign.left
            verticalAlign = VerticalAlign.top
            padding = Padding(9.px, 12.px)
            borderBottomWidth = 1.px
            borderBottomStyle = LineStyle.solid
            borderBottomColor = hairline
            lineHeight = number(1.5)
        }
    }
}
