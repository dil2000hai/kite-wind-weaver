package com.kitewind.weaver.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitewind.weaver.R
import com.kitewind.weaver.core.theme.*
import com.kitewind.weaver.core.ui.*
import com.kitewind.weaver.domain.model.SaveStatus
import com.kitewind.weaver.presentation.dialog.ResetMeadowDialog

@Composable
fun MenuRoute(model: MenuViewModel, canResume: Boolean, onResume: () -> Unit, onPlay: () -> Unit,
    onSettings: () -> Unit, onMeadow: () -> Unit, onKites: () -> Unit, onCalm: () -> Unit) {
    val state by model.uiState.collectAsStateWithLifecycle()
    MenuScreen(state, canResume, onResume, onPlay, onSettings, onMeadow, onKites, onCalm,
        model::selectHill, model::retry, model::retrySave, { model.showReset(true) })
    if (state.resetDialog) ResetMeadowDialog(model::startFresh, { model.showReset(false) })
}

@Composable
fun MenuScreen(state: MenuUiState, canResume: Boolean, onResume: () -> Unit, onPlay: () -> Unit,
    onSettings: () -> Unit, onMeadow: () -> Unit, onKites: () -> Unit, onCalm: () -> Unit,
    onSelect: (Int) -> Unit, onRetry: () -> Unit, onRetrySave: () -> Unit, onFresh: () -> Unit) {
    val ready = state.status != SaveStatus.Loading && state.status != SaveStatus.ReadError
    Backdrop(BackdropKind.Menu) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.spring_journal), style = MaterialTheme.typography.labelSmall, color = BlossomPrimary)
                    Text(stringResource(R.string.your_six_hills), style = MaterialTheme.typography.headlineMedium, color = Ink)
                }
                TextButton(onClick = onSettings, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.settings)) }
            }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                item {
                    MeadowCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KiteArt(state.progress.selectedKite, Modifier.size(76.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(if (state.progress.totalFlowers == 0) R.string.waiting else R.string.growing),
                                    style = MaterialTheme.typography.titleLarge, color = BlossomPrimary)
                                Text(hillName(state.progress.selectedHill), style = MaterialTheme.typography.bodyLarge, color = Ink)
                                state.hills.getOrNull(state.progress.selectedHill)?.let { GrowthMeter(it.grown, it.target) }
                            }
                        }
                        if (state.status == SaveStatus.Loading) {
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                            Text(stringResource(R.string.loading_meadow), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (state.status == SaveStatus.ReadError) item {
                    MeadowCard {
                        Text(stringResource(R.string.load_error), style = MaterialTheme.typography.bodyLarge)
                        SecondaryButton(R.string.retry, onRetry)
                        SecondaryButton(R.string.start_fresh, onFresh)
                    }
                }
                if (ready) items(state.hills, key = { it.index }) { hill -> HillCard(hill) { onSelect(hill.index) } }
                if (state.progress.totalFlowers > 0 && state.completed > 0) item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(R.string.flowers_grown, state.progress.totalFlowers, Modifier.weight(1f))
                        StatCard(R.string.hills_complete, state.completed, Modifier.weight(1f))
                    }
                }
                if (state.progress.bestFlight > 0 && state.progress.lifetimeSeeds > 0) item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(R.string.best_flight, state.progress.bestFlight, Modifier.weight(1f))
                        StatCard(R.string.seeds_gathered, state.progress.lifetimeSeeds, Modifier.weight(1f))
                    }
                }
                item { SaveStatusLine(state.status, onRetrySave) }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                if (canResume) SecondaryButton(R.string.resume, onResume, Modifier.fillMaxWidth())
                Text(stringResource(R.string.steering_hint), style = MaterialTheme.typography.bodyMedium, color = Ink)
                PrimaryButton(R.string.play, onPlay, Modifier.fillMaxWidth(), ready, state.progress.settings.reducedMotion)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(R.string.meadow, onMeadow, Modifier.weight(1f), ready)
                    SecondaryButton(R.string.kite_box, onKites, Modifier.weight(1f), ready)
                    SecondaryButton(R.string.calm_mode, onCalm, Modifier.weight(1f), ready)
                }
            }
        }
    }
}

@Composable
private fun HillCard(hill: HillSummary, onSelect: () -> Unit) {
    val stateText = stringResource(when { hill.selected -> R.string.selected_state; !hill.unlocked -> R.string.locked_state
        hill.grown == hill.target -> R.string.complete_state; else -> R.string.available_state })
    MeadowCard(Modifier.fillMaxWidth().semantics { stateDescription = stateText }, selected = hill.selected) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(48.dp).background(if (hill.selected) LimeAccent.copy(alpha = 0.32f) else PetalBackground, CircleShape), contentAlignment = Alignment.Center) {
                SymbolArt(hill.index % 5, Modifier.size(38.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(hillName(hill.index), style = MaterialTheme.typography.titleMedium, color = Ink)
                GrowthMeter(hill.grown, hill.target)
            }
            SecondaryButton(when { !hill.unlocked -> R.string.locked; hill.selected -> R.string.selected; else -> R.string.select },
                onSelect, enabled = hill.unlocked && !hill.selected)
        }
        if (!hill.unlocked) {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.unlock_previous, hillName(hill.index - 1)), style = MaterialTheme.typography.bodyMedium, color = Ink)
        } else if (hill.grown == hill.target) {
            Text(stringResource(R.string.full_meadow), style = MaterialTheme.typography.labelSmall, color = BlossomPrimary)
        }
    }
}
