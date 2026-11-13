package com.kitewind.weaver.presentation.kitebox

import com.kitewind.weaver.domain.model.SaveStatus

data class KiteDesign(val index: Int, val requiredHills: Int, val available: Boolean, val selected: Boolean)
data class KiteBoxUiState(val designs: List<KiteDesign> = emptyList(), val completed: Int = 0, val status: SaveStatus = SaveStatus.Loading)
