package com.kitewind.weaver.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.kitewind.weaver.R
import com.kitewind.weaver.core.theme.*
import com.kitewind.weaver.domain.model.FlowerPlacement

@Composable
fun SymbolArt(symbol: Int, modifier: Modifier = Modifier, description: String? = null) {
    val painter = when (symbol % 5) {
        0 -> painterResource(R.drawable.sprite_1)
        1 -> painterResource(R.drawable.sprite_2)
        2 -> painterResource(R.drawable.sprite_3)
        3 -> painterResource(R.drawable.sprite_4)
        else -> painterResource(R.drawable.sprite_5)
    }
    Image(painter, description, modifier.size(48.dp), contentScale = ContentScale.Fit)
}

@Composable
fun KiteArt(kite: Int, modifier: Modifier = Modifier, meaningful: Boolean = false) {
    SymbolArt(kite, modifier, if (meaningful) stringResource(R.string.kite_description, kiteName(kite)) else null)
}

@Composable
fun hillName(index: Int): String = stringResource(when (index) {
    0 -> R.string.hill_clover; 1 -> R.string.hill_daisy; 2 -> R.string.hill_poppy
    3 -> R.string.hill_iris; 4 -> R.string.hill_buttercup; else -> R.string.hill_wildflower
})

@Composable
fun kiteName(index: Int): String = stringResource(when (index) {
    0 -> R.string.kite_blossom; 1 -> R.string.kite_leaf; 2 -> R.string.kite_ribbon; else -> R.string.kite_star
})

@Composable
fun MeadowIllustration(flowers: List<FlowerPlacement>, summary: String, modifier: Modifier = Modifier, small: Boolean = false) {
    val reduced = LocalReducedMotion.current
    BoxWithConstraints(modifier.clip(RoundedCornerShape(18.dp)).background(
        Brush.verticalGradient(listOf(PetalBackground.copy(alpha = 0.70f), DustyPink.copy(alpha = 0.75f), LimeAccent.copy(alpha = 0.35f))))
        .semantics(mergeDescendants = true) { contentDescription = summary }) {
        Canvas(Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(0f, size.height * 0.65f)
                cubicTo(size.width * 0.35f, size.height * 0.30f, size.width * 0.65f, size.height * 0.90f, size.width, size.height * 0.55f)
                lineTo(size.width, size.height); lineTo(0f, size.height); close()
            }
            drawPath(path, LimeAccent.copy(alpha = 0.25f))
        }
        val sprite = if (small) 18.dp else 32.dp
        val fieldWidth = maxWidth
        val fieldHeight = maxHeight
        AnimatedContent(targetState = flowers, modifier = Modifier.fillMaxSize(), transitionSpec = {
            val enter = if (reduced) fadeIn(tween(120)) else fadeIn(tween(180)) + scaleIn(spring(dampingRatio = 0.72f, stiffness = 420f), initialScale = 0.95f)
            enter togetherWith fadeOut(tween(if (reduced) 120 else 180))
        }, label = "meadowGrowth") { visibleFlowers ->
            Box(Modifier.fillMaxSize()) {
                visibleFlowers.forEach { flower ->
                    SymbolArt(flower.symbol, Modifier.offset(x = (fieldWidth - sprite) * flower.x,
                        y = (fieldHeight - sprite) * (0.32f + flower.y * 0.60f)).size(sprite))
                }
                if (visibleFlowers.isEmpty()) SymbolArt(4, Modifier.align(Alignment.Center).size(if (small) 24.dp else 40.dp))
            }
        }
    }
}
