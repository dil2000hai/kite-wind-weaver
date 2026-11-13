package com.kitewind.weaver.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitewind.weaver.R
import com.kitewind.weaver.core.theme.*
import com.kitewind.weaver.core.ui.*

@Composable
fun SplashRoute(model: SplashViewModel, onFinished: () -> Unit) {
    val state by model.uiState.collectAsStateWithLifecycle()
    val finished by model.finished.collectAsStateWithLifecycle()
    val currentFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) { model.events.collect { currentFinished() } }
    SplashScreen(state.copy(isFinished = finished))
}

@Composable
fun SplashScreen(state: SplashUiState) {
    val transition = rememberInfiniteTransition(label = "springBreeze")
    val drift by transition.animateFloat(-8f, 8f, infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "kiteDrift")
    val roll by transition.animateFloat(-3f, 3f, infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "kiteRoll")
    val pulse by transition.animateFloat(0.96f, 1f, infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "quietPulse")
    val preparing = stringResource(R.string.preparing)
    Backdrop(BackdropKind.Splash) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.spring_journal), style = MaterialTheme.typography.labelSmall, color = CloudWhite.copy(alpha = 0.88f))
            Spacer(Modifier.height(28.dp))
            Box(Modifier.fillMaxWidth().height(232.dp), contentAlignment = Alignment.TopCenter) {
                Canvas(Modifier.fillMaxSize()) {
                    val path = Path().apply {
                        moveTo(size.width * 0.50f, size.height * 0.48f)
                        cubicTo(size.width * 0.28f, size.height * 0.63f, size.width * 0.69f, size.height * 0.77f, size.width * 0.41f, size.height * 0.94f)
                    }
                    drawPath(path, CloudWhite.copy(alpha = 0.65f), style = Stroke(width = 2f))
                    drawOval(Ink.copy(alpha = 0.08f), topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height * 0.94f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.04f))
                }
                Image(painterResource(R.drawable.logo), null, Modifier.size(160.dp).graphicsLayer {
                    translationY = if (state.reducedMotion) 0f else drift.dp.toPx()
                    rotationZ = if (state.reducedMotion) 0f else roll
                    alpha = if (state.reducedMotion) pulse else 1f
                }, contentScale = ContentScale.Fit)
                SymbolArt(4, Modifier.align(Alignment.CenterStart).size(30.dp).graphicsLayer { translationY = if (state.reducedMotion) 0f else -drift.dp.toPx() })
                SymbolArt(2, Modifier.align(Alignment.CenterEnd).size(24.dp).graphicsLayer { translationY = if (state.reducedMotion) 0f else drift.dp.toPx() })
            }
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, color = CloudWhite, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.tagline), style = MaterialTheme.typography.bodyLarge, color = CloudWhite, textAlign = TextAlign.Center)
            Spacer(Modifier.height(40.dp))
            LinearProgressIndicator(modifier = Modifier.width(112.dp).height(4.dp).semantics { contentDescription = preparing },
                color = LimeAccent, trackColor = DustyPink.copy(alpha = 0.60f))
            Spacer(Modifier.height(14.dp))
            Text(preparing, style = MaterialTheme.typography.labelSmall, color = CloudWhite)
        }
    }
}
