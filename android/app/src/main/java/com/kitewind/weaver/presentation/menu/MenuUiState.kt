package com.kitewind.weaver.presentation.menu

import com.kitewind.weaver.domain.model.*

data class HillSummary(val index: Int, val grown: Int, val target: Int, val unlocked: Boolean, val selected: Boolean)
data class MenuUiState(
    val progress: MeadowProgress = MeadowProgress(),
    val hills: List<HillSummary> = emptyList(),
    val completed: Int = 0,
    val status: SaveStatus = SaveStatus.Loading,
    val resetDialog: Boolean = false
)
