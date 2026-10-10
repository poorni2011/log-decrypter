package model

import java.io.File

/**
 * Immutable application UI states for the Log Decrypter workflow.
 */
sealed interface DecryptionState {

    data object Idle : DecryptionState

    data class Ready(
        val files: List<LogFile>,
        val keyFile: File
    ) : DecryptionState

    data class Decrypting(
        val currentFile: String,
        val currentIndex: Int,
        val totalFiles: Int,
        val progressPercent: Float
    ) : DecryptionState

    data class Success(
        val outputDirectory: File,
        val decryptedFiles: List<File>,
        val combinedFile: File
    ) : DecryptionState

    data class Error(
        val message: String,
        val details: String? = null
    ) : DecryptionState
}
