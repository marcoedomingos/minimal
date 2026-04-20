package ao.marco.domingos.minimal.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Black,
    surface = Gray900,
    onPrimary = Black,
    onBackground = White,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = White,
    surface = Gray100,
    onPrimary = White,
    onBackground = Black,
    onSurface = Black
)

@Composable
fun MinimalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}