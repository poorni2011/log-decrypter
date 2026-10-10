package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.DecryptionState

@Composable
fun SuccessScreen(
    state: DecryptionState.Success,
    onOpenCombinedLog: () -> Unit,
    onShowInFinder: () -> Unit,
    onDecryptAgain: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✓",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Decryption Done",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp)
            )

            Text(
                text = "${state.decryptedFiles.size} files decrypted",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            Button(
                onClick = onOpenCombinedLog,
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(44.dp)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Open Combined Log")
            }

            OutlinedButton(
                onClick = onShowInFinder,
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Open Output Folder")
            }

            OutlinedButton(
                onClick = onDecryptAgain,
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Decrypt Again")
            }
        }
    }
}
