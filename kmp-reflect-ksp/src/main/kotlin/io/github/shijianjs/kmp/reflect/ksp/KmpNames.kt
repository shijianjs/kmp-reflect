package io.github.shijianjs.kmp.reflect.ksp

import com.squareup.kotlinpoet.ClassName

/**
 * Names the generated code has to refer to. Kept in one place so a rename in the runtime module is a
 * single-line change here.
 */
internal object KmpNames {
    const val PACKAGE = "io.github.shijianjs.kmp.reflect"

    const val KMP_REFLECT = "$PACKAGE.KmpReflect"
    const val KMP_REFLECT_IGNORE = "$PACKAGE.KmpReflectIgnore"
    const val KMP_REFLECTABLE = "$PACKAGE.KmpReflectable"

    val KMP_CLASS: ClassName = ClassName(PACKAGE, "KmpClass")
    val KMP_TYPE_REF: ClassName = ClassName(PACKAGE, "KmpTypeRef")
    val KMP_TYPE_KIND: ClassName = ClassName(PACKAGE, "KmpTypeKind")
    val KMP_VARIANCE: ClassName = ClassName(PACKAGE, "KmpVariance")
    val KMP_KIND: ClassName = ClassName(PACKAGE, "KmpKind")
    val KMP_VISIBILITY: ClassName = ClassName(PACKAGE, "KmpVisibility")
    val KMP_MODIFIER: ClassName = ClassName(PACKAGE, "KmpModifier")
    val KMP_TYPE_PARAMETER: ClassName = ClassName(PACKAGE, "KmpTypeParameter")
    val KMP_ANNOTATION: ClassName = ClassName(PACKAGE, "KmpAnnotation")
    val KMP_PARAMETER: ClassName = ClassName(PACKAGE, "KmpParameter")
    val KMP_FUNCTION: ClassName = ClassName(PACKAGE, "KmpFunction")
    val KMP_PROPERTY: ClassName = ClassName(PACKAGE, "KmpProperty")
    val KMP_CONSTRUCTOR: ClassName = ClassName(PACKAGE, "KmpConstructor")
    val KMP_ENUM_ENTRY: ClassName = ClassName(PACKAGE, "KmpEnumEntry")
    val KMP_REFLECT_MODULE: ClassName = ClassName(PACKAGE, "KmpReflectModule")
    val KMP_INVOCATION_HANDLER: ClassName = ClassName("$PACKAGE.proxy", "KmpInvocationHandler")
    val KMP_RESOURCE: ClassName = ClassName("$PACKAGE.resource", "KmpResource")

    /** File-level annotation that keeps generated lambdas quiet about the casts they are born with. */
    val SUPPRESS: ClassName = ClassName("kotlin", "Suppress")
    val OPT_IN: ClassName = ClassName("kotlin", "OptIn")
    val EXPERIMENTAL_ENCODING_API: ClassName = ClassName("kotlin.io.encoding", "ExperimentalEncodingApi")
    val BASE64: ClassName = ClassName("kotlin.io.encoding", "Base64")
}

/** The mangled identifier of the top-level `KmpClass` value generated for [qualifiedName]. */
internal fun metadataValueName(qualifiedName: String): String =
    "kmpClass_" + qualifiedName.replace('.', '_').replace('$', '_')

/** The mangled class name of the generated proxy for the interface [qualifiedName]. */
internal fun proxyClassName(qualifiedName: String): String =
    "KmpProxy_" + qualifiedName.replace('.', '_').replace('$', '_')

/** The file name that carries a reflected class, without the `.kt` suffix. */
internal fun metadataFileName(qualifiedName: String): String =
    "KmpReflect_" + qualifiedName.replace('.', '_').replace('$', '_')
