package com.kitewind.weaver.presentation.meadow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitewind.weaver.R
import com.kitewind.weaver.core.theme.*
import com.kitewind.weaver.core.ui.*

@Composable
fun MeadowRoute(model: MeadowViewModel, onBack: () -> Unit, onPlay: (Int) -> Unit) {
    val state by model.uiState.collectAsStateWithLifecycle()
    MeadowViewScreen(state, onBack, model::show, { onPlay(state.hill) }, model::retrySave)
}

@Composable
fun MeadowViewScreen(state: MeadowUiState, onBack: () -> Unit, onSelect: (Int) -> Unit, onPlay: () -> Unit, onRetry: () -> Unit) {
    Backdrop(BackdropKind.Meadow) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            ScreenHeader(stringResource(R.string.meadow_title), onBack)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.hills.forEach { hill ->
                    val status = stringResource(if (hill.selected) R.string.selected_state else if (!hill.unlocked) R.string.locked_state else R.string.available_state)
                    OutlinedButton(onClick = { onSelect(hill.index) }, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .semantics { stateDescription = status }, shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(if (hill.selected) 2.dp else 1.dp, if (hill.selected) BlossomPrimary else DustyPink),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (hill.selected) LimeAccent.copy(alpha = 0.4f) else CloudWhite.copy(alpha = 0.8f)),
                        contentPadding = PaddingValues(10.dp)) {
                        Text(stringResource(R.string.hill_number, hill.index + 1), color = Ink)
                    }
                }
            }
            MeadowIllustration(state.flowers, stringResource(R.string.meadow_accessibility, hillName(state.hill), state.grown, state.target),
                Modifier.fillMaxWidth().height(280.dp))
            Text(hillName(state.hill), style = MaterialTheme.typography.headlineMedium, color = BlossomPrimary)
            MeadowCard(Modifier.fillMaxWidth()) {
                GrowthMeter(state.grown, state.target)
                if (!state.unlocked) {
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.unlock_previous, hillName((state.hill - 1).coerceAtLeast(0))), style = MaterialTheme.typography.bodyLarge, color = Ink)
                }
            }
            if (state.grown == 0 && state.unlocked) Text(stringResource(R.string.empty_meadow),
                style = MaterialTheme.typography.titleLarge, color = Ink, textAlign = TextAlign.Center)
            SaveStatusLine(state.status, onRetry)
            Text(stringResource(R.string.meadow_hint), style = MaterialTheme.typography.bodyLarge, color = Ink, textAlign = TextAlign.Center)
            PrimaryButton(R.string.play, onPlay, Modifier.fillMaxWidth(), state.unlocked)
            Spacer(Modifier.height(8.dp))
        }
    }
}
