import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.MainScreen
import ui.theme.MacTheme
import viewmodel.LogDecrypterViewModel


fun main() = application {
    val windowState = rememberWindowState(size = DpSize(580.dp, 740.dp))
    val viewModel = remember { LogDecrypterViewModel() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Log Decrypter",
        state = windowState
    ) {
        MacTheme {
            MainScreen(viewModel = viewModel)
        }
    }
}
