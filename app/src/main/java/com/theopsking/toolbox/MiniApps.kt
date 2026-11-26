package com.theopsking.toolbox

import androidx.compose.runtime.Composable
import com.theopsking.toolbox.games.offthescale.OffTheScaleScreen

/**
 * Every idea is a mini app registered here. Adding one means writing its screen
 * and adding an entry to [miniApps]. The id is saved across restarts, so keep it stable.
 */
data class MiniApp(
    val id: String,
    val title: String,
    val description: String,
    val content: @Composable (onBack: () -> Unit) -> Unit,
)

val miniApps: List<MiniApp> = listOf(
    MiniApp(
        id = "off_the_scale",
        title = "Off the Scale",
        description = "Draw a prompt and pick one of its three scales.",
    ) { onBack -> OffTheScaleScreen(onBack = onBack) },
)
