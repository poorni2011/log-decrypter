package model

import java.io.File

/**
 * Represents a selected log file with file metadata.
 */
data class LogFile(
    val file: File,
    val name: String = file.name,
    val sizeBytes: Long = file.length(),
    val sizeFormatted: String = formatFileSize(file.length())
)

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    return String.format("%.1f MB", mb)
}
