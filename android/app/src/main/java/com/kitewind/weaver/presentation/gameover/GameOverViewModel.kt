package com.kitewind.weaver.presentation.gameover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitewind.weaver.domain.repository.MeadowRepository
import com.kitewind.weaver.domain.usecase.MeadowRules
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameOverViewModel(private val repository: MeadowRepository, private val rules: MeadowRules) : ViewModel() {
    private val _uiState = MutableStateFlow(GameOverUiState())
    val uiState = _uiState.asStateFlow()
    init {
        viewModelScope.launch { repository.state.collect { store ->
            _uiState.update { state -> state.copy(status = store.status, reducedMotion = store.progress.settings.reducedMotion,
                flowers = state.result?.let { rules.flowers(it.hill, store.progress.flowers[it.hill]) } ?: emptyList()) }
        } }
    }
    fun show(id: Long) {
        val result = repository.result(id)
        _uiState.update { it.copy(result = result, status = repository.state.value.status,
            newKiteUnlocked = result?.let { value -> repository.kites.any { kite ->
                kite.requiredHills > value.completedHills - value.newCompletedHills && kite.requiredHills <= value.completedHills } } ?: false,
            flowers = result?.let { value -> rules.flowers(value.hill, repository.state.value.progress.flowers[value.hill]) } ?: emptyList()) }
    }
    fun retrySave() { viewModelScope.launch { repository.retrySave() } }
}
