package io.github.shijianjs.kmp.reflect.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Entry point the KSP service loader instantiates; the `META-INF/services` file names this class.
 */
public class KmpReflectProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        KmpReflectProcessor(environment)
}
