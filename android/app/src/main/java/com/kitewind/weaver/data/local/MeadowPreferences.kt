package com.kitewind.weaver.data.local

import android.animation.ValueAnimator
import android.content.Context
import com.kitewind.weaver.domain.model.*
import org.json.JSONObject

class MeadowPreferences(context: Context) {
    private val preferences by lazy { context.getSharedPreferences("kite_wind_weaver_local", Context.MODE_PRIVATE) }
    fun read(hills: List<HillSpec>, kites: List<KiteSpec>): MeadowProgress {
        val values = preferences.all
        fun number(key: String, default: Int = 0) = (values[key] as? Number)?.toInt() ?: default
        fun flag(key: String, default: Boolean) = values[key] as? Boolean ?: default
        val flowers = hills.map { number(it.id).coerceIn(0, it.target) }
        val complete = hills.count { flowers[it.index] == it.target }
        val requestedHill = number("selected_hill").coerceIn(0, 5)
        val hill = if (requestedHill == 0 || flowers[requestedHill - 1] == hills[requestedHill - 1].target) requestedHill else 0
        val requestedKite = number("selected_kite").coerceIn(0, 3)
        return MeadowProgress(
            flowers = flowers, selectedHill = hill,
            selectedKite = if (complete >= kites[requestedKite].requiredHills) requestedKite else 0,
            lifetimeSeeds = number("lifetime_seeds").coerceAtLeast(flowers.sum()),
            bestFlight = number("best_flight").coerceIn(0, 48),
            settings = MeadowSettings(flag("sound", true), flag("haptics", false), flag("reduced_motion", !ValueAnimator.areAnimatorsEnabled())),
            lastResult = (values["last_result"] as? String)?.let { decodeResult(it) }
        )
    }
    fun write(progress: MeadowProgress, hills: List<HillSpec>): Boolean {
        val editor = preferences.edit().putInt("schema_version", 1)
        hills.forEach { editor.putInt(it.id, progress.flowers[it.index]) }
        return editor.putInt("selected_hill", progress.selectedHill).putInt("selected_kite", progress.selectedKite)
            .putInt("lifetime_seeds", progress.lifetimeSeeds).putInt("best_flight", progress.bestFlight)
            .putBoolean("sound", progress.settings.sound).putBoolean("haptics", progress.settings.haptics)
            .putBoolean("reduced_motion", progress.settings.reducedMotion)
            .putString("last_result", progress.lastResult?.let { encodeResult(it) }).commit()
    }
    private fun encodeResult(result: FlightResult): String = JSONObject().apply {
        put("id", result.id); put("hill", result.hill); put("kite", result.kite); put("calm", result.calm)
        put("reason", result.reason.name); put("seeds", result.seeds); put("added", result.flowersAdded)
        put("flowers", result.hillFlowers); put("target", result.target); put("seconds", result.elapsedSeconds)
        put("completed", result.completedHills); put("new_completed", result.newCompletedHills)
        put("next", result.nextHill ?: -1)
    }.toString()
    private fun decodeResult(raw: String): FlightResult? = runCatching {
        val value = JSONObject(raw)
        FlightResult(value.getLong("id"), value.getInt("hill").coerceIn(0, 5), value.getInt("kite").coerceIn(0, 3),
            value.getBoolean("calm"), EndReason.valueOf(value.getString("reason")), value.getInt("seeds").coerceAtLeast(0),
            value.getInt("added").coerceIn(0, 132), value.getInt("flowers").coerceIn(0, 32),
            value.getInt("target").coerceIn(12, 32), value.getInt("seconds").coerceAtLeast(0),
            value.getInt("completed").coerceIn(0, 6), value.getInt("new_completed").coerceIn(0, 6),
            value.optInt("next", -1).takeIf { it in 1..5 })
    }.getOrNull()
}
