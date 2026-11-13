package com.kitewind.weaver.presentation.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitewind.weaver.R
import com.kitewind.weaver.core.theme.*
import com.kitewind.weaver.core.ui.*
import com.kitewind.weaver.domain.model.*
import com.kitewind.weaver.presentation.dialog.PauseDialog

@Composable
fun FlightLifecycle(model: FlightViewModel, runId: Long) {
    val owner = LocalLifecycleOwner.current
    val focused = LocalWindowInfo.current.isWindowFocused
    DisposableEffect(owner, model, runId) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) model.pauseRun(runId) }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); model.pauseRun(runId) }
    }
    DisposableEffect(focused, model, runId) {
        if (!focused) model.pauseRun(runId)
        onDispose { model.endDragRun(runId) }
    }
}

@Composable
fun GameRoute(model: GameViewModel, runId: Long, onBack: () -> Unit, onMenu: () -> Unit, onViewResult: () -> Unit, onPlayAgain: () -> Unit) {
    val state by model.uiState.collectAsStateWithLifecycle()
    FlightLifecycle(model, runId)
    GameScreen(state, model::steer, model::dragEnd, model::raise, model::lower, model::pause,
        model::land, model::resume, onBack, onViewResult, onPlayAgain, model::retrySave)
    if (state.pauseDialog) PauseDialog(model::resume, model::land, onMenu, model::dismissPause)
}

@Composable
fun GameScreen(state: GameUiState, onDrag: (Float) -> Unit, onDragEnd: () -> Unit, onRaise: () -> Unit,
    onLower: () -> Unit, onPause: () -> Unit, onLand: () -> Unit, onResume: () -> Unit,
    onBack: () -> Unit, onViewResult: () -> Unit, onPlayAgain: () -> Unit, onRetrySave: () -> Unit) {
    FlightContent(state, onDrag, onDragEnd, onRaise, onLower, onPause, onLand, onResume, onBack, onViewResult, onPlayAgain, onRetrySave)
}

@Composable
fun FlightContent(state: GameUiState, onDrag: (Float) -> Unit, onDragEnd: () -> Unit, onRaise: () -> Unit,
    onLower: () -> Unit, onPause: () -> Unit, onLand: () -> Unit, onResume: () -> Unit,
    onBack: () -> Unit, onViewResult: () -> Unit, onPlayAgain: () -> Unit, onRetrySave: () -> Unit) {
    Backdrop(BackdropKind.Flight) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp, vertical = 8.dp)) {
            val compact = maxHeight < 580.dp
            val session = state.session
            if (session == null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = BlossomPrimary)
            } else {
                val wind = stringResource(when (session.wind) { WindKind.Breeze -> R.string.breeze; WindKind.Gust -> R.string.gust; WindKind.Calm -> R.string.calm_patch })
                val phase = stringResource(when (session.phase) { FlightPhase.Playing -> R.string.phase_playing; FlightPhase.Paused -> R.string.phase_paused
                    FlightPhase.Finished -> R.string.phase_finished; FlightPhase.Ready -> R.string.phase_ready })
                val announcement = stringResource(R.string.flight_announcement, state.announcementSeeds, wind, phase)
                val columnModifier = if (compact) Modifier.fillMaxWidth().verticalScroll(rememberScrollState()) else Modifier.fillMaxSize()
                Column(columnModifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(4.dp)) { Text(stringResource(R.string.back)) }
                        Column(Modifier.weight(1f)) {
                            Text(if (session.calm) stringResource(R.string.calm_mode) else hillName(session.hill), style = MaterialTheme.typography.titleLarge, color = Ink)
                            Text(wind, style = MaterialTheme.typography.bodyMedium, color = BlossomPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.clearAndSetSemantics {
                            liveRegion = LiveRegionMode.Polite; contentDescription = announcement; stateDescription = phase
                        }) {
                            Text(stringResource(R.string.seed_count, session.collectedIds.size), style = MaterialTheme.typography.titleLarge, color = BlossomPrimary)
                            if (!session.calm) Text(stringResource(R.string.goal_count, session.goal), style = MaterialTheme.typography.labelSmall, color = Ink)
                        }
                    }
                    if (session.calm) Text(stringResource(R.string.no_timer), style = MaterialTheme.typography.labelSmall, color = Ink)
                    else Text(stringResource(R.string.seconds_remaining, ((60000f - session.elapsedMs).coerceAtLeast(0f) / 1000f).toInt()),
                        style = MaterialTheme.typography.labelSmall, color = Ink)
                    FlightViewport(session, onDrag, onDragEnd,
                        Modifier.fillMaxWidth().then(if (compact) Modifier.height(240.dp) else Modifier.weight(1f)))
                    val hill = state.hill
                    if (hill != null) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(hillName(hill.index), style = MaterialTheme.typography.labelSmall, color = Ink)
                            Text(stringResource(R.string.flowers_progress, state.progress.flowers[hill.index], hill.target), style = MaterialTheme.typography.labelSmall, color = Ink)
                        }
                        MeadowIllustration(state.flowers, stringResource(R.string.meadow_accessibility, hillName(hill.index), state.progress.flowers[hill.index], hill.target),
                            Modifier.fillMaxWidth().height(68.dp), small = true)
                    }
                    if (state.saveStatus == SaveStatus.SaveError) SaveStatusLine(state.saveStatus, onRetrySave)
                    MeadowCard(Modifier.fillMaxWidth()) {
                        Text(stringResource(when {
                            session.phase == FlightPhase.Finished -> R.string.finished_hint
                            session.phase == FlightPhase.Paused -> R.string.paused_hint
                            session.edgeHoldMs >= 400f -> R.string.edge_hint
                            session.calm -> R.string.calm_hint
                            else -> R.string.steering_hint
                        }), style = MaterialTheme.typography.bodyMedium, color = Ink)
                        Spacer(Modifier.height(8.dp))
                        when (session.phase) {
                            FlightPhase.Finished -> {
                                PrimaryButton(R.string.play_again, onPlayAgain, Modifier.fillMaxWidth(), enabled = state.resultId != null, reducedMotion = state.progress.settings.reducedMotion)
                                Spacer(Modifier.height(8.dp))
                                SecondaryButton(R.string.view_result, onViewResult, Modifier.fillMaxWidth(), state.resultId != null)
                            }
                            FlightPhase.Paused -> {
                                PrimaryButton(R.string.resume, onResume, Modifier.fillMaxWidth(), reducedMotion = state.progress.settings.reducedMotion)
                                Spacer(Modifier.height(8.dp))
                                SecondaryButton(if (session.calm) R.string.finish_flight else R.string.land, onLand, Modifier.fillMaxWidth())
                            }
                            else -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SecondaryButton(R.string.raise, onRaise, Modifier.weight(1f))
                                    SecondaryButton(R.string.lower, onLower, Modifier.weight(1f))
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = onPause, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                                        Image(painterResource(R.drawable.icon_pause), null, Modifier.size(20.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(stringResource(R.string.pause))
                                    }
                                    SecondaryButton(if (session.calm) R.string.finish_flight else R.string.land, onLand, Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
