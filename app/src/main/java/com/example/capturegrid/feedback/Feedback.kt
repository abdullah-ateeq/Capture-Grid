package com.example.capturegrid.feedback

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.staticCompositionLocalOf

enum class Haptic { PLACE, CAPTURE, INVALID, SUCCESS }

/** Plays sound and haptic feedback, honouring the user's settings. */
class FeedbackController(
    private val sounds: SoundEffects?,
    private val view: View?,
    private val soundEnabled: () -> Boolean,
    private val hapticsEnabled: () -> Boolean
) {
    fun sound(sfx: Sfx) {
        if (soundEnabled()) sounds?.play(sfx)
    }

    fun haptic(type: Haptic) {
        val target = view ?: return
        if (!hapticsEnabled()) return
        val constant = when (type) {
            Haptic.PLACE -> HapticFeedbackConstants.CLOCK_TICK
            Haptic.CAPTURE -> HapticFeedbackConstants.CONTEXT_CLICK
            Haptic.INVALID ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
                else HapticFeedbackConstants.LONG_PRESS
            Haptic.SUCCESS ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
                else HapticFeedbackConstants.LONG_PRESS
        }
        target.performHapticFeedback(constant)
    }

    companion object {
        val Silent = FeedbackController(null, null, { false }, { false })
    }
}

val LocalFeedback = staticCompositionLocalOf { FeedbackController.Silent }
