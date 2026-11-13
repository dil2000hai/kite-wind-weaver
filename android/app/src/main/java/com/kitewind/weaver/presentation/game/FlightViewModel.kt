package com.kitewind.weaver.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitewind.weaver.domain.model.*
import com.kitewind.weaver.domain.repository.MeadowRepository
import com.kitewind.weaver.domain.usecase.CreditSeedUseCase
import com.kitewind.weaver.domain.usecase.FlightEngine
import com.kitewind.weaver.domain.usecase.MeadowRules
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class FlightViewModel(
    private val repository: MeadowRepository,
    private val engine: FlightEngine,
    private val creditSeed: CreditSeedUseCase,
    private val rules: MeadowRules,
    private val calm: Boolean
) : ViewModel() {
    private val _flightState = MutableStateFlow(GameUiState())
    val flightState = _flightState.asStateFlow()
    private val _events = Channel<FlightEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var loop: Job? = null
    private var finishing: Job? = null
    private var ending = false
    private val creditMutex = Mutex()
    private val archived = mutableMapOf<Long, GameUiState>()
    init {
        viewModelScope.launch { repository.state.collect { store ->
            _flightState.update { state ->
                val hill = state.session?.destinationHill ?: store.progress.selectedHill
                state.copy(progress = store.progress, saveStatus = store.status,
                    hill = repository.hills[hill], flowers = rules.flowers(hill, store.progress.flowers[hill]))
            }
        } }
    }
    fun start(hill: Int, runId: Long) {
        loop?.cancel()
        _flightState.value.session?.let { archived[it.runId] = _flightState.value }
        ending = false
        val progress = repository.state.value.progress
        val validHill = hill.takeIf { rules.unlocked(it, progress) } ?: progress.selectedHill
        val destination = if (calm) rules.destination(validHill, progress) else validHill
        val target = repository.hills[validHill].target
        val session = FlightSession(runId, validHill, destination, progress.selectedKite, calm,
            (target - progress.flowers[validHill]).takeIf { it > 0 } ?: target,
            startingCompleted = rules.completed(progress), startingFlowers = progress.flowers)
        _flightState.update { it.copy(session = session, progress = progress, hill = repository.hills[destination],
            flowers = rules.flowers(destination, progress.flowers[destination]), pauseDialog = false, resultId = null, announcementSeeds = 0) }
        viewModelScope.launch { repository.update { it.copy(selectedHill = validHill) } }
        launchLoop()
    }
    private fun launchLoop() {
        if (loop?.isActive == true) return
        loop = viewModelScope.launch {
            var previous = System.nanoTime()
            while (isActive) {
                delay(16L)
                val now = System.nanoTime()
                val delta = ((now - previous) / 1000000f).coerceIn(0f, 32f)
                previous = now
                val current = _flightState.value.session ?: break
                if (current.phase != FlightPhase.Playing) break
                val next = engine.tick(current, delta)
                _flightState.update { it.copy(session = next, announcementSeeds = (next.collectedIds.size / 5) * 5) }
                if (next.collectedIds != next.creditedIds) creditPending()
                next.endReason?.let { finish(it) }
            }
        }
    }
    private suspend fun creditPending() = withContext(NonCancellable) {
        creditMutex.withLock {
            val before = _flightState.value.session ?: return@withLock
            val credited = creditSeed(before)
            _flightState.update { state ->
                val current = state.session
                if (current == null || current.runId != credited.runId) state else state.copy(
                    session = current.copy(creditedIds = credited.creditedIds, flowersAdded = credited.flowersAdded, destinationHill = credited.destinationHill),
                    hill = repository.hills[credited.destinationHill],
                    flowers = rules.flowers(credited.destinationHill, repository.state.value.progress.flowers[credited.destinationHill]))
            }
            if (credited.creditedIds.size > before.creditedIds.size) {
                val settings = repository.state.value.progress.settings
                _events.send(FlightEvent.Collection(settings.sound, settings.haptics))
            }
        }
    }
    fun steer(height: Float) {
        _flightState.update { state -> state.session?.let { session ->
            if (session.phase != FlightPhase.Playing) state else state.copy(session = engine.steer(session, height, true))
        } ?: state }
    }
    fun dragEnd() { _flightState.update { state -> state.copy(session = state.session?.let { engine.release(it) }) } }
    fun raise() = nudge(-0.08f)
    fun lower() = nudge(0.08f)
    private fun nudge(amount: Float) {
        _flightState.update { state -> state.session?.let { session ->
            if (session.phase != FlightPhase.Playing) state else state.copy(session = engine.steer(session, (session.targetHeight + amount).coerceIn(0.10f, 0.90f), false))
        } ?: state }
    }
    fun activate(runId: Long) {
        if (_flightState.value.session?.runId == runId) return
        val saved = archived[runId] ?: return
        loop?.cancel()
        loop = null
        _flightState.value.session?.let { archived[it.runId] = _flightState.value }
        ending = saved.session?.phase == FlightPhase.Finished
        _flightState.update { saved.copy(progress = repository.state.value.progress) }
        if (saved.session?.phase != FlightPhase.Finished) pause()
    }
    fun pauseRun(runId: Long) {
        if (_flightState.value.session?.runId == runId) pause()
    }
    fun endDragRun(runId: Long) {
        if (_flightState.value.session?.runId == runId) dragEnd()
    }
    fun pause() {
        loop?.cancel()
        loop = null
        _flightState.update { state ->
            val session = state.session
            if (session == null || session.phase == FlightPhase.Finished) state else state.copy(
                session = engine.release(session).copy(phase = FlightPhase.Paused), pauseDialog = true)
        }
    }
    fun dismissPause() { _flightState.update { it.copy(pauseDialog = false) } }
    fun resume() {
        if (_flightState.value.session?.phase != FlightPhase.Paused) return
        _flightState.update { it.copy(session = it.session?.copy(phase = FlightPhase.Playing), pauseDialog = false) }
        launchLoop()
    }
    fun land() = finish(if (calm) EndReason.Calm else EndReason.Land)
    private fun finish(reason: EndReason) {
        val session = _flightState.value.session ?: return
        if (ending || _flightState.value.resultId != null) return
        ending = true
        loop?.cancel()
        loop = null
        _flightState.update { it.copy(session = it.session?.copy(phase = FlightPhase.Finished, endReason = reason, dragging = false), pauseDialog = false) }
        finishing = viewModelScope.launch {
            creditPending()
            val frozen = _flightState.value.session?.takeIf { it.runId == session.runId } ?: return@launch
            val progress = repository.state.value.progress
            val completed = rules.completed(progress)
            val hill = frozen.hill
            val target = repository.hills[hill].target
            val newlyFull = frozen.startingFlowers[hill] < target && progress.flowers[hill] == target
            val result = FlightResult(frozen.runId, hill, frozen.kite, calm, reason, frozen.collectedIds.size,
                frozen.flowersAdded, progress.flowers[hill], target, (frozen.elapsedMs / 1000f).toInt(),
                completed, (completed - frozen.startingCompleted).coerceAtLeast(0),
                if (calm) {
                    if (completed > frozen.startingCompleted && completed < 6) rules.destination(hill, progress) else null
                } else if (newlyFull && hill < 5) hill + 1 else null)
            repository.recordResult(result)
            _flightState.update { it.copy(resultId = result.id) }
            _events.send(FlightEvent.Result(result))
        }
    }
    fun retrySave() { viewModelScope.launch { repository.retrySave() } }
    fun clear() {
        loop?.cancel(); loop = null
        finishing?.cancel(); finishing = null
        ending = false
        archived.clear()
        _flightState.update { GameUiState(progress = repository.state.value.progress, saveStatus = repository.state.value.status) }
    }
}
