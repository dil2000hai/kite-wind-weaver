package com.kitewind.weaver.presentation.kitebox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitewind.weaver.R
import com.kitewind.weaver.core.theme.*
import com.kitewind.weaver.core.ui.*

@Composable
fun KiteBoxRoute(model: KiteBoxViewModel, onBack: () -> Unit, onMenu: () -> Unit) {
    val state by model.uiState.collectAsStateWithLifecycle()
    KiteBoxScreen(state, onBack, onMenu, model::select, model::retrySave)
}

@Composable
fun KiteBoxScreen(state: KiteBoxUiState, onBack: () -> Unit, onMenu: () -> Unit, onSelect: (Int) -> Unit, onRetry: () -> Unit) {
    Backdrop(BackdropKind.Gallery) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScreenHeader(stringResource(R.string.kite_title), onBack)
            Text(stringResource(R.string.kite_intro), style = MaterialTheme.typography.bodyLarge, color = Ink)
            LazyVerticalGrid(columns = GridCells.Adaptive(144.dp), modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                items(state.designs, key = { it.index }) { design ->
                    val status = stringResource(when { design.selected -> R.string.selected_state; design.available -> R.string.available_state; else -> R.string.locked_state })
                    MeadowCard(Modifier.fillMaxWidth().heightIn(min = 256.dp).semantics { stateDescription = status }, design.selected) {
                        KiteArt(design.index, Modifier.size(76.dp).align(Alignment.CenterHorizontally).alpha(if (design.available) 1f else 0.60f), meaningful = true)
                        Spacer(Modifier.height(12.dp))
                        Text(kiteName(design.index), style = MaterialTheme.typography.titleMedium, color = Ink)
                        Spacer(Modifier.height(6.dp))
                        Text(if (!design.available) stringResource(R.string.kite_requirement, design.requiredHills)
                            else stringResource(if (design.selected) R.string.kite_selected else R.string.kite_available),
                            style = MaterialTheme.typography.bodyMedium, color = Ink)
                        Spacer(Modifier.height(12.dp))
                        SecondaryButton(when { design.selected -> R.string.selected; design.available -> R.string.select; else -> R.string.locked },
                            { onSelect(design.index) }, Modifier.fillMaxWidth(), design.available && !design.selected)
                    }
                }
            }
            SaveStatusLine(state.status, onRetry)
            MenuButton(onMenu, Modifier.fillMaxWidth())
        }
    }
}
