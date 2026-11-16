package io.github.shijianjs.kmp.reflect

/**
 * The invocation capability of a [KmpFunction]: `(receiver, arguments) -> result`.
 *
 * Generated code inlines the actual call, so there is no reflective dispatch at runtime.
 */
public typealias KmpFunctionInvoker = (receiver: Any?, arguments: List<Any?>) -> Any?

/**
 * The invocation capability of a `suspend` [KmpFunction].
 *
 * A `suspend` function cannot be called from [KmpFunction.invoke]; this lambda is the entry point
 * [KmpFunction.invokeSuspend] uses, and it can only be called from a coroutine. The type comes from
 * the standard library, so supporting `suspend` does not add a coroutines dependency.
 */
public typealias KmpSuspendFunctionInvoker = suspend (receiver: Any?, arguments: List<Any?>) -> Any?

/**
 * A reflected member function.
 *
 * Invocation goes through [invoke]: the generated lambda casts the receiver to the declaring class
 * and performs a normal Kotlin call, which is why the receiver has to be an instance of the class
 * the metadata was generated for.
 *
 * @property name the declared name.
 * @property returnType the declared return type.
 * @property parameters parameters in declaration order; extension receivers are not reflected as
 * functions at all in v0.1.
 * @property typeParameters type parameters of the function itself.
 * @property modifiers declaration modifiers.
 * @property annotations annotations written on the function.
 * @property isInvokable `false` when the processor could not generate an invocation lambda for
 * [invoke] — a `suspend` function (use [invokeSuspend] instead), or one whose signature mentions a
 * type parameter. Both entry points then throw.
 */
public class KmpFunction(
    public val name: String,
    public val returnType: KmpTypeRef,
    public val parameters: List<KmpParameter> = emptyList(),
    public val typeParameters: List<KmpTypeParameter> = emptyList(),
    public val modifiers: Set<KmpModifier> = emptySet(),
    public val annotations: List<KmpAnnotation> = emptyList(),
    public val isInvokable: Boolean = true,
    private val invoker: KmpFunctionInvoker? = null,
    private val suspendInvoker: KmpSuspendFunctionInvoker? = null,
) {
    /** Whether the declaration is `suspend`. */
    public val isSuspend: Boolean
        get() = KmpModifier.SUSPEND in modifiers

    /** Whether [invokeSuspend] can call this function: it is `suspend` and its signature is closed. */
    public val isSuspendInvokable: Boolean
        get() = isSuspend && suspendInvoker != null

    /** Whether the declaration is `operator`. */
    public val isOperator: Boolean
        get() = KmpModifier.OPERATOR in modifiers

    /** The parameter with the given name, or `null` when the signature has none. */
    public fun parameter(name: String): KmpParameter? = parameters.firstOrNull { it.name == name }

    /** The fully qualified name of [returnType]. */
    public val returnTypeName: String
        get() = returnType.qualifiedName

    /**
     * Calls the function on [receiver] with [arguments].
     *
     * @throws KmpReflectException when the function is not invokable, when the argument count does
     * not match [parameters], or when the receiver is not an instance of the declaring class.
     */
    public fun invoke(receiver: Any?, vararg arguments: Any?): Any? =
        invoke(receiver, arguments.toList())

    /**
     * Calls the function on [receiver] with [arguments].
     *
     * @throws KmpReflectException when the function is not invokable or the argument count does not
     * match [parameters].
     */
    public fun invoke(receiver: Any?, arguments: List<Any?>): Any? {
        requireArgumentCount(arguments)
        val invoker = invoker ?: throw notInvokable("invokeSuspend")
        return invoker(receiver, arguments)
    }

    /**
     * Suspends the calling coroutine while running this `suspend` function on [receiver].
     *
     * `suspend` functions that suspend for real work exactly like a direct call: the coroutine
     * suspends at the point the generated lambda calls them.
     *
     * @throws KmpReflectException when the function is not `suspend`, is not invokable, or the
     * argument count does not match [parameters].
     */
    public suspend fun invokeSuspend(receiver: Any?, vararg arguments: Any?): Any? =
        invokeSuspend(receiver, arguments.toList())

    /**
     * Suspends the calling coroutine while running this `suspend` function on [receiver].
     *
     * @throws KmpReflectException when the function is not `suspend` or the argument count does not
     * match [parameters].
     */
    public suspend fun invokeSuspend(receiver: Any?, arguments: List<Any?>): Any? {
        requireArgumentCount(arguments)
        val invoker = suspendInvoker ?: throw notInvokable(if (isSuspend) null else "invoke")
        return invoker(receiver, arguments)
    }

    /**
     * Calls the function and casts the result to [T].
     *
     * @throws ClassCastException when the declared return type does not match [T].
     */
    public inline fun <reified T> invokeAs(receiver: Any?, vararg arguments: Any?): T =
        invoke(receiver, arguments.toList()) as T

    private fun requireArgumentCount(arguments: List<Any?>) {
        if (parameters.size != arguments.size) {
            throw KmpReflectException(
                "Function '$name' takes ${parameters.size} argument(s), got ${arguments.size}.",
            )
        }
    }

    /** [entryPoint] is the method the caller should use instead, when there is one. */
    private fun notInvokable(entryPoint: String?): KmpReflectException = KmpReflectException(
        "Function '$name' is not invokable: " +
            when {
                entryPoint != null -> "a suspend function has to be called through $entryPoint(...)."
                isSuspend -> "its signature mentions a type parameter, which the generated code cannot cast."
                else -> "its signature mentions a type parameter."
            },
    )

    override fun toString(): String =
        "$name(${parameters.joinToString(", ") { it.toString() }}): $returnType"
}

/** A reflected constructor. */
public class KmpConstructor(
    /** Parameters in declaration order. */
    public val parameters: List<KmpParameter> = emptyList(),
    /** Declaration modifiers; only `public` / `internal` / `protected` / `private` are meaningful. */
    public val modifiers: Set<KmpModifier> = emptySet(),
    /** Annotations written on the constructor. */
    public val annotations: List<KmpAnnotation> = emptyList(),
    /** `false` when no invocation lambda could be generated. */
    public val isInvokable: Boolean = true,
    private val invoker: ((arguments: List<Any?>) -> Any?)? = null,
) {
    /**
     * Creates an instance with [arguments].
     *
     * Every parameter has to be passed, including ones declared with a default value.
     *
     * @throws KmpReflectException when the constructor is not invokable or the argument count does
     * not match [parameters].
     */
    public fun new(vararg arguments: Any?): Any? {
        if (parameters.size != arguments.size) {
            throw KmpReflectException(
                "Constructor takes ${parameters.size} argument(s), got ${arguments.size}.",
            )
        }
        val invoker = invoker
            ?: throw KmpReflectException(
                "Constructor is not invokable: its signature mentions a type parameter.",
            )
        return invoker(arguments.toList())
    }

    /** Creates an instance and casts it to [T]. */
    public inline fun <reified T> newInstance(vararg arguments: Any?): T = new(arguments = arguments) as T

    override fun toString(): String = "(${parameters.joinToString(", ") { it.toString() }})"
}
