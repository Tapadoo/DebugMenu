package com.tapadoo.debugmenu.logs

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapadoo.debugmenu.module.DebugMenuModule

class LoggingModule: DebugMenuModule {
    override val title: String = "Logs"

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        var invertSort by remember { mutableStateOf(false) }
        var selectedLogs by remember { mutableStateOf(setOf<LogItem>()) }
        var expandedLogs by remember { mutableStateOf(setOf<LogItem>()) }
        val context = LocalContext.current

        val logs by remember {
            derivedStateOf {
                if (invertSort) {
                    DebugLogs.events.toList().sortedBy { it.timestampMs }
                } else {
                    DebugLogs.events.toList().sortedByDescending { it.timestampMs }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentPadding = PaddingValues(top = 0.dp, bottom = 12.dp, start = 0.dp, end = 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    if (selectedLogs.isNotEmpty()) {
                        IconButton({ shareSelectedLogs(context, selectedLogs.toList()) }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Share,
                                    contentDescription = "Share"
                                )
                                Text(
                                    text = "${selectedLogs.size}",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        IconButton({ selectedLogs = setOf() }) {
                            Icon(
                                Icons.Outlined.Clear,
                                contentDescription = "Deselect All"
                            )
                        }
                    }
                    IconButton({ DebugLogs.clear() }) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Clear All"
                        )
                    }
                    IconButton({ invertSort = !invertSort }) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "Toggle Sort"
                        )
                    }
                }
            }
            items(logs) { item ->
                val isExpanded = expandedLogs.contains(item)
                val isSelected = selectedLogs.contains(item)
                
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                        .combinedClickable(
                            onClick = {
                                if (selectedLogs.isNotEmpty()) {
                                    // If in selection mode, toggle selection on click
                                    selectedLogs = if (isSelected) {
                                        selectedLogs - item
                                    } else {
                                        selectedLogs + item
                                    }
                                } else {
                                    // Otherwise toggle expansion
                                    expandedLogs = if (isExpanded) {
                                        expandedLogs - item
                                    } else {
                                        expandedLogs + item
                                    }
                                }
                            },
                            onLongClick = {
                                selectedLogs = if (isSelected) {
                                    selectedLogs - item
                                } else {
                                    selectedLogs + item
                                }
                            }
                        )
                        .padding(12.dp)
                ) {
                    Row {
                        if (item.tag != null) {
                            Text(
                                text = "[${item.tag}] ",
                                maxLines = 1,
                                modifier = Modifier.basicMarquee(),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = getPriorityLabel(item.priority),
                            maxLines = 1,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = getPriorityColor(item.priority)
                        )
                    }
                    Text(
                        text = item.message,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.throwable != null) {
                        Text(
                            text = item.throwable.stackTraceToString(),
                            maxLines = if (isExpanded) Int.MAX_VALUE else 5,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            ),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun getPriorityColor(priority: Int) = when (priority) {
        2 -> MaterialTheme.colorScheme.secondary // VERBOSE
        3 -> MaterialTheme.colorScheme.onSurfaceVariant // DEBUG
        4 -> MaterialTheme.colorScheme.primary // INFO
        5 -> MaterialTheme.colorScheme.tertiary // WARN
        6 -> MaterialTheme.colorScheme.error // ERROR
        7 -> MaterialTheme.colorScheme.error // ASSERT
        else -> MaterialTheme.colorScheme.onSurface
    }

    private fun getPriorityLabel(priority: Int) = when (priority) {
        2 -> "VERBOSE"
        3 -> "DEBUG"
        4 -> "INFO"
        5 -> "WARN"
        6 -> "ERROR"
        7 -> "ASSERT"
        else -> "UNKNOWN"
    }

    private fun shareSelectedLogs(context: Context, selectedLogs: List<LogItem>) {
        if (selectedLogs.isEmpty()) return
        
        val logsText = selectedLogs.joinToString("\n\n") { logItem ->
            buildString {
                append("[${getPriorityLabel(logItem.priority)}]")
                if (logItem.tag != null) {
                    append(" [${logItem.tag}]")
                }
                append("\n")
                append(logItem.message)
                if (logItem.throwable != null) {
                    append("\n")
                    append(logItem.throwable.stackTraceToString())
                }
                append("\nTimestamp: ${logItem.timestampMs}")
            }
        }
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, logsText)
            putExtra(Intent.EXTRA_SUBJECT, "Debug Logs (${selectedLogs.size} items)")
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share Logs"))
    }
}