package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect
import io.github.shijianjs.kmp.reflect.KmpReflectIgnore
import io.github.shijianjs.kmp.reflect.KmpReflectable

/**
 * A demo annotation: it exists so the page and the tests can show annotation scanning.
 *
 * Retention is `BINARY` — the processor reads it from the sources, and nothing needs it at runtime.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class KmpRecord(
    val name: String,
    val tags: Array<String> = [],
    val priority: Int = 0,
)

/** The main demo class: a data class with a companion, private state and an excluded member. */
@KmpRecord(name = "person", tags = ["sample", "user"], priority = 3)
@KmpReflect
public data class Person(
    val name: String,
    var age: Int = 0,
    private val secret: String = "hidden",
) {
    /** A derived property: only a getter exists, so metadata marks it read-only. */
    @KmpRecord(name = "greeting", priority = 1)
    public val greeting: String
        get() = "Hi, I'm $name"

    /** A function with a default argument; callers still have to pass every argument. */
    public fun greet(prefix: String = "Hello"): String = "$prefix, $name!"

    /** Mutates state, so a call is visible through the reflected property afterwards. */
    public fun birthday(): Int {
        age += 1
        return age
    }

    @KmpReflectIgnore
    public fun peekSecret(): String = secret

    public companion object {
        public const val SPECIES: String = "human"

        public fun create(name: String): Person = Person(name)
    }
}

/** A `fun interface`: reflected metadata plus a generated dynamic proxy. */
@KmpReflect
public fun interface Greeter {
    public fun greet(name: String): String
}

/** An enum with a constructor property. */
@KmpRecord(name = "shape", tags = ["geometry"])
@KmpReflect
public enum class Shape(public val sides: Int) {
    CIRCLE(0),
    TRIANGLE(3),
    SQUARE(4),
}

/** Opts in through the marker interface instead of the annotation, and is an `object`. */
public object Toolbox : KmpReflectable {
    public const val VERSION: String = "1.0"

    public fun hello(): String = "hello from the marker interface"
}

/** A reflected base class, so `Calculator` shows a superclass linked to its own metadata. */
@KmpReflect
public open class Machine {
    public open fun power(): Int = 1
}

/** Exercises members the processor records but cannot invoke. */
@KmpReflect
public open class Calculator : Machine() {
    public var lastResult: Int = 0
        private set

    public open fun add(left: Int, right: Int): Int = (left + right).also { lastResult = it }

    public operator fun plus(other: Calculator): Calculator = Calculator()

    public suspend fun slowAdd(left: Int, right: Int): Int = left + right

    public fun <T> identity(value: T): T = value
}

/** A class with a nested reflected class, so the page can show nesting. */
@KmpReflect
public class Library(public val name: String) {
    @KmpRecord(name = "shelf", tags = ["nested"])
    @KmpReflect
    public class Shelf(public val books: Int)

    public fun open(): String = "$name is open"
}
