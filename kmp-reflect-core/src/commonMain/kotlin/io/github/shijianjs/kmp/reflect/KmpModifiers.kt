package io.github.shijianjs.kmp.reflect

/**
 * The kind of declaration a [KmpClass] describes.
 *
 * Mirrors Kotlin's own `ClassKind`, with an extra entry for companion objects: they are objects at
 * runtime, but callers usually want to tell a companion apart from a plain singleton.
 */
public enum class KmpKind {
    /** A regular class, including `data`, `value`, `sealed` and `inner` ones. */
    CLASS,

    /** An interface, including `fun interface` ones. */
    INTERFACE,

    /** An `object` declaration. */
    OBJECT,

    /** The `companion object` of a class or interface. */
    COMPANION_OBJECT,

    /** An `enum class`. */
    ENUM_CLASS,

    /** An `annotation class`. */
    ANNOTATION_CLASS,
}

/** Kotlin visibility, as recorded for a reflected declaration. */
public enum class KmpVisibility {
    /** `public`. */
    PUBLIC,

    /** `internal`. */
    INTERNAL,

    /** `protected`. */
    PROTECTED,

    /** `private`. */
    PRIVATE,
}

/**
 * Declaration modifiers, as recorded for a reflected declaration.
 *
 * One enum covers classes, functions and properties: a modifier that does not apply to a given
 * declaration simply never shows up in its set, which keeps the API to one type instead of four.
 */
public enum class KmpModifier {
    /** `final`. */
    FINAL,

    /** `open`. */
    OPEN,

    /** `abstract`. */
    ABSTRACT,

    /** `sealed`. */
    SEALED,

    /** `data` (classes only). */
    DATA,

    /** `enum` (classes only). */
    ENUM,

    /** `annotation` (classes only). */
    ANNOTATION,

    /** `inner` (classes only). */
    INNER,

    /** `value` (classes only). */
    VALUE,

    /** `fun` (interfaces only). */
    FUN,

    /** `inline` (functions and classes). */
    INLINE,

    /** `expect`. */
    EXPECT,

    /** `actual`. */
    ACTUAL,

    /** `external`. */
    EXTERNAL,

    /** `const` (properties only). */
    CONST,

    /** `lateinit` (properties only). */
    LATEINIT,

    /** `suspend` (functions only). */
    SUSPEND,

    /** `operator` (functions only). */
    OPERATOR,

    /** `infix` (functions only). */
    INFIX,

    /** `tailrec` (functions only). */
    TAILREC,

    /** `override` (members only). */
    OVERRIDE,

    /** `reified` (type parameters only). */
    REIFIED,
}

/**
 * The category of a type reference, used to tell a concrete class apart from a type parameter or a
 * star projection when the reference carries no name of its own.
 */
public enum class KmpTypeKind {
    /** A concrete class. */
    CLASS,

    /** An interface. */
    INTERFACE,

    /** An `object`. */
    OBJECT,

    /** An `enum class`. */
    ENUM_CLASS,

    /** An `annotation class`. */
    ANNOTATION_CLASS,

    /** A type parameter of the declaring class or function (`T`). */
    TYPE_PARAMETER,

    /** A star projection (`*`). */
    STAR,

    /** Anything the processor could not resolve (error types, unsupported constructs). */
    UNKNOWN,
}

/** Variance of a type argument, as written at the use site (`List<out E>`). */
public enum class KmpVariance {
    /** No projection: `List<String>`. */
    INVARIANT,

    /** `in` projection: `Comparable<in String>`. */
    IN,

    /** `out` projection: `List<out String>`. */
    OUT,
}
