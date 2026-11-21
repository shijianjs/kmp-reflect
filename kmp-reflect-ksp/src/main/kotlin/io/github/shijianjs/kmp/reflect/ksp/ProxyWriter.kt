package io.github.shijianjs.kmp.reflect.ksp

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * Writes the proxy class generated for an interface.
 *
 * Every abstract member forwards to `KmpInvocationHandler` with the reflected function it maps to, so
 * the proxy exists as ordinary Kotlin code on every target — there is no `java.lang.reflect.Proxy`
 * here.
 */
internal class ProxyWriter(private val metadataWriter: MetadataWriter) {
    /** The file carrying the proxy class for [model], whose `proxyable` flag must be `true`. */
    fun file(model: ClassModel): FileSpec {
        val functions = model.functions.withIndex().filter { (_, function) -> function.isAbstract }
        val proxy = TypeSpec.classBuilder(proxyClassName(model.qualifiedName))
            .addModifiers(KModifier.INTERNAL)
            .addSuperinterface(model.ownerType())
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("kmpClass", KmpNames.KMP_CLASS)
                    .addParameter("handler", KmpNames.KMP_INVOCATION_HANDLER)
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("kmpClass", KmpNames.KMP_CLASS, KModifier.PRIVATE)
                    .initializer("kmpClass")
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("handler", KmpNames.KMP_INVOCATION_HANDLER, KModifier.PRIVATE)
                    .initializer("handler")
                    .build(),
            )
        functions.forEach { (index, function) ->
            proxy.addProperty(
                PropertySpec.builder(accessorName(index), KmpNames.KMP_FUNCTION, KModifier.PRIVATE)
                    .initializer("kmpClass.functions[%L]", index)
                    .build(),
            )
            proxy.addFunction(overrideFunction(model, function, index))
        }
        return FileSpec.builder(model.packageName, proxyFileName(model.qualifiedName))
            .addAnnotation(metadataWriter.fileSuppress())
            .addType(proxy.build())
            .build()
    }

    private fun overrideFunction(model: ClassModel, function: FunctionModel, index: Int): FunSpec {
        val builder = FunSpec.builder(function.name).addModifiers(KModifier.OVERRIDE, KModifier.PUBLIC)
        function.parameters.forEach { parameter ->
            val type = parameter.type.castTypeName() ?: ANY.copy(nullable = true)
            val spec = ParameterSpec.builder(parameter.name, type)
            if (parameter.isVararg) spec.addModifiers(KModifier.VARARG)
            builder.addParameter(spec.build())
        }
        val arguments = CodeBlock.builder().apply {
            add("this, %L, listOf(", accessorName(index))
            function.parameters.forEachIndexed { position, parameter ->
                if (position > 0) add(", ")
                add("%N", parameter.name)
            }
            add(")")
        }.build()
        val returnType = function.returnType.castTypeName()
        builder.returns(returnType ?: ANY.copy(nullable = true))
        if (function.returnType.qualifiedName == "kotlin.Unit") {
            builder.addStatement("handler.invoke(%L)", arguments)
        } else {
            builder.addStatement("return handler.invoke(%L) as %T", arguments, returnType ?: ANY.copy(nullable = true))
        }
        return builder.build()
    }

    private fun accessorName(index: Int): String = "kmpFunction$index"

    private fun proxyFileName(qualifiedName: String): String =
        "KmpProxy_" + qualifiedName.replace('.', '_').replace('$', '_')
}
