package ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkMacColors = darkColorScheme(
    primary = Color(0xFF0A84FF),       // macOS System Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1C273C),
    onPrimaryContainer = Color(0xFF70B5FF),
    background = Color(0xFF1E1E1E),     // Dark developer background
    onBackground = Color(0xFFE6E6E6),
    surface = Color(0xFF252526),        // Card background
    onSurface = Color(0xFFCCCCCC),
    surfaceVariant = Color(0xFF2D2D2E),
    onSurfaceVariant = Color(0xFFA6A6A6),
    outline = Color(0xFF3E3E42),        // Subtle borders
    error = Color(0xFFFF453A),          // macOS System Red
    onError = Color.White
)

@Composable
fun MacTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkMacColors,
        content = content
    )
}
