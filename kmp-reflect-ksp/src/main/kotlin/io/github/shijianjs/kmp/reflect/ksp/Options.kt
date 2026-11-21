package io.github.shijianjs.kmp.reflect.ksp

import com.google.devtools.ksp.processing.KSPLogger
import java.io.File

/** Where the aggregated module object is generated. */
internal data class RegistryTarget(val packageName: String, val objectName: String)

/**
 * The processor options, all optional.
 *
 * | option | meaning |
 * | --- | --- |
 * | `kmp.reflect.registry` | `package:ObjectName`; enables the generated `KmpReflectModule` and resource inlining |
 * | `kmp.reflect.module` | display name recorded in the module object |
 * | `kmp.reflect.resources` | resource directories, separated by `,`, `;` or the platform path separator |
 * | `kmp.reflect.visibility` | `all` (default) reflects `internal` members, `public` reflects only `public` ones |
 */
internal class Options(
    val registry: RegistryTarget?,
    val moduleName: String,
    val resourceDirs: List<File>,
    val publicOnly: Boolean,
) {
    companion object {
        private const val REGISTRY = "kmp.reflect.registry"
        private const val MODULE = "kmp.reflect.module"
        private const val RESOURCES = "kmp.reflect.resources"
        private const val VISIBILITY = "kmp.reflect.visibility"

        fun parse(options: Map<String, String>, logger: KSPLogger): Options {
            val registry = options[REGISTRY]?.trim()?.takeIf { it.isNotEmpty() }?.let { value ->
                val separator = value.lastIndexOf(':')
                if (separator <= 0 || separator == value.lastIndex) {
                    logger.error("Option '$REGISTRY' must be written as '<package>:<ObjectName>', got '$value'.")
                    null
                } else {
                    RegistryTarget(value.substring(0, separator), value.substring(separator + 1))
                }
            }
            val moduleName = options[MODULE]?.trim()?.takeIf { it.isNotEmpty() }
                ?: registry?.objectName
                ?: "module"
            val resourceDirs = options[RESOURCES]
                ?.split(',', ';', File.pathSeparatorChar)
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.map { File(it) }
                ?.filter { directory ->
                    val exists = directory.isDirectory
                    if (!exists) {
                        logger.warn("Resource directory '$directory' does not exist; skipping it.")
                    }
                    exists
                }
                .orEmpty()
            val visibility = options[VISIBILITY]?.trim()?.lowercase()
            if (visibility != null && visibility !in setOf("all", "public")) {
                logger.warn("Option '$VISIBILITY' expects 'all' or 'public', got '$visibility'; using 'all'.")
            }
            return Options(
                registry = registry,
                moduleName = moduleName,
                resourceDirs = resourceDirs,
                publicOnly = visibility == "public",
            )
        }
    }
}
