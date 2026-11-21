package io.github.shijianjs.kmp.reflect.ksp

import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * Writes the module object that ties a module's reflected classes and inlined resources together,
 * plus the resource holders themselves.
 *
 * One file per module: the object, and one `private` lazy `ByteArray` + one `internal` `KmpResource`
 * per file. Nothing is read from disk at runtime — the bytes are Base64 literals.
 */
internal class RegistryWriter {
    private val kclassList: ClassName = ClassName("kotlin.collections", "List")

    /** The module object, [target] naming its package and type, for [classes] and [resources]. */
    fun file(
        target: RegistryTarget,
        moduleName: String,
        classes: List<ClassModel>,
        resources: List<ResourceEntry>,
    ): FileSpec {
        val builder = FileSpec.builder(target.packageName, target.objectName)
            .addAnnotation(
                com.squareup.kotlinpoet.AnnotationSpec.builder(KmpNames.SUPPRESS)
                    .addMember("%S", "UNCHECKED_CAST")
                    .build(),
            )
        if (resources.isNotEmpty()) {
            builder.addAnnotation(
                com.squareup.kotlinpoet.AnnotationSpec.builder(KmpNames.OPT_IN)
                    .addMember("%T::class", KmpNames.EXPERIMENTAL_ENCODING_API)
                    .build(),
            )
        }
        resources.forEachIndexed { index, resource ->
            builder.addProperty(
                PropertySpec.builder("kmpResourceBytes$index", BYTE_ARRAY, KModifier.PRIVATE)
                    .delegate(CodeBlock.of("lazy { %T.decode(%S) }", KmpNames.BASE64, resource.base64))
                    .build(),
            )
            builder.addProperty(
                PropertySpec.builder("kmpResource$index", KmpNames.KMP_RESOURCE, KModifier.INTERNAL)
                    .initializer(
                        CodeBlock.of(
                            "%T(path = %S, mimeType = %S, size = %L) { kmpResourceBytes%L }",
                            KmpNames.KMP_RESOURCE,
                            resource.path,
                            resource.mimeType,
                            resource.size,
                            index,
                        ),
                    )
                    .build(),
            )
        }
        classes.forEach { model ->
            builder.addImport(model.packageName, model.valueName)
        }
        val moduleObject = TypeSpec.objectBuilder(target.objectName)
            .addSuperinterface(KmpNames.KMP_REFLECT_MODULE)
            .addProperty(
                PropertySpec.builder("module", ClassName("kotlin", "String"), KModifier.OVERRIDE)
                    .initializer("%S", moduleName)
                    .build(),
            )
            .addProperty(
                PropertySpec.builder(
                    "classes",
                    kclassList.parameterizedBy(KmpNames.KMP_CLASS),
                    KModifier.OVERRIDE,
                )
                    .getter(
                        FunSpec.getterBuilder()
                            .addCode("return classNames.mapNotNull { name -> load(name) }")
                            .build(),
                    )
                    .build(),
            )
            .addProperty(
                PropertySpec.builder(
                    "classNames",
                    kclassList.parameterizedBy(ClassName("kotlin", "String")),
                    KModifier.OVERRIDE,
                )
                    .initializer(nameList(classes))
                    .build(),
            )
            .addFunction(loadFunction(classes))
        if (resources.isNotEmpty()) {
            moduleObject.addProperty(
                PropertySpec.builder(
                    "resources",
                    kclassList.parameterizedBy(KmpNames.KMP_RESOURCE),
                    KModifier.OVERRIDE,
                )
                    .initializer(resourceList(resources))
                    .build(),
            )
        }
        return builder.addType(moduleObject.build()).build()
    }

    private fun nameList(classes: List<ClassModel>): CodeBlock {
        if (classes.isEmpty()) return CodeBlock.of("emptyList()")
        return CodeBlock.builder().apply {
            add("listOf(\n")
            indent()
            classes.sortedBy { it.qualifiedName }.forEach { model ->
                add("%S,\n", model.qualifiedName)
            }
            unindent()
            add(")")
        }.build()
    }

    /**
     * The per-name loader: each branch returns the class's own `by lazy` property, so a lookup builds
     * one class and a module with a hundred classes costs nothing until something is asked for.
     */
    private fun loadFunction(classes: List<ClassModel>): FunSpec {
        val builder = FunSpec.builder("load")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("qualifiedName", ClassName("kotlin", "String"))
            .returns(KmpNames.KMP_CLASS.copy(nullable = true))
        if (classes.isEmpty()) {
            return builder.addStatement("return null").build()
        }
        builder.beginControlFlow("return when (qualifiedName)")
        classes.sortedBy { it.qualifiedName }.forEach { model ->
            builder.addStatement("%S -> %N", model.qualifiedName, model.valueName)
        }
        builder.addStatement("else -> null")
        builder.endControlFlow()
        return builder.build()
    }

    private fun resourceList(resources: List<ResourceEntry>): CodeBlock {
        if (resources.isEmpty()) return CodeBlock.of("emptyList()")
        return CodeBlock.builder().apply {
            add("listOf(\n")
            indent()
            resources.forEachIndexed { index, _ ->
                add("kmpResource%L,\n", index)
            }
            unindent()
            add(")")
        }.build()
    }
}
