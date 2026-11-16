package io.github.shijianjs.kmp.reflect.gradle

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.PathSensitivity

/**
 * Wires the kmp-reflect processor into a project.
 *
 * Three things happen here, all of them things a consumer would otherwise have to repeat per target:
 *
 * 1. the processor is added to every target's `ksp<Target>` configuration (KSP 2 has no catch-all);
 * 2. the `kmp.reflect.*` processor options are forwarded from [KmpReflectExtension];
 * 3. the declared resource directories become inputs of the KSP tasks, so a changed resource is
 *    re-inlined instead of staying at its previous value.
 *
 * Target detection goes through configuration names rather than the Kotlin plugin's API: the KSP
 * plugin creates `ksp<Target>` (and `ksp<Target>Test`) configurations when targets are declared, and
 * a name-based rule keeps this plugin from depending on `kotlin-gradle-plugin-api` — which would tie
 * the plugin to one Kotlin version.
 */
public class KmpReflectPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, KmpReflectExtension::class.java)
        extension.visibility.convention("all")
        extension.registerProcessor.convention(true)
        extension.processorCoordinates.convention("io.github.shijianjs:kmp-reflect-ksp:$KMP_REFLECT_VERSION")

        // Declared on every KSP task, including the test compilations' ones: a task that reads a file
        // without declaring it silently ignores edits to it.
        project.tasks.configureEach { task ->
            if (task.name.isKspTask()) {
                task.inputs.files(extension.resources)
                    .withPropertyName("kmpReflectResources")
                    .withPathSensitivity(PathSensitivity.RELATIVE)
                    .optional()
            }
        }

        project.plugins.withId(KSP_PLUGIN_ID) {
            registerProcessor(project, extension)
            project.afterEvaluate { forwardOptions(project, extension) }
        }

        project.afterEvaluate {
            if (!project.plugins.hasPlugin(KSP_PLUGIN_ID)) {
                project.logger.warn(
                    "kmp-reflect: the KSP plugin is not applied to '${project.path}', so no metadata " +
                        "will be generated. Add `id(\"com.google.devtools.ksp\")`.",
                )
            }
        }
    }

    private fun registerProcessor(project: Project, extension: KmpReflectExtension) {
        val multiplatform = project.plugins.hasPlugin(KMP_PLUGIN_ID)
        project.configurations.configureEach { configuration ->
            if (!extension.registerProcessor.get()) return@configureEach
            if (!configuration.name.isProcessorConfiguration(multiplatform)) return@configureEach
            val dependency = extension.processorProject.orNull
                ?.takeIf { it.isNotBlank() }
                ?.let { path -> project.dependencies.project(mapOf("path" to path)) }
                ?: project.dependencies.create(extension.processorCoordinates.get())
            configuration.dependencies.add(dependency)
        }
    }

    private fun forwardOptions(project: Project, extension: KmpReflectExtension) {
        val ksp = project.extensions.findByType(KspExtension::class.java)
        if (ksp == null) {
            project.logger.warn("kmp-reflect: the KSP extension is missing; processor options were not set.")
            return
        }
        val registry = extension.registry.orNull?.takeIf { it.isNotBlank() }
        registry?.let { ksp.arg(OPTION_REGISTRY, it) }
        extension.module.orNull?.takeIf { it.isNotBlank() }?.let { ksp.arg(OPTION_MODULE, it) }
        extension.visibility.orNull?.takeIf { it.isNotBlank() }?.let { ksp.arg(OPTION_VISIBILITY, it) }

        // The processor takes the directories as one `,`-separated string, which is also what keeps
        // the value stable across hosts (a platform path separator would not be).
        val directories = extension.resources.files.filter { it.isDirectory }.map { it.absolutePath }
        if (directories.isNotEmpty()) {
            if (registry == null) {
                project.logger.warn(
                    "kmp-reflect: resources are configured but 'kmpReflect.registry' is not set; " +
                        "resources are only inlined into the generated module object.",
                )
            }
            ksp.arg(OPTION_RESOURCES, directories.joinToString(","))
        }
    }

    private companion object {
        const val EXTENSION_NAME = "kmpReflect"
        const val KSP_PLUGIN_ID = "com.google.devtools.ksp"
        const val KMP_PLUGIN_ID = "org.jetbrains.kotlin.multiplatform"

        const val OPTION_REGISTRY = "kmp.reflect.registry"
        const val OPTION_MODULE = "kmp.reflect.module"
        const val OPTION_RESOURCES = "kmp.reflect.resources"
        const val OPTION_VISIBILITY = "kmp.reflect.visibility"

        /** `kspKotlinJvm`, `kspKotlinJs`, `kspKotlinMingwX64`, … — the name KSP gives its task. */
        fun String.isKspTask(): Boolean = startsWith("ksp") && length > 3 && !contains("Test")

        /**
         * `kspJvm`, `kspJs`, `kspWasmJs`, `kspMingwX64` — one per target, no test compilations and no
         * common-metadata compilation (metadata would generate into `commonMain` while each target
         * generates its own copy of the same files).
         *
         * The plain `ksp` configuration is the JVM-only project's one; in a multiplatform project it is
         * the deprecated catch-all that KSP 2 no longer understands, so it is left alone there.
         */
        fun String.isProcessorConfiguration(multiplatform: Boolean): Boolean {
            if (this == "ksp") return !multiplatform
            if (!startsWith("ksp")) return false
            if (contains("Metadata") || endsWith("Test")) return false
            return true
        }
    }
}
