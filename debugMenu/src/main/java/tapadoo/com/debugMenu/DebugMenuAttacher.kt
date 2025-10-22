package com.tapadoo.debugmenu

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Attaches the Debug Menu overlay to any Activity without requiring the app module to depend on
 * Compose APIs directly. Call once from your Activity onCreate().
 */
object DebugMenuAttacher {
    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    @JvmStatic
    fun attach(
        activity: Activity,
        dataStores: List<DataStore<Preferences>> = emptyList(),
    ) = runCatching {
        val decor = activity.window?.decorView as? ViewGroup ?: return@runCatching
        // Avoid duplicates
        val existing = decor.findViewWithTag<FrameLayout>(TAG)
        if (existing != null) return@runCatching

        val container = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = false
            isFocusable = false
            isFocusableInTouchMode = false
            importantForAccessibility = ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO
            tag = TAG
        }

        val composeView = ComposeView(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setContent {
                DebugMenuOverlay(
                    dataStores = dataStores,
                    showFab = true
                )
            }
        }
        container.addView(composeView)
        decor.addView(container)
    }

    private const val TAG = "debugMenu_overlay_container"
}