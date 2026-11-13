package com.kitewind.weaver.domain.usecase

import com.kitewind.weaver.domain.model.FlightSession
import com.kitewind.weaver.domain.repository.MeadowRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CreditSeedUseCase(private val repository: MeadowRepository, private val rules: MeadowRules) {
    private data class Receipt(val added: Boolean, val destination: Int)
    private val receipts = mutableMapOf<Pair<Long, Int>, Receipt>()
    private val mutex = Mutex()
    suspend operator fun invoke(session: FlightSession): FlightSession = mutex.withLock {
        var current = session
        for (seedId in session.collectedIds - session.creditedIds) {
            val key = session.runId to seedId
            val receipt = receipts[key] ?: run {
                var added = false
                var destination = current.destinationHill
                repository.update { progress ->
                    destination = if (current.calm) rules.destination(destination, progress) else current.hill
                    val count = progress.flowers[destination]
                    added = count < repository.hills[destination].target
                    progress.copy(
                        flowers = progress.flowers.mapIndexed { index, value -> if (index == destination && added) value + 1 else value },
                        lifetimeSeeds = (progress.lifetimeSeeds.toLong() + 1L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                    )
                }
                Receipt(added, destination).also { receipts[key] = it }
            }
            current = current.copy(creditedIds = current.creditedIds + seedId,
                flowersAdded = current.flowersAdded + if (receipt.added) 1 else 0,
                destinationHill = if (current.calm) rules.destination(receipt.destination, repository.state.value.progress) else receipt.destination)
        }
        current
    }
}
