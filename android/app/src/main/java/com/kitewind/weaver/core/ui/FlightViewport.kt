package com.kitewind.weaver.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.kitewind.weaver.R
import com.kitewind.weaver.core.theme.*
import com.kitewind.weaver.domain.model.FlightPhase
import com.kitewind.weaver.domain.model.FlightSession

@Composable
fun FlightViewport(session: FlightSession, onDrag: (Float) -> Unit, onDragEnd: () -> Unit, modifier: Modifier = Modifier) {
    val reducedMotion = LocalReducedMotion.current
    val summary = stringResource(R.string.flight_summary)
    val phase = stringResource(when (session.phase) {
        FlightPhase.Ready -> R.string.phase_ready; FlightPhase.Playing -> R.string.phase_playing
        FlightPhase.Paused -> R.string.phase_paused; FlightPhase.Finished -> R.string.phase_finished
    })
    BoxWithConstraints(modifier.clip(RoundedCornerShape(18.dp))
        .background(Brush.verticalGradient(listOf(CloudWhite.copy(alpha = 0.58f), CandyMist.copy(alpha = 0.25f))))
        .border(if (session.edgeHoldMs >= 400f) 2.dp else 1.dp, if (session.edgeHoldMs >= 400f) BlossomPrimary else CloudWhite, RoundedCornerShape(18.dp))
        .semantics { contentDescription = summary; stateDescription = phase }
        .pointerInput(session.runId, session.phase) {
            if (session.phase == FlightPhase.Playing) {
                val margin = 40.dp.toPx()
                fun normalized(y: Float) = ((y - margin) / (size.height - margin * 2).coerceAtLeast(1f)).coerceIn(0f, 1f)
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    onDrag(normalized(down.position.y))
                    try {
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (change.pressed) {
                                onDrag(normalized(change.position.y))
                                change.consume()
                            }
                        } while (event.changes.any { it.pressed })
                    } finally {
                        onDragEnd()
                    }
                }
            }
        }) {
        val kiteY = (maxHeight - 80.dp) * session.height
        val kiteX = maxWidth * 0.22f - 32.dp
        Canvas(Modifier.fillMaxSize()) {
            repeat(3) { index ->
                val offset = if (reducedMotion) index * 80f else (session.worldOffset * 50f + index * 80f) % 240f
                val path = Path().apply {
                    moveTo(-offset, size.height * (0.18f + index * 0.20f))
                    cubicTo(size.width * 0.25f, size.height * (0.07f + index * 0.20f), size.width * 0.65f,
                        size.height * (0.34f + index * 0.14f), size.width + 100f, size.height * (0.12f + index * 0.22f))
                }
                drawPath(path, BlossomPrimary.copy(alpha = 0.08f), style = Stroke(2f))
            }
            val path = Path().apply {
                val startX = size.width * 0.22f
                val startY = 40.dp.toPx() + (size.height - 80.dp.toPx()) * session.height
                moveTo(startX, startY)
                cubicTo(startX - 36.dp.toPx(), startY + 64.dp.toPx(), size.width * 0.40f, size.height * 0.85f, size.width * 0.25f, size.height)
            }
            drawPath(path, Ink.copy(alpha = 0.38f), style = Stroke(1.3.dp.toPx()))
        }
        session.visibleSeeds.forEach { seed ->
            SymbolArt(4, Modifier.offset(x = maxWidth * seed.x - 14.dp, y = 40.dp + (maxHeight - 80.dp) * seed.y - 18.dp).size(28.dp, 36.dp))
        }
        KiteArt(session.kite, Modifier.offset(kiteX, kiteY).size(64.dp, 80.dp))
    }
}
