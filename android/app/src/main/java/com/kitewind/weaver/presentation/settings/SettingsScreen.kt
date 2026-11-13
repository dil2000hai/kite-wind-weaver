package com.kitewind.weaver.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitewind.weaver.R
import com.kitewind.weaver.core.theme.*
import com.kitewind.weaver.core.ui.*
import com.kitewind.weaver.presentation.dialog.ResetMeadowDialog

@Composable
fun SettingsRoute(model: SettingsViewModel, onBack: () -> Unit, onMenu: () -> Unit) {
    val state by model.uiState.collectAsStateWithLifecycle()
    SettingsScreen(state, onBack, onMenu, model::sound, model::haptics, model::reducedMotion,
        { model.showReset(true) }, model::retrySave)
    if (state.resetDialog) ResetMeadowDialog(model::reset, { model.showReset(false) })
}

@Composable
fun SettingsScreen(state: SettingsUiState, onBack: () -> Unit, onMenu: () -> Unit, onSound: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit, onReducedMotion: (Boolean) -> Unit, onReset: () -> Unit, onRetry: () -> Unit) {
    Backdrop(BackdropKind.Gallery) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ScreenHeader(stringResource(R.string.settings), onBack)
            KiteArt(0, Modifier.size(104.dp))
            Text(stringResource(R.string.settings_eyebrow), style = MaterialTheme.typography.labelSmall, color = BlossomPrimary)
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium, color = Ink)
            MeadowCard(Modifier.fillMaxWidth()) {
                SettingToggleRow(R.string.sound, R.string.sound_description, state.settings.sound, onSound)
                HorizontalDivider(color = DustyPink.copy(alpha = 0.5f))
                SettingToggleRow(R.string.haptics, R.string.haptics_description, state.settings.haptics, onHaptics)
                HorizontalDivider(color = DustyPink.copy(alpha = 0.5f))
                SettingToggleRow(R.string.reduced_motion, R.string.motion_description, state.settings.reducedMotion, onReducedMotion)
            }
            MeadowCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.total_progress, state.grown), style = MaterialTheme.typography.titleLarge, color = BlossomPrimary)
                Text(stringResource(R.string.hills_progress, state.completed), style = MaterialTheme.typography.bodyLarge, color = Ink)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.local_only), style = MaterialTheme.typography.bodyMedium, color = Ink)
            }
            SaveStatusLine(state.status, onRetry)
            MenuButton(onMenu, Modifier.fillMaxWidth())
            SecondaryButton(R.string.reset_meadow, onReset, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }
    }
}
