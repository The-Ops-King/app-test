package com.theopsking.toolbox.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** L-shaped marks in the corners, the framing detail from jtylerray.com. */
fun Modifier.cornerBrackets(color: Color, length: Dp = 14.dp, stroke: Dp = 2.dp, inset: Dp = 8.dp) =
    drawWithContent {
        drawContent()
        val l = length.toPx()
        val s = stroke.toPx()
        val i = inset.toPx()
        val w = size.width
        val h = size.height
        fun line(a: Offset, b: Offset) = drawLine(color, a, b, s, StrokeCap.Square)
        line(Offset(i, i), Offset(i + l, i)); line(Offset(i, i), Offset(i, i + l))
        line(Offset(w - i, i), Offset(w - i - l, i)); line(Offset(w - i, i), Offset(w - i, i + l))
        line(Offset(i, h - i), Offset(i + l, h - i)); line(Offset(i, h - i), Offset(i, h - i - l))
        line(Offset(w - i, h - i), Offset(w - i - l, h - i)); line(Offset(w - i, h - i), Offset(w - i, h - i - l))
    }
