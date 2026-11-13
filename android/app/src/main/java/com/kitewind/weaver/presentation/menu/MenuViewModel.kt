package com.kitewind.weaver.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitewind.weaver.domain.model.*
import com.kitewind.weaver.domain.repository.MeadowRepository
import com.kitewind.weaver.domain.usecase.MeadowRules
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MenuViewModel(private val repository: MeadowRepository, private val rules: MeadowRules) : ViewModel() {
    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = Channel<Unit>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    init {
        viewModelScope.launch { repository.state.collect { store ->
            _uiState.update { it.copy(progress = store.progress, completed = rules.completed(store.progress), status = store.status,
                hills = repository.hills.map { hill -> HillSummary(hill.index, store.progress.flowers[hill.index], hill.target,
                    rules.unlocked(hill.index, store.progress), store.progress.selectedHill == hill.index) }) }
        } }
        retry()
    }
    fun selectHill(hill: Int) {
        if (!rules.unlocked(hill, repository.state.value.progress)) return
        viewModelScope.launch { repository.update { it.copy(selectedHill = hill) } }
    }
    fun retry() { viewModelScope.launch { repository.load() } }
    fun retrySave() { viewModelScope.launch { repository.retrySave() } }
    fun showReset(show: Boolean) { _uiState.update { it.copy(resetDialog = show) } }
    fun startFresh() { viewModelScope.launch {
        repository.reset()
        _uiState.update { it.copy(resetDialog = false) }
        _events.send(Unit)
    } }
}
