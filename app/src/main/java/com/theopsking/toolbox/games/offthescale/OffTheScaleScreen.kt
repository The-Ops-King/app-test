package com.theopsking.toolbox.games.offthescale

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.theopsking.toolbox.R
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffTheScaleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val deck = remember {
        Deck.parse(context.assets.open(Deck.ASSET_PATH).bufferedReader().use { it.readText() })
    }
    var seed by rememberSaveable { mutableLongStateOf(Random.nextLong()) }
    var position by rememberSaveable { mutableIntStateOf(0) }
    var pickedScale by rememberSaveable { mutableStateOf<Int?>(null) }

    val card = deck[Deck.cardIndexAt(seed, position, deck.size)]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Off the Scale") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                Button(
                    onClick = {
                        position++
                        pickedScale = null
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text("Next card", style = MaterialTheme.typography.titleMedium)
                }
            }
        },
    ) { padding ->
        AnimatedContent(targetState = card, label = "card", modifier = Modifier.padding(padding)) { shown ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Text(
                        shown.text,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 32.dp),
                    )
                }
                Text(
                    "Pick a scale",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                shown.scales.forEachIndexed { i, scale ->
                    ScaleRow(
                        scale = scale,
                        picked = pickedScale == i,
                        dimmed = pickedScale != null && pickedScale != i,
                        onClick = { pickedScale = if (pickedScale == i) null else i },
                    )
                }
                Text(
                    "Card ${position % deck.size + 1} of ${deck.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

@Composable
private fun ScaleRow(scale: Scale, picked: Boolean, dimmed: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (picked) colors.secondaryContainer else colors.surfaceContainer,
        border = if (picked) BorderStroke(2.dp, colors.secondary) else null,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ScaleEnd(number = "1", label = scale.low, dimmed = dimmed)
            ScaleEnd(number = "10", label = scale.high, dimmed = dimmed)
        }
    }
}

@Composable
private fun ScaleEnd(number: String, label: String, dimmed: Boolean) {
    val alpha = if (dimmed) 0.45f else 1f
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = alpha),
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    number,
                    color = MaterialTheme.colorScheme.onTertiary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
        )
    }
}
