package com.kitewind.weaver.data.repository

import com.kitewind.weaver.data.local.MeadowPreferences
import com.kitewind.weaver.data.sample.SampleData
import com.kitewind.weaver.domain.model.*
import com.kitewind.weaver.domain.repository.MeadowRepository
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class MeadowRepositoryImpl(private val local: MeadowPreferences) : MeadowRepository {
    override val hills = SampleData.hills
    override val kites = SampleData.kites
    private val mutex = Mutex()
    private val _state = MutableStateFlow(MeadowStore())
    override val state = _state.asStateFlow()
    private val results = ConcurrentHashMap<Long, FlightResult>()
    override suspend fun load() = withContext(Dispatchers.IO) {
        mutex.withLock {
            _state.update { it.copy(status = SaveStatus.Loading) }
            runCatching { local.read(hills, kites) }.fold(
                onSuccess = { progress ->
                    progress.lastResult?.let { results[it.id] = it }
                    _state.update { it.copy(progress = progress, status = SaveStatus.Ready) }
                },
                onFailure = { _state.update { it.copy(status = SaveStatus.ReadError) } }
            )
        }
    }
    override suspend fun update(transform: (MeadowProgress) -> MeadowProgress): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            _state.update { it.copy(progress = transform(it.progress), status = SaveStatus.Saving) }
            val saved = runCatching { local.write(_state.value.progress, hills) }.getOrDefault(false)
            _state.update { it.copy(status = if (saved) SaveStatus.Ready else SaveStatus.SaveError) }
            saved
        }
    }
    override suspend fun retrySave(): Boolean = update { it }
    override suspend fun reset(): Boolean {
        results.clear()
        return update { MeadowProgress(settings = it.settings) }
    }
    override suspend fun recordResult(result: FlightResult): Boolean {
        results[result.id] = result
        return update { progress -> progress.copy(lastResult = result,
            bestFlight = if (result.calm) progress.bestFlight else maxOf(progress.bestFlight, result.seeds)) }
    }
    override fun result(id: Long): FlightResult? = results[id] ?: state.value.progress.lastResult?.takeIf { it.id == id }
}
