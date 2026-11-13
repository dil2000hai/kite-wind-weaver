package com.kitewind.weaver.presentation.game

import com.kitewind.weaver.domain.repository.MeadowRepository
import com.kitewind.weaver.domain.usecase.CreditSeedUseCase
import com.kitewind.weaver.domain.usecase.FlightEngine
import com.kitewind.weaver.domain.usecase.MeadowRules

class GameViewModel(repository: MeadowRepository, engine: FlightEngine, credit: CreditSeedUseCase, rules: MeadowRules) :
    FlightViewModel(repository, engine, credit, rules, false) {
    val uiState = flightState
}
