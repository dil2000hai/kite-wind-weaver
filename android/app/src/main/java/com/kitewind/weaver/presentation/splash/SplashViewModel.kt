package com.kitewind.weaver.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitewind.weaver.core.config.GameConfig
import com.kitewind.weaver.domain.repository.MeadowRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SplashViewModel(repository: MeadowRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState = _uiState.asStateFlow()
    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()
    private val _events = Channel<Unit>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    init {
        viewModelScope.launch { repository.state.collect { store ->
            _uiState.update { it.copy(reducedMotion = store.progress.settings.reducedMotion) }
        } }
        viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            _finished.value = true
            _uiState.update { it.copy(isFinished = true) }
            _events.send(Unit)
        }
    }
}
