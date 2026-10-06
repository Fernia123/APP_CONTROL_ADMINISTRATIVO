package com.example.ctpa

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ctpa.ui.navigation.AppNavigation
import com.example.ctpa.ui.theme.CTPATheme
import com.example.ctpa.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.graphics.Color as ComposeColor

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDark = themeViewModel.isDarkMode()

            // Barras de estado/navegación acordes al tema de la app
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as? Activity)?.window ?: return@SideEffect
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.isAppearanceLightStatusBars = !isDark
                    controller.isAppearanceLightNavigationBars = !isDark
                    // En APIs < 29 la barra de navegación necesita color explícito
                    window.navigationBarColor =
                        (if (isDark) ComposeColor(0xFF0A0F1A) else ComposeColor(0xFFF9FAFB)).toArgb()
                }
            }

            CTPATheme(isDark = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
