package service

import java.awt.Desktop
import java.io.File

/**
 * Handles file system operations, output directory management,
 * combined log generation, and macOS Finder integration.
 */
class FileService {

    fun getOutputDirectory(sampleInputFile: File? = null): File {
        val userHome = System.getProperty("user.home")
        val downloadsDir = File(userHome, "Downloads")
        val outputDir = if (downloadsDir.exists()) {
            File(downloadsDir, "logs-decrypted")
        } else {
            File(userHome, "logs-decrypted")
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        return outputDir
    }

    fun getDecryptedOutputFile(inputFile: File, outputDir: File): File {
        val baseName = inputFile.nameWithoutExtension
        val ext = inputFile.extension
        val newName = if (ext.isNotBlank()) "${baseName}_dec.$ext" else "${baseName}_dec"
        return File(outputDir, newName)
    }

    fun createCombinedLogFile(outputDir: File, decryptedFiles: List<File>): File {
        val combinedFile = File(outputDir, "combined_decrypted_logs.log")
        combinedFile.bufferedWriter().use { writer ->
            decryptedFiles.forEach { file ->
                if (!file.isFile) return@forEach
                file.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        writer.appendLine(line)
                    }
                }
            }
        }
        return combinedFile
    }

    fun openFile(file: File): Boolean {
        if (!file.exists()) return false
        return try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file)
                true
            } else {
                ProcessBuilder("open", file.absolutePath).start()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun showInFinder(fileOrDir: File): Boolean {
        if (!fileOrDir.exists()) return false
        return try {
            ProcessBuilder("open", "-R", fileOrDir.absolutePath).start()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
