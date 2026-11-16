package io.github.shijianjs.kmp.reflect

/**
 * One parameter of a [KmpFunction] or [KmpConstructor].
 *
 * @property name the declared name, or `arg<index>` when the source has none (it always has one in
 * Kotlin, but hand-built metadata may not).
 * @property type the declared type.
 * @property index position in the parameter list, starting at `0`.
 * @property isVararg whether it is a `vararg` parameter; the value passed through
 * [KmpFunction.invoke] is then the whole array, not a single element.
 * @property hasDefault whether the declaration gives it a default value. Callers still have to pass
 * every parameter: the generated invocation code cannot express "use the default".
 * @property annotations annotations written on the parameter.
 */
public class KmpParameter(
    public val name: String,
    public val type: KmpTypeRef,
    public val index: Int,
    public val isVararg: Boolean = false,
    public val hasDefault: Boolean = false,
    public val annotations: List<KmpAnnotation> = emptyList(),
) {
    override fun toString(): String = buildString {
        if (isVararg) append("vararg ")
        append(name)
        append(": ")
        append(type)
    }
}
