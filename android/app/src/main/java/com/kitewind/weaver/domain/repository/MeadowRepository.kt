package com.kitewind.weaver.domain.repository

import com.kitewind.weaver.domain.model.FlightResult
import com.kitewind.weaver.domain.model.HillSpec
import com.kitewind.weaver.domain.model.KiteSpec
import com.kitewind.weaver.domain.model.MeadowProgress
import com.kitewind.weaver.domain.model.MeadowStore
import kotlinx.coroutines.flow.StateFlow

interface MeadowRepository {
    val state: StateFlow<MeadowStore>
    val hills: List<HillSpec>
    val kites: List<KiteSpec>
    suspend fun load()
    suspend fun update(transform: (MeadowProgress) -> MeadowProgress): Boolean
    suspend fun retrySave(): Boolean
    suspend fun reset(): Boolean
    suspend fun recordResult(result: FlightResult): Boolean
    fun result(id: Long): FlightResult?
}
