package com.kitewind.weaver.presentation.meadow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitewind.weaver.domain.repository.MeadowRepository
import com.kitewind.weaver.domain.usecase.MeadowRules
import com.kitewind.weaver.presentation.menu.HillSummary
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MeadowViewModel(private val repository: MeadowRepository, private val rules: MeadowRules) : ViewModel() {
    private val _uiState = MutableStateFlow(MeadowUiState())
    val uiState = _uiState.asStateFlow()
    init { viewModelScope.launch { repository.state.collect { refresh() } } }
    fun show(hill: Int) { _uiState.update { it.copy(hill = hill.coerceIn(0, 5)) }; refresh() }
    private fun refresh() {
        val store = repository.state.value
        val progress = store.progress
        _uiState.update { state -> state.copy(
            hills = repository.hills.map { HillSummary(it.index, progress.flowers[it.index], it.target,
                rules.unlocked(it.index, progress), state.hill == it.index) },
            flowers = rules.flowers(state.hill, progress.flowers[state.hill]), grown = progress.flowers[state.hill],
            target = repository.hills[state.hill].target, unlocked = rules.unlocked(state.hill, progress), status = store.status) }
    }
    fun retrySave() { viewModelScope.launch { repository.retrySave() } }
}
