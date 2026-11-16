package io.github.shijianjs.kmp.reflect.ksp

import com.google.devtools.ksp.processing.KSPLogger
import java.io.File
import java.util.Base64

/** One resource that will be inlined into generated code. */
internal class ResourceEntry(
    /** Path relative to its configured root, `/`-separated. */
    val path: String,
    val mimeType: String,
    val size: Int,
    val base64: String,
)

/**
 * Reads the configured resource directories and turns every file into an entry the writer can inline.
 *
 * Paths are relative to the directory they were found in, so the configured roots never leak into the
 * API. The first directory wins when two roots contain the same path.
 */
internal class ResourceScanner(private val logger: KSPLogger) {
    fun scan(roots: List<File>): List<ResourceEntry> {
        val byPath = LinkedHashMap<String, ResourceEntry>()
        roots.forEach { root ->
            val files = root.walkTopDown()
                .filter { it.isFile }
                .sortedBy { it.relativeTo(root).invariantSeparatorsPath }
            files.forEach { file ->
                val path = file.relativeTo(root).invariantSeparatorsPath
                if (path.contains('\\')) {
                    logger.warn("Resource path '$path' contains a backslash; it will not be addressable.")
                }
                if (path in byPath) {
                    logger.warn("Resource '$path' appears in more than one configured directory; keeping the first one.")
                    return@forEach
                }
                byPath[path] = entryOf(file, path)
            }
        }
        return byPath.values.sortedBy { it.path }
    }

    private fun entryOf(file: File, path: String): ResourceEntry {
        val bytes = file.readBytes()
        return ResourceEntry(
            path = path,
            mimeType = MimeTypes.of(file.name),
            size = bytes.size,
            base64 = Base64.getEncoder().encodeToString(bytes),
        )
    }
}
