package com.kitewind.weaver.presentation.settings

import com.kitewind.weaver.domain.model.MeadowSettings
import com.kitewind.weaver.domain.model.SaveStatus

data class SettingsUiState(val settings: MeadowSettings = MeadowSettings(), val grown: Int = 0,
    val completed: Int = 0, val resetDialog: Boolean = false, val status: SaveStatus = SaveStatus.Loading)
