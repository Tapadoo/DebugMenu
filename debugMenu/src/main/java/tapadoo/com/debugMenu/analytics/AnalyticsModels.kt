package com.tapadoo.debugmenu.analytics

import android.R.attr.value
import android.os.Bundle
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/** Public display model for analytics items shown in the debug menu */
data class AnalyticsItem(
    val name: String,
    val params: Map<String, String> = emptyMap(),
    val timestampMs: Long = System.currentTimeMillis(),
)

/** Internal state holder and public API for analytics */
object DebugAnalytics {
    private var mapper: ((Any) -> AnalyticsItem)? = null
    internal val events: SnapshotStateList<AnalyticsItem> = mutableStateListOf()


    /** Push an event coming from your AnalyticsManager.logEvent(event). */
    fun logEvent(eventName: String, bundle: Bundle) {
        val params = bundle.keySet().associateWith { key ->
            bundle.get(key).toString()
        }
        
        runCatching {
            events.add(AnalyticsItem(
                name = eventName,
                params = params
            ))
        }
    }

    fun logEvent(event: AnalyticsItem) {
        runCatching {
            events.add(event)
        }
    }

    fun clear() = events.clear()

    private fun defaultMap(event: Any): AnalyticsItem = AnalyticsItem(
        name = event::class.simpleName ?: "Event",
        params = mapOf("toString" to event.toString())
    )
}
