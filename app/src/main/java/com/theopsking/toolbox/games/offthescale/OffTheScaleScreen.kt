package com.theopsking.toolbox.games.offthescale

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theopsking.toolbox.R
import com.theopsking.toolbox.ui.cornerBrackets
import com.theopsking.toolbox.ui.theme.Brand
import com.theopsking.toolbox.ui.theme.Eyebrow
import com.theopsking.toolbox.ui.theme.Mono
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

    val dealt = Dealt(number = position + 1, card = deck[Deck.cardIndexAt(seed, position, deck.size)])

    Scaffold(
        containerColor = Brand.Ink,
        topBar = {
            TopAppBar(
                title = { Text("OFF THE SCALE", style = Eyebrow.copy(fontSize = 14.sp), color = Brand.Bone) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back", tint = Brand.Bone)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brand.Ink),
            )
        },
        bottomBar = {
            // navigationBarsPadding keeps the button above Android's home/back bar.
            Box(Modifier.fillMaxWidth().background(Brand.Ink).navigationBarsPadding().padding(16.dp)) {
                Button(
                    onClick = {
                        position++
                        pickedScale = null
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand.Accent, contentColor = Brand.Bone),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text("Deal next card", style = MaterialTheme.typography.titleMedium)
                }
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = dealt,
            transitionSpec = {
                (slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280)))
                    .togetherWith(slideOutHorizontally(tween(200)) { -it / 3 } + fadeOut(tween(200)))
            },
            label = "deal",
            modifier = Modifier.padding(padding),
        ) { shown ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PromptCard(shown)
                Text(
                    "PICK A SCALE",
                    style = Eyebrow,
                    color = Brand.GreyMid,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                )
                shown.card.scales.forEachIndexed { i, scale ->
                    ScaleOption(
                        scale = scale,
                        picked = pickedScale == i,
                        dimmed = pickedScale != null && pickedScale != i,
                        onClick = { pickedScale = if (pickedScale == i) null else i },
                    )
                }
            }
        }
    }
}

private data class Dealt(val number: Int, val card: PromptCard)

@Composable
private fun PromptCard(dealt: Dealt) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brand.Raised, RoundedCornerShape(8.dp))
            .border(1.dp, Brand.Hair, RoundedCornerShape(8.dp))
            .cornerBrackets(Brand.HairStrong, inset = 10.dp)
            .padding(horizontal = 28.dp, vertical = 36.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("CARD %03d".format(dealt.number), style = Eyebrow, color = Brand.AccentText)
            Text(
                dealt.card.text,
                style = MaterialTheme.typography.headlineSmall.copy(lineHeight = 32.sp),
                color = Brand.Bone,
            )
        }
    }
}

@Composable
private fun ScaleOption(scale: Scale, picked: Boolean, dimmed: Boolean, onClick: () -> Unit) {
    val alpha = if (dimmed) 0.4f else 1f
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (picked) Brand.AccentSoft else Brand.Raised,
        border = BorderStroke(1.dp, if (picked) Brand.Accent else Brand.Hair),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (scale.phrase != null) {
                Text(
                    scale.phrase,
                    style = MaterialTheme.typography.titleMedium,
                    color = Brand.Bone.copy(alpha = alpha),
                )
            }
            ScaleBar(scale, picked, alpha)
        }
    }
}

/** 1 ←─┼─┼─┼─┼─┼─┼─┼─┼─→ 10, with the end labels underneath. */
@Composable
private fun ScaleBar(scale: Scale, picked: Boolean, alpha: Float) {
    val hot = if (picked) Brand.AccentText else Brand.Accent
    val numberStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = Mono, fontWeight = FontWeight.Bold)
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("1", style = numberStyle, color = Brand.GreyMid.copy(alpha = alpha), modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
            Canvas(Modifier.weight(1f).height(24.dp).padding(horizontal = 6.dp)) {
                val y = size.height / 2
                val brush = Brush.horizontalGradient(listOf(Brand.GreyMid.copy(alpha = alpha), hot.copy(alpha = alpha)))
                val stroke = 2.dp.toPx()
                val head = 7.dp.toPx()
                drawLine(brush, Offset(0f, y), Offset(size.width, y), stroke, StrokeCap.Round)
                // arrowheads
                drawLine(brush, Offset(0f, y), Offset(head, y - head), stroke, StrokeCap.Round)
                drawLine(brush, Offset(0f, y), Offset(head, y + head), stroke, StrokeCap.Round)
                drawLine(brush, Offset(size.width, y), Offset(size.width - head, y - head), stroke, StrokeCap.Round)
                drawLine(brush, Offset(size.width, y), Offset(size.width - head, y + head), stroke, StrokeCap.Round)
                // 10 ticks, one per number
                val tick = 5.dp.toPx()
                val start = head + 4.dp.toPx()
                val span = size.width - 2 * start
                for (i in 0 until 10) {
                    val x = start + span * i / 9f
                    val c = lerp(Brand.GreyMid, hot, i / 9f).copy(alpha = alpha)
                    drawLine(c, Offset(x, y - tick), Offset(x, y + tick), 1.5.dp.toPx(), StrokeCap.Round)
                }
            }
            Text("10", style = numberStyle, color = hot.copy(alpha = alpha), modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
        }
        Row(Modifier.fillMaxWidth()) {
            Text(scale.low.uppercase(), style = Eyebrow, color = Brand.GreyMid.copy(alpha = alpha), modifier = Modifier.weight(1f))
            Text(scale.high.uppercase(), style = Eyebrow, color = hot.copy(alpha = alpha), textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
    }
}

private fun lerp(a: Color, b: Color, t: Float) = androidx.compose.ui.graphics.lerp(a, b, t)
