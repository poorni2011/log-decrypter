package service

import model.DecryptionResult
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.util.concurrent.TimeUnit

/**
 * Runs the bundled DecryptLog command-line tool.
 *
 * The real DecryptLog binary is packaged as a JVM resource and extracted to a
 * private application directory before execution. No shell script is required.
 */
class ProcessService {

    companion object {
        private const val TOOL_RESOURCE = "/decryption/DecryptLog"
        private const val TOOL_NAME = "DecryptLog"
        private const val TIMEOUT_SECONDS = 120L
    }

    private var extractedTool: File? = null

    fun executeDecryption(
        inputFile: File,
        outputFile: File,
        keyFile: File
    ): DecryptionResult {
        val absInput = inputFile.canonicalFile
        val absKey = keyFile.canonicalFile
        val absOutput = outputFile.canonicalFile

        require(absInput.isFile && absInput.canRead()) {
            "Cannot read encrypted log file: ${absInput.absolutePath}"
        }
        require(absKey.isFile && absKey.canRead()) {
            "Cannot read key file: ${absKey.absolutePath}"
        }

        absOutput.parentFile?.mkdirs()

        val executable = getBundledTool()
        val command = mutableListOf<String>()

        // The bundled binary is x86_64. On Apple Silicon, run it through
        // Rosetta. On Intel, execute it directly.
        if (isArm64Host()) {
            command += listOf("/usr/bin/arch", "-x86_64")
        }

        command += listOf(
            executable.absolutePath,
            absInput.absolutePath,
            absKey.absolutePath,
            "no-table"
        )

        val process = try {
            ProcessBuilder(command)
                .directory(executable.parentFile)
                .start()
        } catch (e: Exception) {
            throw IllegalStateException(
                "Could not launch $TOOL_NAME: ${e.message}",
                e
            )
        }

        val stdout = StringBuilder()
        val stderr = StringBuilder()

        val stdoutThread = Thread {
            process.inputStream.bufferedReader().use { reader ->
                stdout.append(reader.readText())
            }
        }.apply { name = "decryptlog-stdout" }

        val stderrThread = Thread {
            process.errorStream.bufferedReader().use { reader ->
                stderr.append(reader.readText())
            }
        }.apply { name = "decryptlog-stderr" }

        stdoutThread.start()
        stderrThread.start()

        val completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (!completed) {
            process.destroyForcibly()
        }

        stdoutThread.join(5_000)
        stderrThread.join(5_000)

        val exitCode = if (completed) process.exitValue() else -1
        val stdoutText = stdout.toString()
        val stderrText = stderr.toString()
        val success = completed && exitCode == 0

        if (success) {
            absOutput.writeText(stdoutText)
        }

        val details = buildString {
            if (!completed) {
                append("$TOOL_NAME timed out after $TIMEOUT_SECONDS seconds.")
            } else {
                append("$TOOL_NAME exited with code $exitCode.")
            }

            if (stderrText.isNotBlank()) {
                appendLine()
                appendLine("Diagnostic output:")
                append(stderrText)
            }
        }

        return DecryptionResult(
            inputFile = absInput,
            outputFile = if (success) absOutput else null,
            isSuccess = success,
            exitCode = exitCode,
            stdout = stdoutText,
            stderr = details
        )
    }

    private fun getBundledTool(): File {
        extractedTool?.let { if (it.isFile) return it }

        val input = ProcessService::class.java.getResourceAsStream(TOOL_RESOURCE)
            ?: throw IllegalStateException(
                "Bundled $TOOL_NAME was not found in the application resources."
            )

        val toolDir = File(
            System.getProperty("user.home"),
            ".log-decrypter/tools"
        )

        if (!toolDir.exists() && !toolDir.mkdirs()) {
            throw IllegalStateException(
                "Could not create tool directory: ${toolDir.absolutePath}"
            )
        }

        val target = File(toolDir, TOOL_NAME)

        input.use { stream ->
            stream.copyTo(target.outputStream())
        }

        makeExecutable(target)
        extractedTool = target
        return target
    }

    private fun makeExecutable(file: File) {
        if (file.setExecutable(true, false)) return

        // Best-effort POSIX permission update for macOS.
        try {
            val permissions = Files.getPosixFilePermissions(file.toPath()).toMutableSet()
            permissions += PosixFilePermission.OWNER_EXECUTE
            permissions += PosixFilePermission.GROUP_EXECUTE
            permissions += PosixFilePermission.OTHERS_EXECUTE
            Files.setPosixFilePermissions(file.toPath(), permissions)
        } catch (_: Exception) {
            throw IllegalStateException(
                "Could not make bundled $TOOL_NAME executable: ${file.absolutePath}"
            )
        }
    }

    private fun isArm64Host(): Boolean {
        val arch = System.getProperty("os.arch") ?: ""
        return arch.contains("aarch64", ignoreCase = true) ||
            arch.contains("arm64", ignoreCase = true)
    }
}
