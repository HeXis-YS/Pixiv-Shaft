package com.effective.android.panel

import androidx.fragment.app.Fragment

class PanelSwitchHelper private constructor() {

    class Builder(@Suppress("UNUSED_PARAMETER") fragment: Fragment) {
        fun contentScrollOutsideEnable(@Suppress("UNUSED_PARAMETER") enable: Boolean) = this
        fun logTrack(@Suppress("UNUSED_PARAMETER") enable: Boolean) = this
        fun build(@Suppress("UNUSED_PARAMETER") autoInit: Boolean): PanelSwitchHelper = PanelSwitchHelper()
    }

    fun resetState() {
        // No-op local replacement.
    }

    fun hookSystemBackByPanelSwitcher(): Boolean = false
}
