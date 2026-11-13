package com.kitewind.weaver.presentation.gameover

import com.kitewind.weaver.domain.model.*

data class GameOverUiState(val result: FlightResult? = null, val flowers: List<FlowerPlacement> = emptyList(),
    val status: SaveStatus = SaveStatus.Loading, val reducedMotion: Boolean = false, val newKiteUnlocked: Boolean = false)
