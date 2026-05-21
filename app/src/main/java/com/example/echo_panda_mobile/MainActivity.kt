package com.example.echo_panda_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.echo_panda_mobile.data.model.AppSettings
import com.example.echo_panda_mobile.data.repository.SettingsRepository
import com.example.echo_panda_mobile.presentation.views.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draws content behind system bars (status bar, nav bar)
        enableEdgeToEdge()

        setContent {
            val settingsRepo = remember(this) { SettingsRepository(this) }
            val settings by settingsRepo.settingsFlow.collectAsState(
                initial = AppSettings()
            )

            MyApplicationTheme(isDarkMode = settings.isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        isDarkMode = settings.isDarkMode,
                        language = settings.language
                    )
                }
            }
        }
    }
}
