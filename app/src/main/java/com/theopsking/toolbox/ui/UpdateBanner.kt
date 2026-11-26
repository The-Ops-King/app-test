package com.theopsking.toolbox.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.theopsking.toolbox.update.UpdateController
import com.theopsking.toolbox.update.UpdateState

@Composable
fun UpdateBanner(state: UpdateState) {
    val context = LocalContext.current
    when (state) {
        UpdateState.Idle, UpdateState.Checking, UpdateState.UpToDate -> Unit

        // A failed check (offline, GitHub down) is shown quietly in the footer instead.
        is UpdateState.Failed -> if (state.manifest != null) {
            BannerCard(state.message) {
                Button(onClick = { UpdateController.install(state.manifest) }) { Text("Try again") }
            }
        }

        is UpdateState.Available -> BannerCard("Version ${state.manifest.versionName} is ready.") {
            Button(onClick = { UpdateController.install(state.manifest) }) { Text("Update now") }
        }

        is UpdateState.NeedsPermission -> BannerCard(
            "Android needs your OK before this app can update itself. Turn on \"Allow from this source\", come back, then tap Update now.",
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { UpdateController.openInstallPermissionSettings(context) }) {
                    Text("Open settings")
                }
                Button(onClick = { UpdateController.install(state.manifest) }) { Text("Update now") }
            }
        }

        is UpdateState.Downloading -> BannerCard("Downloading version ${state.manifest.versionName}...") {
            if (state.progress == null) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            } else {
                LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())
            }
        }

        UpdateState.Installing -> BannerCard("Installing. The app will close when it finishes. Open it again to use the new version.") {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun BannerCard(message: String, actions: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, style = MaterialTheme.typography.bodyLarge)
            actions()
        }
    }
}
