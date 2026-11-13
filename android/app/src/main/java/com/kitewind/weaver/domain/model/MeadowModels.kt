package com.kitewind.weaver.domain.model

data class HillSpec(val id: String, val index: Int, val target: Int)
data class KiteSpec(val id: String, val index: Int, val requiredHills: Int)
data class MeadowSettings(val sound: Boolean = true, val haptics: Boolean = false, val reducedMotion: Boolean = false)
data class MeadowProgress(
    val flowers: List<Int> = List(6) { 0 },
    val selectedHill: Int = 0,
    val selectedKite: Int = 0,
    val lifetimeSeeds: Int = 0,
    val bestFlight: Int = 0,
    val settings: MeadowSettings = MeadowSettings(),
    val lastResult: FlightResult? = null
) {
    val totalFlowers: Int get() = flowers.sum()
}
enum class SaveStatus { Loading, Ready, Saving, ReadError, SaveError }
data class MeadowStore(val progress: MeadowProgress = MeadowProgress(), val status: SaveStatus = SaveStatus.Loading)
data class FlowerPlacement(val x: Float, val y: Float, val symbol: Int)
enum class FlightPhase { Ready, Playing, Paused, Finished }
enum class WindKind { Breeze, Gust, Calm }
enum class EndReason { Goal, Time, Edge, Land, Calm }
data class Seed(val id: Int, val x: Float, val y: Float)
data class FlightSession(
    val runId: Long,
    val hill: Int,
    val destinationHill: Int,
    val kite: Int,
    val calm: Boolean,
    val goal: Int,
    val phase: FlightPhase = FlightPhase.Playing,
    val height: Float = 0.5f,
    val targetHeight: Float = 0.5f,
    val dragging: Boolean = false,
    val elapsedMs: Float = 0f,
    val worldOffset: Float = 0f,
    val edgeHoldMs: Float = 0f,
    val wind: WindKind = WindKind.Breeze,
    val visibleSeeds: List<Seed> = emptyList(),
    val collectedIds: Set<Int> = emptySet(),
    val creditedIds: Set<Int> = emptySet(),
    val flowersAdded: Int = 0,
    val startingCompleted: Int = 0,
    val startingFlowers: List<Int> = List(6) { 0 },
    val endReason: EndReason? = null
)
data class FlightResult(
    val id: Long,
    val hill: Int,
    val kite: Int,
    val calm: Boolean,
    val reason: EndReason,
    val seeds: Int,
    val flowersAdded: Int,
    val hillFlowers: Int,
    val target: Int,
    val elapsedSeconds: Int,
    val completedHills: Int,
    val newCompletedHills: Int,
    val nextHill: Int? = null
)
