package io.github.shijianjs.kmp.reflect

import kotlin.reflect.KClass

/**
 * A type as it appears in reflected metadata: a class name plus its arguments and nullability.
 *
 * `kotlin.reflect.KType` is not available on every KMP target, and generic arguments are erased at
 * runtime anyway, so the processor records the written type here instead of handing out a
 * `KClass<*>`.
 *
 * @property qualifiedName fully qualified name of the class, or the type parameter's name for
 * [KmpTypeKind.TYPE_PARAMETER] references.
 * @property simpleName the last segment of [qualifiedName].
 * @property kind category of the referenced declaration.
 * @property nullable whether the type was written with a trailing `?`.
 * @property arguments type arguments, in declaration order; empty for non-generic types.
 * @property variance the projection this reference was written with, when it is an argument.
 * @property runtime the runtime class, when the processor could resolve one; whoever builds metadata
 * by hand may leave it `null`, in which case [isInstance] answers `true`.
 */
public class KmpTypeRef(
    public val qualifiedName: String,
    public val simpleName: String = qualifiedName.substringAfterLast('.'),
    public val kind: KmpTypeKind = KmpTypeKind.CLASS,
    public val nullable: Boolean = false,
    public val arguments: List<KmpTypeRef> = emptyList(),
    public val variance: KmpVariance = KmpVariance.INVARIANT,
    private val runtime: KClass<*>? = null,
) {
    /** The runtime class backing this reference, or `null` when the processor could not resolve one. */
    public val kclass: KClass<*>?
        get() = runtime

    /** [kclass] for inlinable call sites that have no name to compare against. */
    @PublishedApi
    internal val runtimeKClass: KClass<*>?
        get() = runtime

    /**
     * Whether [value] is an instance of this type.
     *
     * Generic arguments are erased, so `List<String>` accepts any list. A `null` [value] is only
     * accepted by a [nullable] reference. When no runtime class was recorded the answer is `true`,
     * because the metadata cannot prove otherwise.
     */
    public fun isInstance(value: Any?): Boolean {
        if (value == null) return nullable
        val runtime = runtime ?: return true
        return runtime.isInstance(value)
    }

    /** This type with the given nullability, keeping every other property. */
    public fun withNullability(nullable: Boolean): KmpTypeRef =
        if (nullable == this.nullable) {
            this
        } else {
            KmpTypeRef(qualifiedName, simpleName, kind, nullable, arguments, variance, runtime)
        }

    override fun toString(): String = buildString {
        append(qualifiedName)
        if (arguments.isNotEmpty()) {
            append('<')
            arguments.forEachIndexed { index, argument ->
                if (index > 0) append(", ")
                when (argument.variance) {
                    KmpVariance.INVARIANT -> Unit
                    KmpVariance.IN -> append("in ")
                    KmpVariance.OUT -> append("out ")
                }
                append(argument)
            }
            append('>')
        }
        if (nullable) append('?')
    }
}

/** A type parameter of a reflected class or function (`class Box<T : Any>`). */
public class KmpTypeParameter(
    /** The parameter's name, as written (`T`). */
    public val name: String,
    /** Upper bounds, in order; `Any?` when the declaration writes none. */
    public val bounds: List<KmpTypeRef> = emptyList(),
    /** Whether the bound is nullable (`T : Any?`). */
    public val nullable: Boolean = false,
    /** Whether the parameter is `reified` (inline functions only). */
    public val reified: Boolean = false,
) {
    override fun toString(): String = if (bounds.isEmpty()) name else "$name : ${bounds.joinToString(" & ")}"
}
