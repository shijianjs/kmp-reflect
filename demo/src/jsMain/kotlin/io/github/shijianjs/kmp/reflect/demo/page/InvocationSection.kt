package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.kmp.reflect.KmpClass
import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.model.Person
import web.html.HTMLElement

/**
 * Constructs a `Person` and calls its members through the metadata, once, at start-up.
 *
 * The values are fixed by the time the page is drawn; only their labels depend on the language, so
 * [applyCopy] rewrites the lines instead of repeating the reflective calls.
 */
internal class InvocationSection {
    private val shell = SectionShell()

    val root: HTMLElement = shell.root

    private val list = OutputList()

    /** `null` when the metadata is not registered, which also means there is nothing to show. */
    private val constructed: String?
    private val greetResult: String?
    private val ageAfterBirthday: String?

    init {
        val personClass: KmpClass? = KmpClasses.byNameOrNull("io.github.shijianjs.kmp.reflect.demo.model.Person")
        val person = personClass?.constructor(3)?.new("Ada", 36, "hidden") as? Person
        if (person == null) {
            constructed = null
            greetResult = null
            ageAfterBirthday = null
        } else {
            constructed = "${person.name} (${person.age})"
            greetResult = personClass.function("greet", 1)?.invoke(person, "Hello") as? String
            val birthday = personClass.function("birthday", 0)?.invoke(person)
            // `age` is read back through its getter: the call above went through `birthday`.
            ageAfterBirthday = "${personClass.property("age")?.get(person)} (birthday -> $birthday)"
        }
        shell.body.appendChild(list.element)
        applyCopy(english)
    }

    fun applyCopy(copy: Copy) {
        shell.setCopy(copy.invocationHeading, copy.invocationHint)
        list.setLines(
            listOfNotNull(
                constructed?.let { value -> "${copy.constructed}: $value" } ?: copy.detailEmpty,
                greetResult?.let { value -> "${copy.called}: $value" },
                ageAfterBirthday?.let { value -> "${copy.propertyValue} age: $value" },
            ),
        )
    }
}
