package com.ourspace.app.util

import android.view.Choreographer
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Detects severe UI freezes (skipped frames > [freezeThresholdNanos]) and
 * reports them to Firebase Crashlytics as non-fatal exceptions so they show
 * up in the Crashlytics dashboard under "Non-fatals".
 */
class UiFreezeDetector : Choreographer.FrameCallback {

    private var lastFrameTimeNanos: Long = 0L
    private val freezeThresholdNanos: Long = 300_000_000L // 300 ms = severe freeze

    fun start() {
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun stop() {
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (lastFrameTimeNanos != 0L) {
            val deltaNanos = frameTimeNanos - lastFrameTimeNanos
            if (deltaNanos > freezeThresholdNanos) {
                val freezeMs = deltaNanos / 1_000_000L
                val exception = RuntimeException("Severe UI Freeze: skipped frames for ${freezeMs}ms")
                // Report the freeze as a non-fatal exception to Crashlytics
                try {
                    FirebaseCrashlytics.getInstance().apply {
                        setCustomKey("freeze_duration_ms", freezeMs)
                        recordException(exception)
                    }
                } catch (ignored: Exception) {
                    // Crashlytics not yet initialized – fail silently
                }
            }
        }
        lastFrameTimeNanos = frameTimeNanos
        Choreographer.getInstance().postFrameCallback(this)
    }
}
