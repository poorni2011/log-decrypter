package viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.DecryptionState
import model.LogFile
import service.DecryptionService
import service.FileService
import java.io.File

class LogDecrypterViewModel(
    private val decryptionService: DecryptionService = DecryptionService(),
    private val fileService: FileService = FileService()
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var decryptionJob: Job? = null

    private val selectedFiles = mutableListOf<LogFile>()
    private var selectedKeyFile: File? = null

    private val _uiState = MutableStateFlow<DecryptionState>(DecryptionState.Idle)
    val uiState: StateFlow<DecryptionState> = _uiState.asStateFlow()

    fun addFiles(files: List<File>) {
        val validLogFiles = files
            .filter { it.isFile && (it.extension.lowercase() == "log" || it.extension.isBlank() || it.name.contains("log", ignoreCase = true)) }
            .map { LogFile(it) }

        validLogFiles.forEach { newFile ->
            if (selectedFiles.none { it.file.absolutePath == newFile.file.absolutePath }) {
                selectedFiles.add(newFile)
            }
        }
        updateState()
    }

    fun removeFile(logFile: LogFile) {
        selectedFiles.removeAll { it.file.absolutePath == logFile.file.absolutePath }
        updateState()
    }

    fun clearFiles() {
        selectedFiles.clear()
        updateState()
    }

    fun setKeyFile(file: File) {
        if (file.isFile && (file.name == "CryptLogKey.h" || file.extension == "h" || file.extension == "key" || file.extension.isBlank())) {
            selectedKeyFile = file
        }
        updateState()
    }

    private fun updateState() {
        val key = selectedKeyFile
        _uiState.value = if (selectedFiles.isNotEmpty() && key != null) {
            DecryptionState.Ready(files = selectedFiles.toList(), keyFile = key)
        } else {
            DecryptionState.Idle
        }
    }

    fun startDecryption() {
        val key = selectedKeyFile ?: return
        if (selectedFiles.isEmpty()) return

        val filesToDecrypt = selectedFiles.toList()
        decryptionJob?.cancel()

        decryptionJob = scope.launch {
            _uiState.value = DecryptionState.Decrypting(
                currentFile = filesToDecrypt.first().name,
                currentIndex = 1,
                totalFiles = filesToDecrypt.size,
                progressPercent = 0.0f
            )

            val result = decryptionService.decryptLogs(
                logFiles = filesToDecrypt,
                keyFile = key,
                onProgress = { currentFile, currentIndex, totalFiles, percent ->
                    _uiState.value = DecryptionState.Decrypting(
                        currentFile = currentFile,
                        currentIndex = currentIndex,
                        totalFiles = totalFiles,
                        progressPercent = percent
                    )
                }
            )

            _uiState.value = when (result) {
                is DecryptionService.BatchDecryptionResult.Success -> {
                    DecryptionState.Success(
                        outputDirectory = result.outputDirectory,
                        decryptedFiles = result.decryptedFiles,
                        combinedFile = result.combinedFile
                    )
                }
                is DecryptionService.BatchDecryptionResult.Failure -> {
                    DecryptionState.Error(
                        message = result.message,
                        details = result.details
                    )
                }
            }
        }
    }

    fun cancelDecryption() {
        decryptionJob?.cancel()
        updateState()
    }

    fun reset() {
        decryptionJob?.cancel()
        selectedFiles.clear()
        selectedKeyFile = null
        _uiState.value = DecryptionState.Idle
    }

    fun openCombinedLog() {
        val state = _uiState.value
        if (state is DecryptionState.Success) {
            fileService.openFile(state.combinedFile)
        }
    }

    fun showInFinder() {
        val state = _uiState.value
        if (state is DecryptionState.Success) {
            fileService.showInFinder(state.outputDirectory)
        }
    }
}
