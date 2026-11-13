package com.kitewind.weaver.presentation.meadow

import com.kitewind.weaver.domain.model.*
import com.kitewind.weaver.presentation.menu.HillSummary

data class MeadowUiState(val hill: Int = 0, val hills: List<HillSummary> = emptyList(),
    val flowers: List<FlowerPlacement> = emptyList(), val grown: Int = 0, val target: Int = 12,
    val unlocked: Boolean = true, val status: SaveStatus = SaveStatus.Loading)
