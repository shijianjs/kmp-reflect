package io.github.shijianjs.kmp.reflect.gradle

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property

/**
 * Configuration for the `io.github.shijianjs.kmp-reflect` plugin.
 *
 * ```kotlin
 * kmpReflect {
 *     registry.set("com.example.reflect:AppReflection")
 *     module.set("app")
 *     resources.from("src/commonMain/resources")
 * }
 * ```
 *
 * Everything but [registry] is optional, and the processor itself is added to every target's KSP
 * configuration automatically — the whole point of the plugin, since KSP 2 registers the processor
 * per target and has no catch-all configuration.
 */
public abstract class KmpReflectExtension {
    /**
     * `package:ObjectName` of the generated `KmpReflectModule`. Without it no module object is
     * generated and resources cannot be inlined, so most projects set this first.
     */
    public abstract val registry: Property<String>

    /** Display name recorded in the module object; defaults to the registry object's name. */
    public abstract val module: Property<String>

    /** `all` (default) reflects `internal`/`protected` members; `public` reflects only public ones. */
    public abstract val visibility: Property<String>

    /**
     * Directories whose files are inlined into the generated code as Base64, with path and MIME type.
     *
     * They are also declared as inputs of the KSP tasks, so editing a resource reruns processing instead
     * of leaving a stale copy compiled in.
     */
    public abstract val resources: ConfigurableFileCollection

    /**
     * Whether the plugin adds the processor to each target's `ksp<Target>` configuration.
     *
     * Defaults to `true`; set it to `false` to own the dependency wiring yourself, for example when the
     * processor is substituted through a composite build.
     */
    public abstract val registerProcessor: Property<Boolean>

    /**
     * Coordinates of the processor artifact, used when [processorProject] is unset.
     *
     * Defaults to `io.github.shijianjs:kmp-reflect-ksp:<this plugin's version>`.
     */
    public abstract val processorCoordinates: Property<String>

    /**
     * Project path of the processor, for builds that compile it themselves (`":kmp-reflect-ksp"`).
     *
     * Unset for consumers: they resolve [processorCoordinates] like any other dependency. This
     * repository sets it in `:demo` so the integration test runs against the sources under development.
     */
    public abstract val processorProject: Property<String>
}
