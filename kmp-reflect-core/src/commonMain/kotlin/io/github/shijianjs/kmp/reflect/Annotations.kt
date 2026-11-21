package io.github.shijianjs.kmp.reflect

/**
 * Marks a Kotlin Multiplatform declaration as reflected by `kmp-reflect`.
 *
 * The KSP processor reads this annotation at compile time and emits the declaration's metadata as
 * ordinary Kotlin source (a [KmpClass] built from inlined lambdas). Nothing is loaded at runtime: an
 * annotated class either has its metadata compiled in, or the build fails to see the annotation.
 *
 * Applying it to a class covers its *declared* members; nested classes are only reflected when they
 * carry the annotation themselves. Applying it to an interface additionally generates a dynamic
 * proxy class (see [io.github.shijianjs.kmp.reflect.proxy.KmpProxy]).
 *
 * @see KmpReflectable for the marker-interface form of the same opt-in.
 * @see KmpReflectIgnore for excluding a single member.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class KmpReflect(
    /**
     * Whether members compiled as `internal` are reflected too. Set to `false` to keep only
     * `public` members.
     *
     * `private` and `protected` members are never reflected: the generated metadata is a top-level
     * file outside the class, so a call to either would not compile.
     */
    val includeNonPublic: Boolean = true,
)

/**
 * Excludes a declaration from the generated metadata.
 *
 * Applies to classes (the whole class is skipped), functions, properties and constructors. It wins
 * over [KmpReflect] and over [KmpReflectable].
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
@Retention(AnnotationRetention.BINARY)
public annotation class KmpReflectIgnore

/**
 * Marker interface: any class implementing it is reflected without a [KmpReflect] annotation.
 *
 * The processor finds implementors by walking the module's own source files, so the interface has to
 * be reachable from the compiled module's sources (directly or through another local interface).
 * Implementations coming from a dependency's klib are not discovered; annotate those with
 * [KmpReflect] in the module that owns them.
 */
public interface KmpReflectable
