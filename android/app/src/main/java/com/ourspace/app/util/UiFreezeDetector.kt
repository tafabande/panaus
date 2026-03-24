package com.ourspace.app.util

import android.view.Choreographer
import com.google.firebase.crashlytics.FirebaseCrashlytics

class UiFreezeDetector : Choreographer.FrameCallback {

    private var lastFrameTimeNanos: Long = 0L
    private val freezeThresholdNanos: Long = 300_000_000L // 300ms threshold for severe freeze

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
                val exception = RuntimeException("Severe UI Freeze detected: skipped frames for $freezeMs ms")
                FirebaseCrashlytics.getInstance().recordException(exception)
                
                // Optionally let the user know their device froze for a moment
                // GlobalErrorHandler.recordException(exception, "UI paused for ${freezeMs}ms executing slow code.")
            }
        }
        lastFrameTimeNanos = frameTimeNanos
        Choreographer.getInstance().postFrameCallback(this) // keep listening for next frames
    }
}
