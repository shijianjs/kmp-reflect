package io.github.shijianjs.kmp.reflect.ksp

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier

import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName

/**
 * Writes the generated Kotlin for reflected metadata: one `internal` top-level value per class,
 * a `lazy` initialiser, and every member's invocation inlined as a lambda.
 *
 * The writer is stateful only for the currently written class, which the member writers need as the
 * receiver type of their `as` casts. It is used from a single thread by the processor.
 */
internal class MetadataWriter {
    private var owner: ClassModel? = null

    /** The file carrying [model]'s metadata, written into [ClassModel.packageName]. */
    fun file(model: ClassModel): FileSpec {
        owner = model
        try {
            return FileSpec.builder(model.packageName, metadataFileName(model.qualifiedName))
                .addAnnotation(fileSuppress())
                .addProperty(
                    PropertySpec.builder(model.valueName, KmpNames.KMP_CLASS)
                        .addModifiers(KModifier.INTERNAL)
                        .delegate(
                            CodeBlock.builder()
                                .add("lazy {\n")
                                .indent()
                                .add(classBlock(model))
                                .add("\n")
                                .unindent()
                                .add("}")
                                .build(),
                        )
                        .build(),
                )
                .build()
        } finally {
            owner = null
        }
    }

    /** An `@file:Suppress` that keeps the generated casts and unused holder quiet. */
    fun fileSuppress(): AnnotationSpec = AnnotationSpec.builder(KmpNames.SUPPRESS)
        .addMember(
            "%S, %S, %S, %S",
            "UNCHECKED_CAST",
            "unused",
            "RedundantVisibilityModifier",
            "RemoveExplicitTypeArguments",
        )
        .build()

    private fun classBlock(model: ClassModel): CodeBlock = CodeBlock.builder().apply {
        add("%T(\n", KmpNames.KMP_CLASS)
        indent()
        add("qualifiedName = %S,\n", model.qualifiedName)
        add("simpleName = %S,\n", model.simpleName)
        add("packageName = %S,\n", model.packageName)
        add("kind = %T.%L,\n", KmpNames.KMP_KIND, model.kind)
        add("visibility = %T.%L,\n", KmpNames.KMP_VISIBILITY, model.visibility)
        add("modifiers = ").modifierSet(model.modifiers).add(",\n")
        add("annotations = ").annotationList(model.annotations).add(",\n")
        add("typeParameters = ").typeParameterList(model.typeParameters).add(",\n")
        add("supertypes = ").typeList(model.supertypes).add(",\n")
        add("superclasses = ").linkedClassList(model.reflectedSupertypeNames).add(",\n")
        add("constructors = ").constructorList(model.constructors).add(",\n")
        add("functions = ").functionList(model.functions).add(",\n")
        add("properties = ").propertyList(model.properties).add(",\n")
        add("enumEntries = ").enumEntryList(model).add(",\n")
        add("companion = %L,\n", model.companionQualifiedName?.let { metadataValueName(it) } ?: "null")
        add("nestedClasses = ").nestedList(model.nestedQualifiedNames).add(",\n")
        if (model.hasObjectInstance) {
            add("objectInstance = %T,\n", model.className)
        } else {
            add("objectInstance = null,\n")
        }
        add("runtime = %T::class,\n", model.className)
        if (model.proxyable) {
            add(
                "proxyFactory = { handler -> %L(handler = handler, kmpClass = %L) },\n",
                proxyClassName(model.qualifiedName),
                model.valueName,
            )
        }
        unindent()
        add(")")
    }.build()

    private fun CodeBlock.Builder.modifierSet(modifiers: List<String>): CodeBlock.Builder = apply {
        if (modifiers.isEmpty()) {
            add("emptySet()")
        } else {
            add("setOf(")
            modifiers.forEachIndexed { index, modifier ->
                if (index > 0) add(", ")
                add("%T.%L", KmpNames.KMP_MODIFIER, modifier)
            }
            add(")")
        }
    }

    private fun CodeBlock.Builder.annotationList(annotations: List<AnnotationModel>): CodeBlock.Builder = apply {
        if (annotations.isEmpty()) {
            add("emptyList()")
        } else {
            add("listOf(\n")
            indent()
            annotations.forEach { annotation ->
                annotationBlock(annotation)
                add(",\n")
            }
            unindent()
            add(")")
        }
    }

    private fun CodeBlock.Builder.annotationBlock(annotation: AnnotationModel): CodeBlock.Builder = apply {
        add("%T(type = ", KmpNames.KMP_ANNOTATION)
        typeRef(annotation.type)
        if (annotation.arguments.isNotEmpty()) {
            add(", arguments = mapOf(")
            annotation.arguments.forEachIndexed { index, (name, argument) ->
                if (index > 0) add(", ")
                add("%S to ", name)
                annotationArgument(argument)
            }
            add(")")
        }
        add(")")
    }

    private fun CodeBlock.Builder.annotationArgument(argument: AnnotationArgument): CodeBlock.Builder = apply {
        when (argument) {
            is AnnotationArgument.Text -> add("%S", argument.value)
            is AnnotationArgument.Number -> add("%L", argument.literal)
            is AnnotationArgument.Flag -> add("%L", argument.value)
            is AnnotationArgument.Character -> add("%S", argument.value.toString())
            is AnnotationArgument.ClassReference -> add("%T::class", argument.className)
            is AnnotationArgument.EnumReference -> add("%T.%N", argument.enumClassName, argument.entryName)
            is AnnotationArgument.Nested -> annotationBlock(argument.annotation)
            is AnnotationArgument.ArrayOf -> {
                if (argument.values.isEmpty()) {
                    // An untyped `listOf()` would leave the map's value type to inference.
                    add("emptyList<Any?>()")
                } else {
                    add("listOf(")
                    argument.values.forEachIndexed { index, value ->
                        if (index > 0) add(", ")
                        annotationArgument(value)
                    }
                    add(")")
                }
            }
            is AnnotationArgument.Unsupported -> add("null")
        }
    }

    private fun CodeBlock.Builder.typeParameterList(parameters: List<TypeParameterModel>): CodeBlock.Builder = apply {
        if (parameters.isEmpty()) {
            add("emptyList()")
        } else {
            add("listOf(\n")
            indent()
            parameters.forEach { parameter ->
                add("%T(name = %S, bounds = ", KmpNames.KMP_TYPE_PARAMETER, parameter.name)
                typeList(parameter.bounds)
                if (parameter.reified) add(", reified = true")
                add("),\n")
            }
            unindent()
            add(")")
        }
    }

    private fun CodeBlock.Builder.typeList(types: List<TypeModel>): CodeBlock.Builder = apply {
        if (types.isEmpty()) {
            add("emptyList()")
        } else {
            add("listOf(\n")
            indent()
            types.forEach { type ->
                typeRef(type)
                add(",\n")
            }
            unindent()
            add(")")
        }
    }

    private fun CodeBlock.Builder.typeRef(type: TypeModel): CodeBlock.Builder = apply {
        add("%T(\n", KmpNames.KMP_TYPE_REF)
        indent()
        add("qualifiedName = %S,\n", type.qualifiedName)
        add("simpleName = %S,\n", type.simpleName)
        add("kind = %T.%L,\n", KmpNames.KMP_TYPE_KIND, type.kind)
        add("nullable = %L,\n", type.nullable)
        if (type.variance != "INVARIANT") {
            add("variance = %T.%L,\n", KmpNames.KMP_VARIANCE, type.variance)
        }
        if (type.arguments.isNotEmpty()) {
            add("arguments = ")
            typeList(type.arguments)
            add(",\n")
        }
        type.className?.let { add("runtime = %T::class,\n", it) }
        unindent()
        add(")")
    }

    private fun CodeBlock.Builder.constructorList(constructors: List<ConstructorModel>): CodeBlock.Builder = apply {
        if (constructors.isEmpty()) {
            add("emptyList()")
        } else {
            add("listOf(\n")
            indent()
            constructors.forEach { constructor ->
                add("%T(\n", KmpNames.KMP_CONSTRUCTOR)
                indent()
                add("parameters = ").parameterList(constructor.parameters).add(",\n")
                add("modifiers = ").modifierSet(constructor.modifiers).add(",\n")
                add("annotations = ").annotationList(constructor.annotations).add(",\n")
                if (constructor.visibility != "PUBLIC") {
                    add("visibility = %T.%L,\n", KmpNames.KMP_VISIBILITY, constructor.visibility)
                }
                add("isInvokable = %L,\n", constructor.invokable)
                if (constructor.invokable) {
                    add("invoker = { args -> ")
                    argumentDispatch(constructor.parameters) { count ->
                        add("%T(%L)", ownerType(), callArguments(constructor.parameters, count))
                    }
                    add(" },\n")
                }
                unindent()
                add("),\n")
            }
            unindent()
            add(")")
        }
    }

    private fun CodeBlock.Builder.functionList(functions: List<FunctionModel>): CodeBlock.Builder = apply {
        if (functions.isEmpty()) {
            add("emptyList()")
        } else {
            add("listOf(\n")
            indent()
            functions.forEach { function ->
                add("%T(\n", KmpNames.KMP_FUNCTION)
                indent()
                add("name = %S,\n", function.name)
                add("returnType = ").typeRef(function.returnType).add(",\n")
                add("parameters = ").parameterList(function.parameters).add(",\n")
                add("typeParameters = ").typeParameterList(function.typeParameters).add(",\n")
                add("modifiers = ").modifierSet(function.modifiers).add(",\n")
                add("annotations = ").annotationList(function.annotations).add(",\n")
                if (function.visibility != "PUBLIC") {
                    add("visibility = %T.%L,\n", KmpNames.KMP_VISIBILITY, function.visibility)
                }
                add("isInvokable = %L,\n", function.invokable)
                when {
                    // A `suspend` function's lambda carries the `suspend` modifier itself, so the
                    // runtime can call it from a coroutine and nowhere else.
                    function.isSuspend && function.invokable -> {
                        add("suspendInvoker = suspend { receiver, args -> ")
                        argumentDispatch(function.parameters) { count ->
                            add("(receiver as %T).%N(%L)", ownerType(), function.name, callArguments(function.parameters, count))
                        }
                        add(" },\n")
                    }
                    function.invokable -> {
                        add("invoker = { receiver, args -> ")
                        argumentDispatch(function.parameters) { count ->
                            add("(receiver as %T).%N(%L)", ownerType(), function.name, callArguments(function.parameters, count))
                        }
                        add(" },\n")
                    }
                }
                unindent()
                add("),\n")
            }
            unindent()
            add(")")
        }
    }

    private fun CodeBlock.Builder.propertyList(properties: List<PropertyModel>): CodeBlock.Builder = apply {
        if (properties.isEmpty()) {
            add("emptyList()")
        } else {
            add("listOf(\n")
            indent()
            properties.forEach { property ->
                add("%T(\n", KmpNames.KMP_PROPERTY)
                indent()
                add("name = %S,\n", property.name)
                add("type = ").typeRef(property.type).add(",\n")
                add("isMutable = %L,\n", property.mutable)
                add("modifiers = ").modifierSet(property.modifiers).add(",\n")
                add("annotations = ").annotationList(property.annotations).add(",\n")
                if (property.visibility != "PUBLIC") {
                    add("visibility = %T.%L,\n", KmpNames.KMP_VISIBILITY, property.visibility)
                }
                add("isReadable = %L,\n", property.readable)
                add("isWritable = %L,\n", property.writable)
                if (property.readable) {
                    add("getter = { receiver -> (receiver as %T).%N },\n", ownerType(), property.name)
                }
                if (property.writable) {
                    add(
                        "setter = { receiver, value -> (receiver as %T).%N = value as %T },\n",
                        ownerType(),
                        property.name,
                        property.type.castTypeName() ?: ANY.copy(nullable = true),
                    )
                }
                unindent()
                add("),\n")
            }
            unindent()
            add(")")
        }
    }

    private fun CodeBlock.Builder.enumEntryList(model: ClassModel): CodeBlock.Builder = apply {
        if (model.enumEntries.isEmpty()) {
            add("emptyList()")
        } else {
            add("listOf(\n")
            indent()
            model.enumEntries.forEach { entry ->
                add("%T(name = %S, ordinal = %L, annotations = ", KmpNames.KMP_ENUM_ENTRY, entry.name, entry.ordinal)
                annotationList(entry.annotations)
                add(", instance = %T.%N),\n", model.className, entry.name)
            }
            unindent()
            add(")")
        }
    }

    /** References to other classes' metadata values; qualified, since the class may be in another package. */
    private fun CodeBlock.Builder.linkedClassList(names: List<String>): CodeBlock.Builder = apply {
        if (names.isEmpty()) {
            add("emptyList()")
        } else {
            add("listOf(")
            names.forEachIndexed { index, name ->
                if (index > 0) add(", ")
                // A top-level `internal` property is reachable across packages when qualified.
                val prefix = name.substringBeforeLast('.', "")
                add("%L", if (prefix.isEmpty()) metadataValueName(name) else "$prefix.${metadataValueName(name)}")
            }
            add(")")
        }
    }

    private fun CodeBlock.Builder.nestedList(names: List<String>): CodeBlock.Builder = apply {        if (names.isEmpty()) {
            add("emptyList()")
        } else {
            add("listOf(")
            names.forEachIndexed { index, name ->
                if (index > 0) add(", ")
                add("%L", metadataValueName(name))
            }
            add(")")
        }
    }

    private fun CodeBlock.Builder.parameterList(parameters: List<ParameterModel>): CodeBlock.Builder = apply {
        if (parameters.isEmpty()) {
            add("emptyList()")
        } else {
            add("listOf(\n")
            indent()
            parameters.forEach { parameter ->
                add("%T(\n", KmpNames.KMP_PARAMETER)
                indent()
                add("name = %S,\n", parameter.name)
                add("type = ").typeRef(parameter.type).add(",\n")
                add("index = %L,\n", parameter.index)
                if (parameter.isVararg) add("isVararg = true,\n")
                if (parameter.hasDefault) add("hasDefault = true,\n")
                add("annotations = ").annotationList(parameter.annotations).add(",\n")
                unindent()
                add("),\n")
            }
            unindent()
            add(")")
        }
    }

    /** The argument list of an inlined call: each parameter cast back to its declared type. */
    private fun callArguments(parameters: List<ParameterModel>, count: Int = parameters.size): CodeBlock =
        CodeBlock.builder().apply {
            parameters.take(count).forEachIndexed { position, parameter ->
                if (position > 0) add(", ")
                val castType = parameter.type.castTypeName() ?: ANY.copy(nullable = true)
                if (parameter.isVararg) {
                    add("*(args[%L] as %T)", parameter.index, arrayType(castType))
                } else {
                    add("args[%L] as %T", parameter.index, castType)
                }
            }
        }.build()

    /**
     * The argument counts the generated lambda accepts: the full list, plus every shorter tail whose
     * omitted parameters all declare a default. Those counts are contiguous, so they are written as
     * one `when (args.size)` instead of one lambda per count.
     */
    private fun allowedCounts(parameters: List<ParameterModel>): IntRange {
        val minimum = parameters.size - parameters.takeLastWhile { it.hasDefault }.size
        return minimum..parameters.size
    }

    /** Emits [call] for the full arity, or a `when (args.size)` over the allowed counts. */
    private inline fun CodeBlock.Builder.argumentDispatch(
        parameters: List<ParameterModel>,
        call: CodeBlock.Builder.(count: Int) -> Unit,
    ) {
        val counts = allowedCounts(parameters)
        if (counts.first == counts.last) {
            call(counts.first)
            return
        }
        add("when (args.size) {\n")
        indent()
        counts.forEach { count ->
            add("%L -> ", count)
            call(count)
            add("\n")
        }
        // The public entry points already reject other counts, so this branch is unreachable.
        add("else -> error(%S)\n", "unreachable: the runtime checks the argument count")
        unindent()
        add("}")
    }

    private fun arrayType(element: TypeName): TypeName = ClassName("kotlin", "Array").parameterizedBy(element)

    private fun ownerType(): TypeName = requireNotNull(owner) { "A class model must be set before writing members." }.ownerType()
}
