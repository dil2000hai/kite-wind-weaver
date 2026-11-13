package com.kitewind.weaver.presentation.calm

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitewind.weaver.presentation.game.FlightContent
import com.kitewind.weaver.presentation.game.FlightLifecycle
import com.kitewind.weaver.presentation.dialog.PauseDialog

@Composable
fun CalmModeRoute(model: CalmModeViewModel, runId: Long, onBack: () -> Unit, onMenu: () -> Unit, onViewResult: () -> Unit, onPlayAgain: () -> Unit) {
    val state by model.uiState.collectAsStateWithLifecycle()
    FlightLifecycle(model, runId)
    CalmModeScreen(state, model::steer, model::dragEnd, model::raise, model::lower, model::pause,
        model::land, model::resume, onBack, onViewResult, onPlayAgain, model::retrySave)
    if (state.flight.pauseDialog) PauseDialog(model::resume, model::land, onMenu, model::dismissPause)
}

@Composable
fun CalmModeScreen(state: CalmModeUiState, onDrag: (Float) -> Unit, onDragEnd: () -> Unit, onRaise: () -> Unit,
    onLower: () -> Unit, onPause: () -> Unit, onLand: () -> Unit, onResume: () -> Unit,
    onBack: () -> Unit, onViewResult: () -> Unit, onPlayAgain: () -> Unit, onRetrySave: () -> Unit) {
    FlightContent(state.flight, onDrag, onDragEnd, onRaise, onLower, onPause, onLand, onResume,
        onBack, onViewResult, onPlayAgain, onRetrySave)
}
