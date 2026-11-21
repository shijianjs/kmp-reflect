package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect

/**
 * A class with members the generated file cannot reach.
 *
 * Generated metadata is a top-level file outside the class, so `protected` and `private` members are
 * dropped instead of reflected; `internal` ones stay, because that file is in the same module. Also
 * carries the `vararg` invocation case.
 */
@KmpReflect
public open class Scoped public constructor(public val label: String) {
    /** A protected secondary constructor: not reflected, the generated file could not call it. */
    protected constructor(label: String, order: Int) : this(label + order)

    @Suppress("unused")
    private val cache: String = label

    /** A public property with a protected setter: readable, not writable from another file. */
    public var counter: Int = 0
        protected set

    protected fun hidden(): String = "protected"

    @Suppress("unused")
    private fun secret(): String = cache

    internal fun internalThing(): String = "internal"

    public fun shown(): String = "public"

    public fun join(vararg parts: String): String = parts.joinToString("-")
}
