package io.github.shijianjs.kmp.reflect

import io.github.shijianjs.kmp.reflect.proxy.KmpInvocationHandler
import kotlin.reflect.KClass

/**
 * Reflected metadata for one Kotlin class, interface, object or enum.
 *
 * Instances are produced by the KSP processor at compile time and registered through
 * [KmpClasses.register]; they are never obtained by loading a class at runtime. Everything a JVM
 * `Class` object would answer — name, modifiers, annotations, supertypes, members — is a plain
 * property here, and every member carries an inlined invocation lambda.
 *
 * @property qualifiedName the binary-ish name the processor recorded, e.g. `com.example.Person` or
 * `com.example.Outer.Inner`. Nested classes use dots, exactly like `KClass.qualifiedName`.
 * @property simpleName the last segment of [qualifiedName].
 * @property packageName the package of the outermost declaration.
 * @property kind what was declared.
 * @property visibility the declaration's visibility.
 * @property modifiers the declaration's modifiers.
 * @property annotations annotations written on the declaration.
 * @property typeParameters the declaration's type parameters, in order.
 * @property supertypes directly declared supertypes, in source order.
 * @property superclasses metadata of the declared supertypes that were reflected in the same module,
 * in source order. A supertype from another module is only described by [supertypes], because its
 * generated metadata is `internal` there; use [KmpClasses.implementing] for a name-based check.
 * @property constructors declared constructors.
 * @property functions declared member functions (including property accessors? no — accessors stay
 * inside [properties]).
 * @property properties declared properties.
 * @property enumEntries entries of an `enum class`; empty otherwise.
 * @property companion the companion object's metadata, if the declaration has one.
 * @property nestedClasses nested classes that were reflected themselves.
 * @property objectInstance the singleton instance for `object` / `companion object`, `null`
 * otherwise.
 * @property runtime the runtime class reference, or `null` for hand-built metadata.
 */
public class KmpClass(
    public val qualifiedName: String,
    public val simpleName: String = qualifiedName.substringAfterLast('.'),
    public val packageName: String = qualifiedName.substringBeforeLast('.', ""),
    public val kind: KmpKind = KmpKind.CLASS,
    public val visibility: KmpVisibility = KmpVisibility.PUBLIC,
    public val modifiers: Set<KmpModifier> = emptySet(),
    public val annotations: List<KmpAnnotation> = emptyList(),
    public val typeParameters: List<KmpTypeParameter> = emptyList(),
    public val supertypes: List<KmpTypeRef> = emptyList(),
    public val superclasses: List<KmpClass> = emptyList(),
    public val constructors: List<KmpConstructor> = emptyList(),
    public val functions: List<KmpFunction> = emptyList(),
    public val properties: List<KmpProperty> = emptyList(),
    public val enumEntries: List<KmpEnumEntry> = emptyList(),
    public val companion: KmpClass? = null,
    public val nestedClasses: List<KmpClass> = emptyList(),
    public val objectInstance: Any? = null,
    private val runtime: KClass<*>? = null,
    private val proxyFactory: ((KmpInvocationHandler) -> Any)? = null,
) {
    /** The runtime class reference, or `null` when it was not recorded. */
    public val kclass: KClass<*>?
        get() = runtime

    /** [kclass] for the identity lookup in [KmpClasses.of]. */
    @PublishedApi
    internal val runtimeKClass: KClass<*>?
        get() = runtime

    /** Whether this describes an interface. */
    public val isInterface: Boolean
        get() = kind == KmpKind.INTERFACE

    /** Whether this describes an object or a companion object. */
    public val isObject: Boolean
        get() = kind == KmpKind.OBJECT || kind == KmpKind.COMPANION_OBJECT

    /** Whether this describes an `enum class`. */
    public val isEnum: Boolean
        get() = kind == KmpKind.ENUM_CLASS

    /** Whether the declaration is `abstract`. */
    public val isAbstract: Boolean
        get() = KmpModifier.ABSTRACT in modifiers

    /** Whether the declaration is `data`. */
    public val isData: Boolean
        get() = KmpModifier.DATA in modifiers

    /** Whether a dynamic proxy can be created for this declaration. */
    public val isProxyable: Boolean
        get() = proxyFactory != null

    /** Whether [value] is an instance of this class. */
    public fun isInstance(value: Any?): Boolean = runtime?.isInstance(value) ?: false

    /**
     * Every reflected superclass reachable from this class, nearest first, each name once.
     *
     * Stops at the first supertype that was not reflected, so a chain that leaves the module is
     * reported as the reflected prefix.
     */
    public val allSuperclasses: List<KmpClass>
        get() {
            val seen = LinkedHashMap<String, KmpClass>()
            val pending = ArrayDeque(superclasses)
            while (pending.isNotEmpty()) {
                val next = pending.removeFirst()
                if (next.qualifiedName == qualifiedName) continue
                if (seen.containsKey(next.qualifiedName)) continue
                seen[next.qualifiedName] = next
                pending.addAll(next.superclasses)
            }
            return seen.values.toList()
        }

    /** Functions with the given name, in declaration order. */
    public fun functionsNamed(name: String): List<KmpFunction> = functions.filter { it.name == name }

    /** The first function with the given name, and with a parameter list of the given arity when [arity] is given. */
    public fun function(name: String, arity: Int? = null): KmpFunction? = functions.firstOrNull {
        it.name == name && (arity == null || it.parameters.size == arity)
    }

    /** The property with the given name, or `null` when it is not reflected. */
    public fun property(name: String): KmpProperty? = properties.firstOrNull { it.name == name }

    /** The constructor whose parameter count matches [arity], or the first one when [arity] is `null`. */
    public fun constructor(arity: Int? = null): KmpConstructor? = constructors.firstOrNull {
        arity == null || it.parameters.size == arity
    }

    /** The enum entry with the given name, or `null` when this is not an enum or the name is unknown. */
    public fun enumEntry(name: String): KmpEnumEntry? = enumEntries.firstOrNull { it.name == name }

    /** The annotation with the given fully qualified class name, or `null` when it is not present. */
    public fun annotation(qualifiedName: String): KmpAnnotation? =
        annotations.firstOrNull { it.qualifiedName == qualifiedName }

    /** The annotation of type [A], or `null` when it is not present. */
    public inline fun <reified A : Annotation> annotationOf(): KmpAnnotation? =
        annotations.firstOrNull { annotation -> annotation.type.matches(A::class, kmpReflectiveName(A::class)) }

    /**
     * Creates a dynamic proxy for this interface.
     *
     * @throws KmpReflectException when no proxy class was generated, which happens for anything that
     * is not an interface, or for an interface whose members are all unsupported.
     */
    public fun newProxy(handler: KmpInvocationHandler): Any {
        val factory = proxyFactory
            ?: throw KmpReflectException(
                "No proxy class was generated for '$qualifiedName'. Proxies are only generated for " +
                    "@KmpReflect interfaces with at least one plain member function.",
            )
        return factory(handler)
    }

    override fun toString(): String = when (kind) {
        KmpKind.CLASS -> "class $qualifiedName"
        KmpKind.INTERFACE -> "interface $qualifiedName"
        KmpKind.OBJECT -> "object $qualifiedName"
        KmpKind.COMPANION_OBJECT -> "companion object $qualifiedName"
        KmpKind.ENUM_CLASS -> "enum class $qualifiedName"
        KmpKind.ANNOTATION_CLASS -> "annotation class $qualifiedName"
    }
}
