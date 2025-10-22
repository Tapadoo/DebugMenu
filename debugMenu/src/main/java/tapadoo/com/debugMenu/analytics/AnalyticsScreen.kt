package com.tapadoo.debugmenu.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.tapadoo.debugmenu.analytics.DebugAnalytics

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AnalyticsScreen() {
    var invertSort by remember { mutableStateOf(false) }

    val events by derivedStateOf {
        if (invertSort) {
            DebugAnalytics.events.toList().sortedBy { it.timestampMs }
        } else {
            DebugAnalytics.events.toList().sortedByDescending { it.timestampMs }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentPadding = PaddingValues( top = 0.dp, bottom = 12.dp, start = 0.dp, end = 0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton({ DebugAnalytics.events.clear() }) {
                    Icon(
                        Icons.Outlined.Delete, null
                    )
                }
                IconButton({ invertSort = !invertSort }) {
                    Icon(
                        Icons.AutoMirrored.Filled.List, null
                    )
                }
            }
        }
        items(events) { item ->
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(12.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.params.isNotEmpty()) {
                    Text(
                        text = item.params.entries.joinToString("\n") { (k, v) -> "$k: $v" },
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
