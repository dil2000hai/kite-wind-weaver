package com.kitewind.weaver.presentation.kitebox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitewind.weaver.domain.repository.MeadowRepository
import com.kitewind.weaver.domain.usecase.MeadowRules
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class KiteBoxViewModel(private val repository: MeadowRepository, private val rules: MeadowRules) : ViewModel() {
    private val _uiState = MutableStateFlow(KiteBoxUiState())
    val uiState = _uiState.asStateFlow()
    init { viewModelScope.launch { repository.state.collect { store ->
        val complete = rules.completed(store.progress)
        _uiState.update { it.copy(completed = complete, status = store.status, designs = repository.kites.map { kite ->
            KiteDesign(kite.index, kite.requiredHills, complete >= kite.requiredHills, store.progress.selectedKite == kite.index) }) }
    } } }
    fun select(kite: Int) {
        val design = repository.kites.getOrNull(kite) ?: return
        if (rules.completed(repository.state.value.progress) < design.requiredHills) return
        viewModelScope.launch { repository.update { it.copy(selectedKite = kite) } }
    }
    fun retrySave() { viewModelScope.launch { repository.retrySave() } }
}
