package com.theopsking.toolbox.screenshots

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.performClick
import com.theopsking.toolbox.games.offthescale.OffTheScaleScreen
import com.theopsking.toolbox.miniApps
import com.theopsking.toolbox.ui.HomeScreen
import com.theopsking.toolbox.ui.theme.ToolboxTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Renders screens to app/build/screenshots so they can be checked without a phone. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w412dp-h915dp-xxhdpi")
class ScreenshotTest {
    @get:Rule val compose = createComposeRule()

    private fun save(name: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val dir = File("build/screenshots").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun home() {
        compose.setContent { ToolboxTheme { HomeScreen(apps = miniApps, onOpen = {}) } }
        save("home")
    }

    @Test fun offTheScale() {
        compose.setContent { ToolboxTheme { OffTheScaleScreen(onBack = {}) } }
        save("off_the_scale")
        compose.onNodeWithText("PICK A SCALE").assertExists()
    }
}
