package io.github.shijianjs.kmp.reflect.ksp

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName

/**
 * A type as the generator needs it: both the data to put into a `KmpTypeRef` and the KotlinPoet
 * type to write in a cast.
 *
 * Generic arguments are kept (they are metadata), but casts use the erased class with invariant
 * arguments — a projection cannot appear in an `as` expression.
 */
internal class TypeModel(
    val qualifiedName: String,
    val simpleName: String,
    /** `KmpTypeKind` constant name. */
    val kind: String,
    val nullable: Boolean,
    val arguments: List<TypeModel>,
    /** `KmpVariance` constant name of the projection this reference was written with. */
    val variance: String,
    val className: ClassName?,
    val isTypeParameter: Boolean,
    val isStar: Boolean,
) {
    /** `true` when this reference mentions a type parameter somewhere below itself. */
    val usesTypeParameter: Boolean
        get() = isTypeParameter || arguments.any { it.usesTypeParameter }

    /** The type to write in a cast, or `null` when the type cannot be named in generated code. */
    fun castTypeName(): TypeName? {
        if (isTypeParameter || isStar) return null
        val base = className ?: return null
        val type = if (arguments.isEmpty()) base else base.parameterizedBy(arguments.map { it.castArgumentTypeName() })
        return if (nullable) type.copy(nullable = true) else type
    }

    private fun castArgumentTypeName(): TypeName = castTypeName() ?: ANY.copy(nullable = true)

    companion object {
        /** `Any?`, used wherever a signature cannot be expressed. */
        val ANY_NULLABLE: TypeModel = TypeModel(
            qualifiedName = "kotlin.Any",
            simpleName = "Any",
            kind = "UNKNOWN",
            nullable = true,
            arguments = emptyList(),
            variance = "INVARIANT",
            className = ANY,
            isTypeParameter = false,
            isStar = false,
        )

        /** A star projection (`*`). */
        val STAR_PROJECTION: TypeModel = TypeModel(
            qualifiedName = "kotlin.Any",
            simpleName = "*",
            kind = "STAR",
            nullable = true,
            arguments = emptyList(),
            variance = "INVARIANT",
            className = null,
            isTypeParameter = false,
            isStar = true,
        )
    }
}

/** One annotation occurrence, ready to be written as a `KmpAnnotation` construction. */
internal class AnnotationModel(
    val type: TypeModel,
    val arguments: List<Pair<String, AnnotationArgument>>,
)

/** One annotation argument value. */
internal sealed interface AnnotationArgument {
    /** A string literal. */
    data class Text(val value: String) : AnnotationArgument

    /** An integer literal, already rendered (`42`, `7L`). */
    data class Number(val literal: String) : AnnotationArgument

    /** A boolean literal. */
    data class Flag(val value: Boolean) : AnnotationArgument

    /** A char literal. */
    data class Character(val value: Char) : AnnotationArgument

    /** A `KClass` literal. */
    data class ClassReference(val className: ClassName) : AnnotationArgument

    /** An enum entry. */
    data class EnumReference(val enumClassName: ClassName, val entryName: String) : AnnotationArgument

    /** A nested annotation. */
    data class Nested(val annotation: AnnotationModel) : AnnotationArgument

    /** An array argument. */
    data class ArrayOf(val values: List<AnnotationArgument>) : AnnotationArgument

    /** A value the generator cannot express; written as `null` and reported to the build log. */
    data class Unsupported(val description: String) : AnnotationArgument
}

/** One reflected parameter. */
internal class ParameterModel(
    val name: String,
    val type: TypeModel,
    val index: Int,
    val isVararg: Boolean,
    val hasDefault: Boolean,
    val annotations: List<AnnotationModel>,
)

/** One reflected type parameter. */
internal class TypeParameterModel(
    val name: String,
    val bounds: List<TypeModel>,
    val reified: Boolean,
)

/** One reflected member function. */
internal class FunctionModel(
    val name: String,
    val returnType: TypeModel,
    val parameters: List<ParameterModel>,
    val typeParameters: List<TypeParameterModel>,
    /** `KmpModifier` constant names. */
    val modifiers: List<String>,
    val annotations: List<AnnotationModel>,
    val isSuspend: Boolean,
    val isAbstract: Boolean,
    val isExtension: Boolean,
    /** `false` when no invocation lambda can be generated. */
    val invokable: Boolean,
)

/** One reflected property. */
internal class PropertyModel(
    val name: String,
    val type: TypeModel,
    val mutable: Boolean,
    val modifiers: List<String>,
    val annotations: List<AnnotationModel>,
    val readable: Boolean,
    val writable: Boolean,
)

/** One reflected constructor. */
internal class ConstructorModel(
    val parameters: List<ParameterModel>,
    val modifiers: List<String>,
    val annotations: List<AnnotationModel>,
    val invokable: Boolean,
)

/** One reflected enum entry. */
internal class EnumEntryModel(
    val name: String,
    val ordinal: Int,
    val annotations: List<AnnotationModel>,
)

/** Everything the generator knows about one reflected class. */
internal class ClassModel(
    val qualifiedName: String,
    val simpleName: String,
    val packageName: String,
    val className: ClassName,
    /** `KmpKind` constant name. */
    val kind: String,
    /** `KmpVisibility` constant name. */
    val visibility: String,
    val modifiers: List<String>,
    val annotations: List<AnnotationModel>,
    val typeParameters: List<TypeParameterModel>,
    val supertypes: List<TypeModel>,
    val constructors: List<ConstructorModel>,
    val functions: List<FunctionModel>,
    val properties: List<PropertyModel>,
    val enumEntries: List<EnumEntryModel>,
    val companionQualifiedName: String?,
    val nestedQualifiedNames: List<String>,
    val hasObjectInstance: Boolean,
    val isInterface: Boolean,
    /** Set by the processor once proxy generation is decided; read by the metadata writer. */
    var proxyable: Boolean = false,
) {
    /** The name of the top-level value this class's metadata is generated into. */
    val valueName: String = metadataValueName(qualifiedName)
}
