package io.github.shijianjs.kmp.reflect.ksp

/** Maps a file extension to the MIME type recorded in the inlined resource. */
internal object MimeTypes {
    private val BY_EXTENSION: Map<String, String> = mapOf(
        "txt" to "text/plain",
        "md" to "text/markdown",
        "html" to "text/html",
        "htm" to "text/html",
        "css" to "text/css",
        "csv" to "text/csv",
        "json" to "application/json",
        "jsonl" to "application/x-ndjson",
        "xml" to "application/xml",
        "yaml" to "application/yaml",
        "yml" to "application/yaml",
        "toml" to "application/toml",
        "svg" to "image/svg+xml",
        "png" to "image/png",
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "gif" to "image/gif",
        "webp" to "image/webp",
        "ico" to "image/x-icon",
        "pdf" to "application/pdf",
        "zip" to "application/zip",
        "gz" to "application/gzip",
        "wasm" to "application/wasm",
        "mp3" to "audio/mpeg",
        "wav" to "audio/wav",
        "ogg" to "audio/ogg",
        "mp4" to "video/mp4",
        "webm" to "video/webm",
        "ttf" to "font/ttf",
        "otf" to "font/otf",
        "woff" to "font/woff",
        "woff2" to "font/woff2",
        "kt" to "text/x-kotlin",
        "js" to "text/javascript",
        "mjs" to "text/javascript",
    )

    const val DEFAULT: String = "application/octet-stream"

    /** The MIME type for [fileName], guessed from its extension. */
    fun of(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return BY_EXTENSION[extension] ?: DEFAULT
    }
}
