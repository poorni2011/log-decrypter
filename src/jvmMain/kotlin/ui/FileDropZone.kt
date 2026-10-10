package ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun FileDropZone(
    onFilesSelected: (List<File>) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                openNativeFilePicker(onFilesSelected)
            },
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📄",
                fontSize = 40.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Drop encrypted log files here",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "or",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { openNativeFilePicker(onFilesSelected) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Select Log Files", fontSize = 14.sp)
            }
        }
    }
}

fun openNativeFilePicker(onFilesSelected: (List<File>) -> Unit) {
    try {
        val fileDialog = FileDialog(null as Frame?, "Select Encrypted Log Files", FileDialog.LOAD)
        fileDialog.isMultipleMode = true
        fileDialog.isVisible = true

        val files = fileDialog.files
        if (files != null && files.isNotEmpty()) {
            onFilesSelected(files.toList())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
