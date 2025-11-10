package com.tapadoo.debugmenu.network

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList


data class DebugNetworkRequest(
    val url: String,
    val method: String,
    val headers: Map<String, String>,
    val responseHeaders: Map<String, String>,
    val body: String,
    val timestamp: Long,
    val durationMs: Long,
    val isSuccessful: Boolean,
    val code: Int,
    val error: String? = null,
    val response: String? = null,
    val requestSize: Long = 0,
) {
    companion object
}


object DebugNetworkEvents {
    val events: SnapshotStateList<DebugNetworkRequest> = mutableStateListOf()

    fun addEvent(event: DebugNetworkRequest) = events.add(event)
}
