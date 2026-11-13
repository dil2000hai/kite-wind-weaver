package com.kitewind.weaver.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kitewind.weaver.core.theme.*

@Composable
fun PrimaryButton(textRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, reducedMotion: Boolean = LocalReducedMotion.current) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && !reducedMotion) 0.98f else 1f, spring(dampingRatio = 0.78f, stiffness = 500f), label = "buttonScale")
    val shape = RoundedCornerShape(18.dp)
    Button(onClick = onClick, enabled = enabled, interactionSource = source, shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Ink,
            disabledContainerColor = DustyPink, disabledContentColor = Ink),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        modifier = modifier.heightIn(min = 60.dp).scale(scale).shadow(6.dp, shape, ambientColor = BlossomPrimary.copy(alpha = 0.16f),
            spotColor = BlossomPrimary.copy(alpha = 0.16f))
            .background(Brush.linearGradient(if (enabled) listOf(LimeAccent, DustyPink) else listOf(DustyPink, PetalBackground)), shape)) {
        Text(stringResource(textRes), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(textRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, enabled = enabled, shape = RoundedCornerShape(16.dp),
        modifier = modifier.heightIn(min = 48.dp).shadow(2.dp, RoundedCornerShape(16.dp)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = CloudWhite.copy(alpha = 0.90f), contentColor = BlossomPrimary,
            disabledContainerColor = DustyPink.copy(alpha = 0.65f), disabledContentColor = Ink),
        border = BorderStroke(1.dp, BlossomPrimary.copy(alpha = if (enabled) 0.40f else 0.12f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)) {
        Text(stringResource(textRes), style = MaterialTheme.typography.labelLarge)
    }
}
