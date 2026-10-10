package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.DecryptionState
import viewmodel.LogDecrypterViewModel

@Composable
fun MainScreen(viewModel: LogDecrypterViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Log Decrypter",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (uiState) {
                    is DecryptionState.Decrypting -> "Decrypting your logs"
                    is DecryptionState.Success -> "Decryption completed"
                    is DecryptionState.Error -> "Decryption failed"
                    else -> "Decrypt encrypted log files"
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            when (val state = uiState) {
                DecryptionState.Idle -> IdleOrReadyView(
                    files = emptyList(),
                    keyFile = null,
                    onFilesSelected = viewModel::addFiles,
                    onRemoveFile = viewModel::removeFile,
                    onClearAll = viewModel::clearFiles,
                    onKeySelected = viewModel::setKeyFile,
                    onStartDecryption = viewModel::startDecryption
                )

                is DecryptionState.Ready -> IdleOrReadyView(
                    files = state.files,
                    keyFile = state.keyFile,
                    onFilesSelected = viewModel::addFiles,
                    onRemoveFile = viewModel::removeFile,
                    onClearAll = viewModel::clearFiles,
                    onKeySelected = viewModel::setKeyFile,
                    onStartDecryption = viewModel::startDecryption
                )

                is DecryptionState.Decrypting -> DecryptionProgress(state)

                is DecryptionState.Success -> SuccessScreen(
                    state = state,
                    onOpenCombinedLog = viewModel::openCombinedLog,
                    onShowInFinder = viewModel::showInFinder,
                    onDecryptAgain = viewModel::reset
                )

                is DecryptionState.Error -> ErrorScreen(
                    state = state,
                    onTryAgain = viewModel::reset
                )
            }
        }
    }
}

@Composable
private fun IdleOrReadyView(
    files: List<model.LogFile>,
    keyFile: java.io.File?,
    onFilesSelected: (List<java.io.File>) -> Unit,
    onRemoveFile: (model.LogFile) -> Unit,
    onClearAll: () -> Unit,
    onKeySelected: (java.io.File) -> Unit,
    onStartDecryption: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (files.isEmpty()) {
            FileDropZone(onFilesSelected = onFilesSelected)
        } else {
            SelectedFileList(
                files = files,
                onAddMore = onFilesSelected,
                onRemoveFile = onRemoveFile,
                onClearAll = onClearAll
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        KeySelector(
            keyFile = keyFile,
            onKeySelected = onKeySelected
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStartDecryption,
            enabled = files.isNotEmpty() && keyFile != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "Decrypt",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Files are processed locally.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
