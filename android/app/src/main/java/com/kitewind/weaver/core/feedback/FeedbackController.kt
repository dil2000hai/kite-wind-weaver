package com.kitewind.weaver.core.feedback

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock

class FeedbackController {
    private var generator: ToneGenerator? = null
    private var foreground = false
    private var lastTone = 0L
    fun start() { foreground = true }
    fun collection(sound: Boolean) {
        if (!foreground || !sound || SystemClock.elapsedRealtime() - lastTone < 150L) return
        lastTone = SystemClock.elapsedRealtime()
        runCatching {
            val tone = generator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 10).also { generator = it }
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
        }
    }
    fun stop() {
        foreground = false
        generator?.release()
        generator = null
    }
}
