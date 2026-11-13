package com.kitewind.weaver.presentation.game

import com.kitewind.weaver.domain.model.*

data class GameUiState(
    val session: FlightSession? = null,
    val hill: HillSpec? = null,
    val flowers: List<FlowerPlacement> = emptyList(),
    val progress: MeadowProgress = MeadowProgress(),
    val saveStatus: SaveStatus = SaveStatus.Loading,
    val pauseDialog: Boolean = false,
    val resultId: Long? = null,
    val announcementSeeds: Int = 0
)
sealed interface FlightEvent {
    data class Result(val result: FlightResult) : FlightEvent
    data class Collection(val sound: Boolean, val haptics: Boolean) : FlightEvent
}
