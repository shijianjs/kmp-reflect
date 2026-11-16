package io.github.shijianjs.kmp.reflect

/**
 * A reflected property: a `val` / `var` declaration together with its accessors.
 *
 * Only declared properties (backing-field or accessor) are reflected; extension properties are not
 * functions and have no receiver to call them on, so they are out of scope in v0.1.
 *
 * @property name the declared name.
 * @property type the declared type.
 * @property isMutable whether the declaration is a `var`.
 * @property modifiers declaration modifiers.
 * @property annotations annotations written on the property.
 * @property isReadable `false` when no getter lambda could be generated.
 * @property isWritable `false` for a `val`, and for a `var` whose setter could not be generated.
 */
public class KmpProperty(
    public val name: String,
    public val type: KmpTypeRef,
    public val isMutable: Boolean = false,
    public val modifiers: Set<KmpModifier> = emptySet(),
    public val annotations: List<KmpAnnotation> = emptyList(),
    public val isReadable: Boolean = true,
    public val isWritable: Boolean = false,
    private val getter: ((receiver: Any?) -> Any?)? = null,
    private val setter: ((receiver: Any?, value: Any?) -> Unit)? = null,
) {
    /** Whether the property is `lateinit`. */
    public val isLateinit: Boolean
        get() = KmpModifier.LATEINIT in modifiers

    /** Whether the property is `const`. */
    public val isConst: Boolean
        get() = KmpModifier.CONST in modifiers

    /**
     * Reads the property from [receiver].
     *
     * @throws KmpReflectException when the property has no generated getter.
     */
    public fun get(receiver: Any?): Any? {
        val getter = getter
            ?: throw KmpReflectException("Property '$name' is not readable.")
        return getter(receiver)
    }

    /** Reads the property and casts it to [T]. */
    public inline fun <reified T> getAs(receiver: Any?): T = get(receiver) as T

    /**
     * Writes [value] to the property on [receiver].
     *
     * @throws KmpReflectException when the property is not writable.
     */
    public fun set(receiver: Any?, value: Any?) {
        if (!isMutable) {
            throw KmpReflectException("Property '$name' is a val and cannot be written.")
        }
        val setter = setter
            ?: throw KmpReflectException(
                "Property '$name' has no generated setter: its signature mentions a type parameter.",
            )
        setter(receiver, value)
    }

    override fun toString(): String = (if (isMutable) "var $name: " else "val $name: ") + type
}

/** One entry of a reflected `enum class`. */
public class KmpEnumEntry(
    /** The entry's name. */
    public val name: String,
    /** Position in declaration order, starting at `0`. */
    public val ordinal: Int,
    /** Annotations written on the entry. */
    public val annotations: List<KmpAnnotation> = emptyList(),
    /** The entry instance itself, or `null` for hand-built metadata. */
    public val instance: Any? = null,
) {
    override fun toString(): String = name
}
