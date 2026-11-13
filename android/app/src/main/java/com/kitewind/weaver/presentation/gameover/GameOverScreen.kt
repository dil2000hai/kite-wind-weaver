package com.kitewind.weaver.presentation.gameover

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitewind.weaver.R
import com.kitewind.weaver.core.theme.*
import com.kitewind.weaver.core.ui.*
import com.kitewind.weaver.domain.model.EndReason
import com.kitewind.weaver.domain.model.SaveStatus

@Composable
fun GameOverRoute(model: GameOverViewModel, onBack: () -> Unit, onAgain: () -> Unit,
    onNext: () -> Unit, onMeadow: () -> Unit, onMenu: () -> Unit) {
    val state by model.uiState.collectAsStateWithLifecycle()
    GameOverScreen(state, onBack, onAgain, onNext, onMeadow, onMenu, model::retrySave)
}

@Composable
fun GameOverScreen(state: GameOverUiState, onBack: () -> Unit, onAgain: () -> Unit,
    onNext: () -> Unit, onMeadow: () -> Unit, onMenu: () -> Unit, onRetry: () -> Unit) {
    val compact = LocalConfiguration.current.screenHeightDp < 500
    Backdrop(BackdropKind.Result) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScreenHeader(stringResource(R.string.result_label), onBack)
            val result = state.result
            if (result == null) {
                MeadowCard {
                    Text(stringResource(R.string.result_missing), style = MaterialTheme.typography.bodyLarge)
                }
                MenuButton(onMenu, Modifier.fillMaxWidth())
            } else {
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 8.dp)) {
                    item {
                        Text(stringResource(R.string.result_eyebrow), style = MaterialTheme.typography.labelSmall, color = BlossomPrimary)
                    }
                    item {
                        Text(stringResource(when { result.newCompletedHills > 0 -> R.string.hill_bloom; result.calm -> R.string.calm_result; else -> R.string.gentle_landing }),
                            style = MaterialTheme.typography.displaySmall, color = Ink, textAlign = TextAlign.Center,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    }
                    item { KiteArt(result.kite, Modifier.size(104.dp)) }
                    item {
                        MeadowCard(Modifier.fillMaxWidth()) {
                            Text(hillName(result.hill), style = MaterialTheme.typography.titleLarge, color = BlossomPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(when (result.reason) {
                                EndReason.Goal -> R.string.reason_goal; EndReason.Time -> R.string.reason_time; EndReason.Edge -> R.string.reason_edge
                                EndReason.Land -> R.string.reason_land; EndReason.Calm -> R.string.reason_calm
                            }), style = MaterialTheme.typography.bodyLarge, color = Ink)
                            Spacer(Modifier.height(8.dp))
                            if (result.seeds == 0) Text(stringResource(R.string.no_seeds), style = MaterialTheme.typography.bodyLarge, color = Ink)
                            else Text(stringResource(R.string.result_summary, result.seeds, result.flowersAdded), style = MaterialTheme.typography.bodyLarge, color = Ink)
                            Text(stringResource(R.string.active_duration, result.elapsedSeconds), style = MaterialTheme.typography.bodyMedium, color = Ink)
                            Spacer(Modifier.height(12.dp))
                            GrowthMeter(result.hillFlowers, result.target)
                        }
                    }
                    if (result.seeds > 0 && result.flowersAdded > 0) item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(R.string.seeds_gathered, result.seeds, Modifier.weight(1f))
                            StatCard(R.string.new_flowers, result.flowersAdded, Modifier.weight(1f))
                        }
                    }
                    item {
                        MeadowIllustration(state.flowers, stringResource(R.string.meadow_accessibility, hillName(result.hill), result.hillFlowers, result.target),
                            Modifier.fillMaxWidth().height(116.dp))
                    }
                    if (result.newCompletedHills > 0) item {
                        Text(stringResource(R.string.new_hills, result.newCompletedHills), style = MaterialTheme.typography.titleMedium, color = BlossomPrimary)
                    }
                    if (state.newKiteUnlocked) item {
                        Text(stringResource(R.string.new_kite), style = MaterialTheme.typography.bodyLarge, color = BlossomPrimary)
                    }
                    item { SaveStatusLine(state.status, onRetry) }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(when (state.status) {
                        SaveStatus.SaveError -> R.string.save_error; SaveStatus.Saving -> R.string.saving; else -> R.string.result_saved
                    }), style = MaterialTheme.typography.bodyMedium, color = Ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    if (compact && result.nextHill != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PrimaryButton(R.string.play_again, onAgain, Modifier.weight(1f), reducedMotion = state.reducedMotion)
                            SecondaryButton(R.string.next_hill, onNext, Modifier.weight(1f))
                        }
                    } else {
                        PrimaryButton(R.string.play_again, onAgain, Modifier.fillMaxWidth(), reducedMotion = state.reducedMotion)
                        if (result.nextHill != null) SecondaryButton(R.string.next_hill, onNext, Modifier.fillMaxWidth())
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryButton(R.string.view_meadow, onMeadow, Modifier.weight(1f))
                        MenuButton(onMenu, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
