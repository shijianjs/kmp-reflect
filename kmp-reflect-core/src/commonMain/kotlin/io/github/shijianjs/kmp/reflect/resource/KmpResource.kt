package io.github.shijianjs.kmp.reflect.resource

/**
 * A classpath resource that was inlined into generated Kotlin code at compile time.
 *
 * There is no file system and no class-loading involved: the KSP processor read the file and emitted
 * its bytes as a Base64 literal, so the same object behaves identically on the JVM and in a browser.
 * Decoding happens on first access and is cached by the accessor's implementation.
 *
 * @property path the resource path relative to the configured resource root, `/`-separated.
 * @property mimeType the MIME type inferred from the file extension, or `application/octet-stream`.
 * @property size the byte length of the content.
 * @property bytes the decoded content; decodes on every access unless the generated lazy holder is
 * used (generated resources decode once).
 */
public class KmpResource(
    public val path: String,
    public val mimeType: String,
    public val size: Int,
    private val content: () -> ByteArray,
) {
    /** The decoded content. */
    public val bytes: ByteArray
        get() = content()

    /** The content decoded as UTF-8 text. */
    public fun text(): String = bytes.decodeToString()

    /** The directory part of [path], or an empty string for a root-level resource. */
    public val directory: String
        get() = path.substringBeforeLast('/', "")

    /** The file name part of [path]. */
    public val fileName: String
        get() = path.substringAfterLast('/')

    override fun toString(): String = "$path ($mimeType, $size bytes)"
}
