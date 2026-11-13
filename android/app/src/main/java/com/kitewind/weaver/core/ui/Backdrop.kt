package com.kitewind.weaver.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.kitewind.weaver.R
import com.kitewind.weaver.core.theme.*

enum class BackdropKind { Splash, Menu, Flight, Result, Meadow, Gallery }

@Composable
fun Backdrop(kind: BackdropKind, content: @Composable BoxScope.() -> Unit) {
    val colors = when (kind) {
        BackdropKind.Splash -> listOf(BlossomPrimary, BlossomPrimary.copy(alpha = 0.88f), DustyPink.copy(alpha = 0.90f))
        BackdropKind.Menu -> listOf(CandyMist.copy(alpha = 0.94f), PetalBackground.copy(alpha = 0.85f), DustyPink.copy(alpha = 0.52f))
        BackdropKind.Flight -> listOf(CandyMist.copy(alpha = 0.94f), PetalBackground.copy(alpha = 0.80f), DustyPink.copy(alpha = 0.55f))
        BackdropKind.Result -> listOf(DustyPink.copy(alpha = 0.90f), PetalBackground.copy(alpha = 0.93f), CandyMist.copy(alpha = 0.82f))
        BackdropKind.Meadow -> listOf(CandyMist.copy(alpha = 0.90f), PetalBackground.copy(alpha = 0.78f), DustyPink.copy(alpha = 0.60f))
        BackdropKind.Gallery -> listOf(PetalBackground.copy(alpha = 0.95f), CandyMist.copy(alpha = 0.92f), DustyPink.copy(alpha = 0.80f))
    }
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_game), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
        Canvas(Modifier.fillMaxSize()) {
            repeat(3) { index ->
                val path = Path().apply {
                    moveTo(-size.width * 0.15f, size.height * (0.17f + index * 0.12f))
                    cubicTo(size.width * 0.35f, size.height * (0.04f + index * 0.13f), size.width * 0.60f,
                        size.height * (0.32f + index * 0.09f), size.width * 1.15f, size.height * (0.13f + index * 0.12f))
                }
                drawPath(path, if (kind == BackdropKind.Splash) CloudWhite.copy(alpha = 0.12f) else BlossomPrimary.copy(alpha = 0.05f), style = Stroke(width = 2.5f))
            }
        }
        content()
    }
}
