package com.kitewind.weaver.presentation.calm

import androidx.lifecycle.viewModelScope
import com.kitewind.weaver.domain.repository.MeadowRepository
import com.kitewind.weaver.domain.usecase.CreditSeedUseCase
import com.kitewind.weaver.domain.usecase.FlightEngine
import com.kitewind.weaver.domain.usecase.MeadowRules
import com.kitewind.weaver.presentation.game.FlightViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CalmModeViewModel(repository: MeadowRepository, engine: FlightEngine, credit: CreditSeedUseCase, rules: MeadowRules) :
    FlightViewModel(repository, engine, credit, rules, true) {
    val uiState = flightState.map { CalmModeUiState(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, CalmModeUiState())
}
