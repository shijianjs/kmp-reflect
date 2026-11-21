package io.github.shijianjs.kmp.reflect.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Variance
import com.google.devtools.ksp.symbol.Visibility
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Turns KSP symbols into the plain model the writers consume.
 *
 * Everything is resolved here, in the first round, so code generation never asks the resolver again
 * and cannot be affected by later rounds.
 */
internal class SymbolConverter(
    private val logger: KSPLogger,
    private val publicOnly: Boolean,
    private val ignoredDeclarations: Set<String>,
) {
    /** Converts one resolved type. */
    fun typeModel(type: KSType, depth: Int = 0): TypeModel {
        if (depth > MAX_TYPE_DEPTH) {
            logger.warn("Type nesting exceeded $MAX_TYPE_DEPTH levels; recording it as Any?.")
            return anyType()
        }
        var declaration = type.declaration
        // A type alias is written by name but means the expanded type; metadata records the expansion.
        while (declaration is KSTypeAlias) {
            declaration = declaration.type.resolve().declaration
        }
        if (type.isError) return anyType(kind = "UNKNOWN", nullable = true)
        if (declaration is KSTypeParameter) {
            return TypeModel(
                qualifiedName = declaration.simpleName.asString(),
                simpleName = declaration.simpleName.asString(),
                kind = "TYPE_PARAMETER",
                nullable = type.isMarkedNullable,
                arguments = emptyList(),
                variance = "INVARIANT",
                className = null,
                isTypeParameter = true,
                isStar = false,
            )
        }
        val classDeclaration = declaration as? KSClassDeclaration
        val qualifiedName = declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()
        val className = classDeclaration?.toClassName()
        val arguments = type.arguments.map { argument ->
            val reference = argument.type
            if (argument.variance == Variance.STAR || reference == null) {
                TypeModel.STAR_PROJECTION
            } else {
                typeModel(reference.resolve(), depth + 1).withVariance(argument.variance)
            }
        }
        return TypeModel(
            qualifiedName = qualifiedName,
            simpleName = declaration.simpleName.asString(),
            kind = kindOf(classDeclaration, type),
            nullable = type.isMarkedNullable,
            arguments = arguments,
            variance = "INVARIANT",
            className = className,
            isTypeParameter = false,
            isStar = false,
        )
    }

    /** Converts one annotation occurrence, including its arguments. */
    fun annotationModel(annotation: KSAnnotation): AnnotationModel {
        val type = typeModel(annotation.annotationType.resolve())
        val arguments = annotation.arguments.mapNotNull { argument ->
            val name = argument.name?.asString() ?: return@mapNotNull null
            name to argumentModel(argument.value, annotation)
        }
        return AnnotationModel(type, arguments)
    }

    /** Converts the declaration annotations of [declaration], skipping the processor's own markers. */
    fun annotationsOf(declaration: KSDeclaration): List<AnnotationModel> =
        declaration.annotations
            .filterNot { it.shortName.asString() in SELF_ANNOTATION_COERCIONS }
            .map { annotationModel(it) }
            .toList()

    private fun argumentModel(value: Any?, owner: KSAnnotation): AnnotationArgument = when (value) {
        is String -> AnnotationArgument.Text(value)
        is Int -> AnnotationArgument.Number(value.toString())
        is Long -> AnnotationArgument.Number("${value}L")
        is Short -> AnnotationArgument.Number("${value}.toShort()")
        is Byte -> AnnotationArgument.Number("${value}.toByte()")
        is Float -> AnnotationArgument.Number("${value}f")
        is Double -> AnnotationArgument.Number(value.toString())
        is Boolean -> AnnotationArgument.Flag(value)
        is Char -> AnnotationArgument.Character(value)
        is KSType -> typeArgumentModel(value) ?: unsupported(owner, value)
        is KSClassDeclaration -> classArgumentModel(value) ?: unsupported(owner, value)
        is KSAnnotation -> AnnotationArgument.Nested(annotationModel(value))
        is Array<*> -> AnnotationArgument.ArrayOf(value.map { argumentModel(it, owner) })
        is List<*> -> AnnotationArgument.ArrayOf(value.map { argumentModel(it, owner) })
        else -> unsupported(owner, value)
    }

    private fun typeArgumentModel(type: KSType): AnnotationArgument? {
        val declaration = type.declaration
        if (declaration is KSClassDeclaration && declaration.classKind == ClassKind.ENUM_ENTRY) {
            return enumArgumentModel(declaration)
        }
        val className = (declaration as? KSClassDeclaration)?.toClassName() ?: return null
        return AnnotationArgument.ClassReference(className)
    }

    private fun classArgumentModel(declaration: KSClassDeclaration): AnnotationArgument? =
        if (declaration.classKind == ClassKind.ENUM_ENTRY) {
            enumArgumentModel(declaration)
        } else {
            AnnotationArgument.ClassReference(declaration.toClassName())
        }

    private fun enumArgumentModel(entry: KSClassDeclaration): AnnotationArgument? {
        val enumClass = entry.parentDeclaration as? KSClassDeclaration ?: return null
        return AnnotationArgument.EnumReference(enumClass.toClassName(), entry.simpleName.asString())
    }

    private fun unsupported(owner: KSAnnotation, value: Any?): AnnotationArgument.Unsupported {
        val description = "${owner.shortName.asString()}: ${value?.javaClass?.name ?: "null"}"
        logger.warn("Annotation argument the generator cannot express ($description); recorded as null.")
        return AnnotationArgument.Unsupported(description)
    }

    /**
     * Builds the model for one class, or returns `null` when it cannot be reflected (private,
     * `expect`, or a name KSP could not resolve).
     */
    fun classModel(
        declaration: KSClassDeclaration,
        reflectedNames: Set<String>,
        includeNonPublic: Boolean,
    ): ClassModel? {
        val qualifiedName = declaration.qualifiedName?.asString()
        if (qualifiedName == null) {
            logger.warn("Skipping a class without a qualified name.")
            return null
        }
        if (qualifiedName in ignoredDeclarations) return null
        // Generated metadata is a top-level file: neither a `private` nor a `protected` declaration
        // can be referenced from there, so reflecting one would only break the consumer's build.
        if (Modifier.PRIVATE in declaration.modifiers || Modifier.PROTECTED in declaration.modifiers) {
            logger.warn(
                "Skipping '$qualifiedName': private and protected declarations cannot be referenced from generated code.",
                declaration,
            )
            return null
        }
        if (Modifier.EXPECT in declaration.modifiers) {
            logger.info("Skipping '$qualifiedName': expect declarations carry no implementation to reflect.")
            return null
        }
        val visibilityAllowed = if (publicOnly) visibilityOf(declaration) == Visibility.PUBLIC else true
        if (!visibilityAllowed) return null

        // `includeNonPublic` comes from the annotation and can be overridden globally by the option.
        val membersVisible = includeNonPublic && !publicOnly

        val className = declaration.toClassName()
        val packageName = declaration.packageName.asString()
        val supertypes = declaration.superTypes.map { typeModel(it.resolve()) }.toList()
        // Supertypes that are reflected in this same module get linked to their metadata value. A
        // supertype from another module is only described: its metadata is `internal` there.
        val reflectedSupertypeNames = declaration.superTypes
            .mapNotNull { it.resolve().declaration.qualifiedName?.asString() }
            .filter { it in reflectedNames }
            .toList()
        val typeParameters = declaration.typeParameters.map { typeParameterModel(it) }

        val declarations = declaration.declarations.toList()

        val kind = when {
            declaration.isCompanionObject -> "COMPANION_OBJECT"
            declaration.classKind == ClassKind.INTERFACE -> "INTERFACE"
            declaration.classKind == ClassKind.OBJECT -> "OBJECT"
            declaration.classKind == ClassKind.ENUM_CLASS -> "ENUM_CLASS"
            declaration.classKind == ClassKind.ANNOTATION_CLASS -> "ANNOTATION_CLASS"
            else -> "CLASS"
        }
        val isInterface = declaration.classKind == ClassKind.INTERFACE

        val constructors = constructorModels(declaration, declarations, membersVisible)
        val functions = declarations
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { it.simpleName.asString() != CONSTRUCTOR_NAME }
            // Enum helpers (`values`, `valueOf`, `entries`) are synthesized by the compiler; they
            // exist on every enum, so reflecting them would only add noise.
            .filterNot { kind == "ENUM_CLASS" && it.simpleName.asString() in ENUM_SYNTHETIC_FUNCTIONS }
            .filter { memberVisible(it, membersVisible) }
            .filter { it.extensionReceiver == null }
            .map { functionModel(it) }
            .toList()
        val properties = declarations
            .filterIsInstance<KSPropertyDeclaration>()
            // `entries`, `name` and `ordinal` come from the compiler on every enum.
            .filterNot { kind == "ENUM_CLASS" && it.simpleName.asString() in ENUM_SYNTHETIC_PROPERTIES }
            .filter { memberVisible(it, membersVisible) }
            .filter { it.extensionReceiver == null }
            .map { propertyModel(it) }
            .toList()

        val nested = declarations.filterIsInstance<KSClassDeclaration>()
        val enumEntries = nested
            .filter { it.classKind == ClassKind.ENUM_ENTRY }
            .mapIndexed { index, entry ->
                EnumEntryModel(entry.simpleName.asString(), index, annotationsOf(entry))
            }
        val companion = nested.firstOrNull { it.isCompanionObject }
        val companionName = companion?.qualifiedName?.asString()?.takeIf { it in reflectedNames }
        val nestedNames = nested
            .filter { !it.isCompanionObject && it.classKind != ClassKind.ENUM_ENTRY }
            .mapNotNull { it.qualifiedName?.asString() }
            .filter { it in reflectedNames && it != companionName }

        return ClassModel(
            qualifiedName = qualifiedName,
            simpleName = declaration.simpleName.asString(),
            packageName = packageName,
            className = className,
            kind = kind,
            visibility = visibilityName(declaration),
            modifiers = modifierNames(declaration),
            annotations = annotationsOf(declaration),
            typeParameters = typeParameters,
            supertypes = supertypes,
            reflectedSupertypeNames = reflectedSupertypeNames,
            constructors = constructors,
            functions = functions,
            properties = properties,
            enumEntries = enumEntries,
            companionQualifiedName = companionName,
            nestedQualifiedNames = nestedNames,
            hasObjectInstance = kind == "OBJECT" || kind == "COMPANION_OBJECT",
            isInterface = isInterface,
            proxyable = false,
        )
    }

    /**
     * Whether a declared member can appear in generated code at all.
     *
     * `private` and `protected` members cannot: the generated metadata is a top-level file, outside
     * the class, so a call to either would not compile. They are dropped instead of described, which
     * is also what `kmp.reflect.visibility=public` narrows `internal` members to.
     */
    private fun memberVisible(declaration: KSDeclaration, includeNonPublic: Boolean): Boolean {
        if (Modifier.PRIVATE in declaration.modifiers) return false
        if (Modifier.PROTECTED in declaration.modifiers) return false
        if (declaration.qualifiedName?.asString() in ignoredDeclarations) return false
        if (!includeNonPublic && visibilityOf(declaration) != Visibility.PUBLIC) return false
        return true
    }

    private fun constructorModels(
        declaration: KSClassDeclaration,
        declarations: List<KSDeclaration>,
        includeNonPublic: Boolean,
    ): List<ConstructorModel> {
        // Objects, enums and annotation classes cannot be constructed from outside.
        if (declaration.classKind != ClassKind.CLASS && declaration.classKind != ClassKind.INTERFACE) {
            return emptyList()
        }
        val fromDeclarations = declarations
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { it.simpleName.asString() == CONSTRUCTOR_NAME }
        val primary = declaration.primaryConstructor
        val candidates = if (fromDeclarations.isEmpty()) {
            listOfNotNull(primary)
        } else {
            (fromDeclarations + listOfNotNull(primary)).distinctBy { constructorSignature(it) }
        }
        return candidates
            .filter { memberVisible(it, includeNonPublic) }
            .map { constructor ->
                val parameters = parameterModels(constructor)
                val invokable = Modifier.INNER !in declaration.modifiers &&
                    parameters.none { it.type.usesTypeParameter }
                ConstructorModel(
                    parameters = parameters,
                    modifiers = modifierNames(constructor),
                    annotations = annotationsOf(constructor),
                    visibility = visibilityName(constructor),
                    invokable = invokable,
                )
            }
    }

    private fun constructorSignature(constructor: KSFunctionDeclaration): String =
        constructor.parameters.joinToString(",") { it.type.resolve().declaration.qualifiedName?.asString() ?: "?" }

    private fun functionModel(declaration: KSFunctionDeclaration): FunctionModel {
        val parameters = parameterModels(declaration)
        val returnType = declaration.returnType?.resolve()?.let { typeModel(it) } ?: unitType()
        val typeParameters = declaration.typeParameters.map { typeParameterModel(it) }
        val isSuspend = Modifier.SUSPEND in declaration.modifiers
        // `suspend` does not block invocation: the writer emits a `suspend` lambda for it, and the
        // runtime exposes `invokeSuspend`. Only type parameters make a call impossible to inline.
        val invokable = typeParameters.isEmpty() &&
            parameters.none { it.type.usesTypeParameter } &&
            !returnType.usesTypeParameter
        return FunctionModel(
            name = declaration.simpleName.asString(),
            returnType = returnType,
            parameters = parameters,
            typeParameters = typeParameters,
            modifiers = modifierNames(declaration),
            annotations = annotationsOf(declaration),
            visibility = visibilityName(declaration),
            isSuspend = isSuspend,
            isAbstract = declaration.isAbstract,
            isExtension = declaration.extensionReceiver != null,
            invokable = invokable,
        )
    }

    private fun propertyModel(declaration: KSPropertyDeclaration): PropertyModel {
        val type = declaration.type.resolve().let { typeModel(it) }
        // A property is reachable only through its accessors, and the generated file is outside the
        // class: an accessor that is `private` or `protected` there is as good as absent.
        val getterAccessible = declaration.getter?.let { accessor ->
            Modifier.PRIVATE !in accessor.modifiers && Modifier.PROTECTED !in accessor.modifiers
        } ?: true
        val readable = !type.usesTypeParameter && getterAccessible
        // A `var` with a private or protected setter is writable from Kotlin but not from generated
        // code in another file, so the accessor's own visibility decides.
        val setterAccessible = declaration.setter?.let { accessor ->
            Modifier.PRIVATE !in accessor.modifiers && Modifier.PROTECTED !in accessor.modifiers
        } ?: true
        val writable = declaration.isMutable && readable && setterAccessible
        return PropertyModel(
            name = declaration.simpleName.asString(),
            type = type,
            mutable = declaration.isMutable,
            modifiers = modifierNames(declaration),
            annotations = annotationsOf(declaration),
            visibility = visibilityName(declaration),
            readable = readable,
            writable = writable,
        )
    }

    private fun parameterModels(function: KSFunctionDeclaration): List<ParameterModel> =
        function.parameters.mapIndexed { index, parameter ->
            ParameterModel(
                name = parameter.name?.asString() ?: "arg$index",
                type = typeModel(parameter.type.resolve()),
                index = index,
                isVararg = parameter.isVararg,
                hasDefault = parameter.hasDefault,
                annotations = parameter.annotations.map { annotationModel(it) }.toList(),
            )
        }

    private fun typeParameterModel(parameter: KSTypeParameter): TypeParameterModel = TypeParameterModel(
        name = parameter.name.asString(),
        bounds = parameter.bounds.map { typeModel(it.resolve()) }.toList(),
        reified = Modifier.REIFIED in parameter.modifiers,
    )

    private fun visibilityName(declaration: KSDeclaration): String = when (visibilityOf(declaration)) {
        Visibility.PUBLIC -> "PUBLIC"
        Visibility.INTERNAL -> "INTERNAL"
        Visibility.PROTECTED -> "PROTECTED"
        Visibility.PRIVATE -> "PRIVATE"
        else -> "PUBLIC"
    }

    private fun visibilityOf(declaration: KSDeclaration): Visibility {
        val modifiers = declaration.modifiers
        return when {
            Modifier.PRIVATE in modifiers -> Visibility.PRIVATE
            Modifier.PROTECTED in modifiers -> Visibility.PROTECTED
            Modifier.INTERNAL in modifiers -> Visibility.INTERNAL
            else -> Visibility.PUBLIC
        }
    }

    private fun modifierNames(declaration: KSDeclaration): List<String> = declaration.modifiers
        .mapNotNull { modifier -> MODIFIER_NAMES[modifier] }
        .sorted()

    private fun kindOf(declaration: KSClassDeclaration?, type: KSType): String = when {
        type.isError -> "UNKNOWN"
        declaration == null -> "UNKNOWN"
        declaration.classKind == ClassKind.INTERFACE -> "INTERFACE"
        declaration.classKind == ClassKind.OBJECT -> "OBJECT"
        declaration.classKind == ClassKind.ENUM_CLASS || declaration.classKind == ClassKind.ENUM_ENTRY -> "ENUM_CLASS"
        declaration.classKind == ClassKind.ANNOTATION_CLASS -> "ANNOTATION_CLASS"
        else -> "CLASS"
    }

    private fun TypeModel.withVariance(variance: Variance): TypeModel = TypeModel(
        qualifiedName = qualifiedName,
        simpleName = simpleName,
        kind = kind,
        nullable = nullable,
        arguments = arguments,
        variance = when (variance) {
            Variance.COVARIANT -> "OUT"
            Variance.CONTRAVARIANT -> "IN"
            else -> "INVARIANT"
        },
        className = className,
        isTypeParameter = isTypeParameter,
        isStar = isStar,
    )

    private fun anyType(kind: String = "UNKNOWN", nullable: Boolean = true): TypeModel = TypeModel(
        qualifiedName = "kotlin.Any",
        simpleName = "Any",
        kind = kind,
        nullable = nullable,
        arguments = emptyList(),
        variance = "INVARIANT",
        className = ClassName("kotlin", "Any"),
        isTypeParameter = false,
        isStar = false,
    )

    private fun unitType(): TypeModel = TypeModel(
        qualifiedName = "kotlin.Unit",
        simpleName = "Unit",
        kind = "OBJECT",
        nullable = false,
        arguments = emptyList(),
        variance = "INVARIANT",
        className = ClassName("kotlin", "Unit"),
        isTypeParameter = false,
        isStar = false,
    )

    private companion object {
        const val CONSTRUCTOR_NAME = "<init>"
        const val MAX_TYPE_DEPTH = 8

        /** Annotations the processor itself reads; keeping them in the metadata would be noise. */
        val SELF_ANNOTATION_COERCIONS = setOf("KmpReflect", "KmpReflectIgnore", "KmpReflectable")

        /** Members every enum gets from the compiler; they would only add noise to the metadata. */
        val ENUM_SYNTHETIC_FUNCTIONS = setOf("values", "valueOf", "entries", "getEntries")

        val ENUM_SYNTHETIC_PROPERTIES = setOf("entries", "name", "ordinal")

        val MODIFIER_NAMES: Map<Modifier, String> = mapOf(
            Modifier.FINAL to "FINAL",
            Modifier.OPEN to "OPEN",
            Modifier.ABSTRACT to "ABSTRACT",
            Modifier.SEALED to "SEALED",
            Modifier.DATA to "DATA",
            Modifier.ENUM to "ENUM",
            Modifier.ANNOTATION to "ANNOTATION",
            Modifier.INNER to "INNER",
            Modifier.VALUE to "VALUE",
            Modifier.FUN to "FUN",
            Modifier.INLINE to "INLINE",
            Modifier.EXPECT to "EXPECT",
            Modifier.ACTUAL to "ACTUAL",
            Modifier.EXTERNAL to "EXTERNAL",
            Modifier.CONST to "CONST",
            Modifier.LATEINIT to "LATEINIT",
            Modifier.SUSPEND to "SUSPEND",
            Modifier.OPERATOR to "OPERATOR",
            Modifier.INFIX to "INFIX",
            Modifier.TAILREC to "TAILREC",
            Modifier.OVERRIDE to "OVERRIDE",
            Modifier.REIFIED to "REIFIED",
        )
    }
}
