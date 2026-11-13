package com.kitewind.weaver.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitewind.weaver.core.navigation.Screen
import com.kitewind.weaver.domain.repository.MeadowRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AppUiState(val navigationReady: Boolean = false, val reducedMotion: Boolean = false)
sealed interface AppEvent { data object Exit : AppEvent }
class AppViewModel(repository: MeadowRepository) : ViewModel() {
    private val _screen = MutableStateFlow<Screen>(Screen.Splash)
    val screen = _screen.asStateFlow()
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()
    private val backStack = mutableListOf<Screen>()
    private val _events = Channel<AppEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var nextId = System.currentTimeMillis()
    init {
        viewModelScope.launch { repository.state.collect { store ->
            _uiState.update { it.copy(reducedMotion = store.progress.settings.reducedMotion) }
        } }
    }
    fun newRunId(): Long = ++nextId
    fun splashFinished() {
        if (_screen.value != Screen.Splash) return
        _screen.value = Screen.Menu
        _uiState.update { it.copy(navigationReady = true) }
    }
    fun navigate(destination: Screen) {
        if (destination == _screen.value) return
        if (_screen.value != Screen.Splash) backStack.add(_screen.value)
        _screen.value = destination
    }
    fun back() {
        if (_screen.value == Screen.Splash) return
        if (_screen.value == Screen.Menu) {
            viewModelScope.launch { _events.send(AppEvent.Exit) }
        } else _screen.value = if (backStack.isEmpty()) Screen.Menu else backStack.removeAt(backStack.lastIndex)
    }
    fun menu() { backStack.clear(); _screen.value = Screen.Menu }
    fun replay(destination: Screen) {
        val history = backStack + _screen.value
        val flightIndex = history.indexOfLast { it is Screen.Game || it is Screen.CalmMode }
        val prefix = if (flightIndex >= 0) history.take(flightIndex) else listOf(Screen.Menu)
        backStack.clear()
        backStack.addAll(prefix.ifEmpty { listOf(Screen.Menu) })
        _screen.value = destination
    }
}
