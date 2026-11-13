package com.kitewind.weaver.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitewind.weaver.domain.model.MeadowSettings
import com.kitewind.weaver.domain.repository.MeadowRepository
import com.kitewind.weaver.domain.usecase.MeadowRules
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: MeadowRepository, private val rules: MeadowRules) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = Channel<Unit>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    init { viewModelScope.launch { repository.state.collect { store ->
        _uiState.update { it.copy(settings = store.progress.settings, grown = store.progress.totalFlowers,
            completed = rules.completed(store.progress), status = store.status) }
    } } }
    fun sound(value: Boolean) = change { it.copy(sound = value) }
    fun haptics(value: Boolean) = change { it.copy(haptics = value) }
    fun reducedMotion(value: Boolean) = change { it.copy(reducedMotion = value) }
    private fun change(transform: (MeadowSettings) -> MeadowSettings) {
        viewModelScope.launch { repository.update { it.copy(settings = transform(it.settings)) } }
    }
    fun showReset(show: Boolean) { _uiState.update { it.copy(resetDialog = show) } }
    fun reset() { viewModelScope.launch {
        repository.reset()
        _uiState.update { it.copy(resetDialog = false) }
        _events.send(Unit)
    } }
    fun retrySave() { viewModelScope.launch { repository.retrySave() } }
}
