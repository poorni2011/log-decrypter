package model

import java.io.File

/**
 * Execution result for an individual log file decryption operation.
 */
data class DecryptionResult(
    val inputFile: File,
    val outputFile: File?,
    val isSuccess: Boolean,
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)
