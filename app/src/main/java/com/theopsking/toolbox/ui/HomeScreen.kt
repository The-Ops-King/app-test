package com.theopsking.toolbox.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theopsking.toolbox.BuildConfig
import com.theopsking.toolbox.MiniApp
import com.theopsking.toolbox.ui.theme.Brand
import com.theopsking.toolbox.ui.theme.Eyebrow
import com.theopsking.toolbox.update.UpdateController
import com.theopsking.toolbox.update.UpdateState

@Composable
fun HomeScreen(apps: List<MiniApp>, onOpen: (MiniApp) -> Unit) {
    val updateState by UpdateController.state.collectAsState()

    Scaffold(containerColor = Brand.Ink) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(Modifier.padding(top = 16.dp, bottom = 16.dp)) {
                    Text("TOOLBOX", style = Eyebrow, color = Brand.GreyMid)
                    Text(
                        "Things I built for my phone.",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Brand.Bone,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            item { UpdateBanner(updateState) }
            itemsIndexed(apps, key = { _, app -> app.id }) { index, app ->
                Surface(
                    onClick = { onOpen(app) },
                    shape = RoundedCornerShape(8.dp),
                    color = Brand.Raised,
                    border = BorderStroke(1.dp, Brand.Hair),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("%02d".format(index + 1), style = Eyebrow, color = Brand.GreyMid, modifier = Modifier.padding(top = 4.dp))
                        Column {
                            Text(app.title, style = MaterialTheme.typography.titleLarge, color = Brand.Bone)
                            Text(app.description, style = MaterialTheme.typography.labelMedium, color = Brand.GreyMid)
                        }
                    }
                }
            }
            item {
                Column(
                    Modifier.fillMaxWidth().padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("VERSION ${BuildConfig.VERSION_NAME}", style = Eyebrow, color = Brand.GreyMid)
                    val failed = updateState as? UpdateState.Failed
                    if (failed != null && failed.manifest == null) {
                        Text(failed.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { UpdateController.checkForUpdate() }) {
                        Text(
                            if (updateState is UpdateState.UpToDate) "Up to date. Check again" else "Check for updates",
                            color = Brand.AccentText,
                        )
                    }
                }
            }
        }
    }
}
