package service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.LogFile
import java.io.File

/**
 * Coordinates batch decryption using the bundled DecryptLog tool.
 */
class DecryptionService(
    private val processService: ProcessService = ProcessService(),
    private val fileService: FileService = FileService()
) {

    suspend fun decryptLogs(
        logFiles: List<LogFile>,
        keyFile: File,
        onProgress: (currentFile: String, currentIndex: Int, totalFiles: Int, percent: Float) -> Unit
    ): BatchDecryptionResult = withContext(Dispatchers.IO) {
        if (logFiles.isEmpty()) {
            return@withContext BatchDecryptionResult.Failure(
                failedFile = "",
                message = "No encrypted log files selected.",
                details = null
            )
        }

        if (!keyFile.isFile || !keyFile.canRead()) {
            return@withContext BatchDecryptionResult.Failure(
                failedFile = "",
                message = "The selected key file cannot be read.",
                details = keyFile.absolutePath
            )
        }

        val outputDir = fileService.getOutputDirectory(logFiles.first().file)
        val decryptedFiles = mutableListOf<File>()

        logFiles.forEachIndexed { index, logFile ->
            val currentIndex = index + 1
            val outputFile = File(outputDir, "ks${currentIndex}_dec.log")

            onProgress(
                logFile.name,
                currentIndex,
                logFiles.size,
                index.toFloat() / logFiles.size
            )

            val result = try {
                processService.executeDecryption(
                    inputFile = logFile.file,
                    outputFile = outputFile,
                    keyFile = keyFile
                )
            } catch (e: Exception) {
                return@withContext BatchDecryptionResult.Failure(
                    failedFile = logFile.name,
                    message = e.message ?: "Could not decrypt ${logFile.name}.",
                    details = e.cause?.message
                )
            }

            if (!result.isSuccess) {
                return@withContext BatchDecryptionResult.Failure(
                    failedFile = logFile.name,
                    message = "Could not decrypt ${logFile.name}.",
                    details = result.stderr.ifBlank { result.stdout }
                )
            }

            decryptedFiles += outputFile
        }

        onProgress(
            "Combining decrypted logs",
            logFiles.size,
            logFiles.size,
            0.95f
        )

        val combinedFile = try {
            fileService.createCombinedLogFile(outputDir, decryptedFiles)
        } catch (e: Exception) {
            return@withContext BatchDecryptionResult.Failure(
                failedFile = "",
                message = "Decryption succeeded, but the combined log could not be created.",
                details = e.message
            )
        }

        onProgress("Completed", logFiles.size, logFiles.size, 1.0f)

        BatchDecryptionResult.Success(
            outputDirectory = outputDir,
            decryptedFiles = decryptedFiles,
            combinedFile = combinedFile
        )
    }

    sealed interface BatchDecryptionResult {
        data class Success(
            val outputDirectory: File,
            val decryptedFiles: List<File>,
            val combinedFile: File
        ) : BatchDecryptionResult

        data class Failure(
            val failedFile: String,
            val message: String,
            val details: String?
        ) : BatchDecryptionResult
    }
}
