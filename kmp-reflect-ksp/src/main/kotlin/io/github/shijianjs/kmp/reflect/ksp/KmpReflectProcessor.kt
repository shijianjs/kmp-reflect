package io.github.shijianjs.kmp.reflect.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.FileSpec
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * The processor: finds the opt-in declarations, converts them once, and writes plain Kotlin.
 *
 * Work happens in the first round only. Generated files are ordinary source, not new symbols to
 * process, so later rounds have nothing to do; the flag also keeps a second invocation from writing
 * the same files twice.
 */
internal class KmpReflectProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val logger: KSPLogger = environment.logger
    private val codeGenerator: CodeGenerator = environment.codeGenerator
    private val options: Options = Options.parse(environment.options, logger)

    private var processed: Boolean = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) return emptyList()
        processed = true

        val declarations = collectDeclarations(resolver)
        if (declarations.isEmpty()) {
            logger.info("kmp-reflect: no @${KmpNames.KMP_REFLECT} or ${KmpNames.KMP_REFLECTABLE} declarations in this module.")
            return emptyList()
        }

        val ignored = resolver.getSymbolsWithAnnotation(KmpNames.KMP_REFLECT_IGNORE)
            .mapNotNull { (it as? KSDeclaration)?.qualifiedName?.asString() }
            .toSet()
        val converter = SymbolConverter(logger, options.publicOnly, ignored)
        val reflectedNames = declarations.mapNotNull { it.qualifiedName?.asString() }.toSet()

        val models = declarations.mapNotNull { declaration ->
            runCatching {
                converter.classModel(declaration, reflectedNames, includeNonPublic(declaration))
            }.onFailure { error ->
                logger.error(
                    "kmp-reflect could not reflect '${declaration.qualifiedName?.asString()}': ${error.message}",
                    declaration,
                )
            }.getOrNull()
        }
        if (models.isEmpty()) return emptyList()

        models.forEach { model ->
            if (!model.isInterface) return@forEach
            val declaration = declarations.firstOrNull { it.qualifiedName?.asString() == model.qualifiedName }
            model.proxyable = declaration != null && isProxyable(declaration, model)
        }

        writeMetadata(models, declarations)
        writeProxies(models)
        writeRegistry(models, resolver)

        logger.info(
            "kmp-reflect: reflected ${models.size} class(es) " +
                "(${models.count { it.proxyable }} prox${if (models.count { it.proxyable } == 1) "y" else "ies"}).",
        )
        return emptyList()
    }

    private fun writeMetadata(models: List<ClassModel>, declarations: List<KSClassDeclaration>) {
        val writer = MetadataWriter()
        models.forEach { model ->
            val origin = declarations.firstOrNull { it.qualifiedName?.asString() == model.qualifiedName }?.containingFile
            write(writer.file(model), origin, aggregating = false)
        }
    }

    private fun writeProxies(models: List<ClassModel>) {
        val writer = ProxyWriter(MetadataWriter())
        models.filter { it.proxyable }.forEach { model ->
            write(writer.file(model), origin = null, aggregating = false)
        }
    }

    private fun writeRegistry(models: List<ClassModel>, resolver: Resolver) {
        val target = options.registry
        if (target == null) {
            if (options.resourceDirs.isNotEmpty()) {
                logger.warn(
                    "kmp-reflect: resource directories are configured, but 'kmp.reflect.registry' is not. " +
                        "Resources are only inlined into the generated module object; nothing was generated.",
                )
            }
            return
        }
        val resources = if (options.resourceDirs.isEmpty()) {
            emptyList()
        } else {
            ResourceScanner(logger).scan(options.resourceDirs)
        }
        val file = RegistryWriter().file(target, options.moduleName, models, resources)
        write(file, origin = null, aggregating = true, allFiles = resolver.getAllFiles().toList())
        logger.info(
            "kmp-reflect: wrote module object '${target.packageName}.${target.objectName}' " +
                "with ${models.size} class(es) and ${resources.size} resource(s).",
        )
    }

    private fun write(
        spec: FileSpec,
        origin: KSFile?,
        aggregating: Boolean,
        allFiles: List<KSFile> = emptyList(),
    ) {
        val dependencies = when {
            aggregating -> Dependencies(aggregating = true, *(allFiles.toTypedArray()))
            origin != null -> Dependencies(aggregating = false, origin)
            else -> Dependencies(aggregating = false)
        }
        val stream = codeGenerator.createNewFile(dependencies, spec.packageName, spec.name)
        OutputStreamWriter(stream, StandardCharsets.UTF_8).use { writer -> spec.writeTo(writer) }
    }

    /**
     * Collects the opt-in declarations: annotated classes, implementors of the marker interface (searched
     * through every source file, since no annotation points at them), and the companion objects of
     * annotated classes, which are part of the class they belong to.
     */
    private fun collectDeclarations(resolver: Resolver): List<KSClassDeclaration> {
        val found = LinkedHashMap<String, KSClassDeclaration>()

        fun add(declaration: KSClassDeclaration) {
            val name = declaration.qualifiedName?.asString() ?: return
            found.putIfAbsent(name, declaration)
            declaration.declarations
                .filterIsInstance<KSClassDeclaration>()
                .firstOrNull { it.isCompanionObject }
                ?.let { companion -> companion.qualifiedName?.asString()?.let { found.putIfAbsent(it, companion) } }
        }

        resolver.getSymbolsWithAnnotation(KmpNames.KMP_REFLECT)
            .filterIsInstance<KSClassDeclaration>()
            .forEach(::add)

        val marker = resolver.getClassDeclarationByName(resolver.getKSNameFromString(KmpNames.KMP_REFLECTABLE))
        if (marker == null) {
            logger.warn("kmp-reflect: '${KmpNames.KMP_REFLECTABLE}' is not on the compile classpath.")
        } else {
            val markerType = marker.asStarProjectedType()
            resolver.getAllFiles()
                .flatMap { it.declarations.allClasses() }
                .filter { declaration -> declaration.qualifiedName?.asString() != KmpNames.KMP_REFLECTABLE }
                .filter { declaration -> markerType.isAssignableFrom(declaration.asStarProjectedType()) }
                .forEach(::add)
        }

        return found.values.toList()
    }

    private fun includeNonPublic(declaration: KSClassDeclaration): Boolean {
        val annotation = declaration.annotations
            .firstOrNull { it.shortName.asString() == "KmpReflect" }
            ?: return declaration.isMarkerOptIn()
        val argument = annotation.arguments
            .firstOrNull { it.name?.asString() == "includeNonPublic" }
            ?.value as? Boolean
        return argument ?: true
    }

    /** Marker-interface opt-ins have no annotation, so they fall back to the annotation's default. */
    private fun KSClassDeclaration.isMarkerOptIn(): Boolean = superTypes.any { it.resolve().declaration.qualifiedName?.asString() == KmpNames.KMP_REFLECTABLE } ||
        superTypes.any { superType ->
            val declaration = superType.resolve().declaration as? KSClassDeclaration ?: return@any false
            declaration.classKind.name == "INTERFACE" && declaration.superTypes.any { it.resolve().declaration.qualifiedName?.asString() == KmpNames.KMP_REFLECTABLE }
        }

    /**
     * A proxy is only generated when the interface's *declared* abstract members are all plain
     * functions this metadata can describe: no properties, no `suspend`, no type parameters, and no
     * inherited abstract member without its own entry.
     */
    private fun isProxyable(declaration: KSClassDeclaration, model: ClassModel): Boolean {
        val abstractFunctions = declaration.getAllFunctions().filter { it.isAbstract }.toList()
        // KSP has no `isAbstract` on properties; comparing counts is cheaper and safer: any property
        // the metadata does not describe is either inherited or abstract, and both block a proxy.
        val allProperties = declaration.getAllProperties().toList()
        if (abstractFunctions.isEmpty()) {
            logger.info("kmp-reflect: '${model.qualifiedName}' has no abstract function; no proxy generated.")
            return false
        }
        if (allProperties.isNotEmpty()) {
            logger.warn(
                "kmp-reflect: '${model.qualifiedName}' declares properties, which proxies do not " +
                    "support in v0.1; no proxy generated.",
                declaration,
            )
            return false
        }
        val inherited = abstractFunctions.filter {
            it.parentDeclaration?.qualifiedName?.asString() != model.qualifiedName
        }
        if (inherited.isNotEmpty()) {
            logger.warn(
                "kmp-reflect: '${model.qualifiedName}' inherits abstract members " +
                    "(${inherited.joinToString { it.simpleName.asString() }}); no proxy generated.",
                declaration,
            )
            return false
        }
        val unsupported = abstractFunctions.filter { function ->
            Modifier.SUSPEND in function.modifiers ||
                function.typeParameters.isNotEmpty() ||
                function.parameters.any { it.type.resolve().declaration is com.google.devtools.ksp.symbol.KSTypeParameter } ||
                function.returnType?.resolve()?.declaration is com.google.devtools.ksp.symbol.KSTypeParameter
        }
        if (unsupported.isNotEmpty()) {
            logger.warn(
                "kmp-reflect: '${model.qualifiedName}' has members a proxy cannot forward " +
                    "(${unsupported.joinToString { it.simpleName.asString() }}); no proxy generated.",
                declaration,
            )
            return false
        }
        val missingMetadata = abstractFunctions.any { function ->
            model.functions.none { candidate ->
                candidate.name == function.simpleName.asString() &&
                    candidate.parameters.size == function.parameters.size &&
                    candidate.invokable
            }
        }
        if (missingMetadata) {
            logger.warn(
                "kmp-reflect: '${model.qualifiedName}' has members without generated metadata; no proxy generated.",
                declaration,
            )
            return false
        }
        return true
    }
}

/** Every class declaration nested somewhere under these declarations. */
private fun Sequence<KSDeclaration>.allClasses(): Sequence<KSClassDeclaration> = flatMap { declaration ->
    if (declaration is KSClassDeclaration) {
        sequenceOf(declaration) + declaration.declarations.allClasses()
    } else {
        emptySequence()
    }
}
