package com.theopsking.toolbox

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.theopsking.toolbox.ui.HomeScreen
import com.theopsking.toolbox.ui.theme.ToolboxTheme
import com.theopsking.toolbox.update.UpdateController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dark system bars to match the always-dark theme.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        UpdateController.init(applicationContext)
        if (savedInstanceState == null) UpdateController.checkForUpdate()
        setContent {
            ToolboxTheme { ToolboxApp() }
        }
    }
}

@Composable
private fun ToolboxApp() {
    var openAppId by rememberSaveable { mutableStateOf<String?>(null) }
    val openApp = miniApps.firstOrNull { it.id == openAppId }

    if (openApp == null) {
        HomeScreen(apps = miniApps, onOpen = { openAppId = it.id })
    } else {
        BackHandler { openAppId = null }
        openApp.content { openAppId = null }
    }
}
