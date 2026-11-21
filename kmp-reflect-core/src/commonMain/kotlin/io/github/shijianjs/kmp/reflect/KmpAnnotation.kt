package io.github.shijianjs.kmp.reflect

import kotlin.reflect.KClass

/**
 * One annotation occurrence on a reflected declaration, with its arguments already evaluated.
 *
 * Arguments are stored as plain values so the model works on every platform:
 *
 * | argument kind | stored as |
 * | --- | --- |
 * | `String`, `Int`, `Long`, `Short`, `Byte`, `Float`, `Double`, `Boolean`, `Char` | the value itself |
 * | `KClass<*>` | the [KClass] reference |
 * | enum entry | the enum entry instance (`Color.RED`) |
 * | nested annotation | a [KmpAnnotation] |
 * | array of any of the above | a `List<Any?>`, in source order |
 *
 * The annotation *instance* is not materialized: Kotlin only supports instantiating annotation
 * classes on the JVM, and this project has to behave the same way on JS. Compare by [type] and read
 * [arguments] instead — or use the typed helpers on a [KmpClass].
 */
public class KmpAnnotation(
    /** The annotation class this occurrence refers to. */
    public val type: KmpTypeRef,
    /**
     * Arguments by name, in declaration order. Values the annotation class declares defaults for
     * are present even when the source did not write them, so `arguments` mirrors what the
     * annotation would answer at runtime rather than the literal text.
     */
    public val arguments: Map<String, Any?> = emptyMap(),
) {
    /** The fully qualified name of the annotation class. */
    public val qualifiedName: String
        get() = type.qualifiedName

    /** The argument with the given name, or `null` when it was not written. */
    public operator fun get(name: String): Any? = arguments[name]

    /** The named argument cast to [String], or `null` when absent or of another type. */
    public fun string(name: String): String? = arguments[name] as? String

    /** The named argument cast to [Int], or `null` when absent or of another type. */
    public fun int(name: String): Int? = arguments[name] as? Int

    /** The named argument cast to [Long], or `null` when absent or of another type. */
    public fun long(name: String): Long? = arguments[name] as? Long

    /** The named argument cast to [Boolean], or `null` when absent or of another type. */
    public fun boolean(name: String): Boolean? = arguments[name] as? Boolean

    /** The named argument taken as a nested annotation, or `null` when absent or of another type. */
    public fun annotation(name: String): KmpAnnotation? = arguments[name] as? KmpAnnotation

    /** The named argument taken as a class reference, or `null` when absent or of another type. */
    public fun kclass(name: String): KClass<*>? = arguments[name] as? KClass<*>

    override fun toString(): String =
        if (arguments.isEmpty()) "@$qualifiedName" else "@$qualifiedName($arguments)"
}

/** Returns the annotation of type [A] on this declaration, or `null` when it is not present. */
public inline fun <reified A : Annotation> List<KmpAnnotation>.find(): KmpAnnotation? =
    firstOrNull { annotation -> annotation.type.matches(A::class, kmpReflectiveName(A::class)) }

/** Returns every annotation on this declaration whose class is [A]. */
public inline fun <reified A : Annotation> List<KmpAnnotation>.filterByType(): List<KmpAnnotation> =
    filter { annotation -> annotation.type.matches(A::class, kmpReflectiveName(A::class)) }
