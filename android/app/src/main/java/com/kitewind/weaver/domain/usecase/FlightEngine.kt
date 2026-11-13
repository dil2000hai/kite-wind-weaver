package com.kitewind.weaver.domain.usecase

import com.kitewind.weaver.domain.model.EndReason
import com.kitewind.weaver.domain.model.FlightPhase
import com.kitewind.weaver.domain.model.FlightSession
import com.kitewind.weaver.domain.model.Seed
import com.kitewind.weaver.domain.model.WindKind
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sin

class FlightEngine {
    fun steer(session: FlightSession, height: Float, dragging: Boolean): FlightSession =
        if (session.phase == FlightPhase.Finished) session else session.copy(
            targetHeight = height.coerceIn(if (session.calm) 0.10f else 0.04f, if (session.calm) 0.90f else 0.96f),
            dragging = dragging,
            edgeHoldMs = if (dragging && (height <= 0.08f || height >= 0.92f)) session.edgeHoldMs else 0f
        )
    fun release(session: FlightSession): FlightSession = session.copy(dragging = false, edgeHoldMs = 0f)
    fun tick(session: FlightSession, deltaMs: Float): FlightSession {
        if (session.phase != FlightPhase.Playing) return session
        val delta = deltaMs.coerceIn(0f, 32f)
        val cycle = session.elapsedMs % 12000f
        val wind = when {
            cycle < 5000f -> WindKind.Breeze
            cycle < 8000f -> WindKind.Gust
            else -> WindKind.Calm
        }
        val speed = when (wind) { WindKind.Breeze -> 0.28f; WindKind.Gust -> 0.40f; WindKind.Calm -> 0.18f }
        val travel = session.worldOffset + speed * delta / 1000f
        val target = if (!session.dragging) {
            val safe = session.targetHeight.coerceIn(0.10f, 0.90f)
            session.targetHeight + (safe - session.targetHeight) * (delta / 200f).coerceAtMost(1f)
        } else session.targetHeight
        val movement = (target - session.height).coerceIn(-0.90f * delta / 1000f, 0.90f * delta / 1000f)
        val height = session.height + movement
        val first = floor((travel - 1.18f) / 0.32f).toInt().coerceAtLeast(0)
        val last = ceil((travel - 0.02f) / 0.32f).toInt().coerceAtLeast(0)
        val collected = session.collectedIds.toMutableSet()
        val visible = mutableListOf<Seed>()
        for (index in first..last) {
            if (!session.calm && index >= 48) break
            val worldX = 1.10f + 0.32f * index
            val x = worldX - travel
            val oldX = worldX - session.worldOffset
            val y = (0.50 + 0.26 * sin(index * PI / 6 + session.hill * PI / 9)).toFloat()
            if (index !in collected && x <= 0.28f && oldX >= 0.16f && abs(y - height) <= 0.06f) collected.add(index)
            if (index !in collected && x in -0.08f..1.08f) visible.add(Seed(index, x, y))
        }
        val edge = if (!session.calm && session.dragging && (target <= 0.08f || target >= 0.92f)) session.edgeHoldMs + delta else 0f
        val elapsed = session.elapsedMs + delta
        val reason = if (session.calm) null else when {
            collected.size >= session.goal -> EndReason.Goal
            edge >= 2400f -> EndReason.Edge
            elapsed >= 60000f -> EndReason.Time
            else -> null
        }
        return session.copy(height = height, targetHeight = target, elapsedMs = elapsed, worldOffset = travel,
            edgeHoldMs = edge, wind = wind, visibleSeeds = visible, collectedIds = collected,
            endReason = reason, phase = if (reason == null) FlightPhase.Playing else FlightPhase.Finished)
    }
}
