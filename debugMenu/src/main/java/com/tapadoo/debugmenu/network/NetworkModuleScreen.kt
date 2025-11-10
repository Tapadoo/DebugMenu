package com.tapadoo.debugmenu.network

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tapadoo.debugmenu.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun NetworkScreen() {
    var selectedRequest by remember { mutableStateOf<DebugNetworkRequest?>(null) }
    var selectedRequests by remember { mutableStateOf(setOf<DebugNetworkRequest>()) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (selectedRequests.isNotEmpty()) {
                        IconButton({ shareSelectedRequests(context, selectedRequests.toList()) }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Share,
                                    contentDescription = "Share"
                                )
                                Text(
                                    text = "${selectedRequests.size}",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        IconButton({ selectedRequests = setOf() }) {
                            Icon(
                                Icons.Outlined.Clear,
                                contentDescription = "Deselect All"
                            )
                        }
                    }
                    IconButton({ DebugNetworkEvents.events.clear() }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Clear all")
                    }
                }
            }
            items(DebugNetworkEvents.events.reversed()) { request ->
                val isSelected = selectedRequests.contains(request)
                NetworkRequestItem(
                    request = request,
                    isSelected = isSelected,
                    onClick = {
                        if (selectedRequests.isNotEmpty()) {
                            selectedRequests = if (isSelected) {
                                selectedRequests - request
                            } else {
                                selectedRequests + request
                            }
                        } else {
                            selectedRequest = request
                        }
                    },
                    onLongClick = {
                        selectedRequests = if (isSelected) {
                            selectedRequests - request
                        } else {
                            selectedRequests + request
                        }
                    }
                )
            }
        }

        selectedRequest?.let { request ->
            NetworkRequestDetailDialog(
                request = request,
                onDismiss = { selectedRequest = null }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun NetworkRequestItem(
    request: DebugNetworkRequest,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerLowest
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                StatusIndicator(isSuccessful = request.isSuccessful)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = request.code.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getStatusCodeName(request.code),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "${request.durationMs}ms",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = request.url.substringAfterLast("/"),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = request.method,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Row {
                Text(
                    text = "↑ ${formatBytes(request.requestSize)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "↓ ${formatBytes(request.response?.length?.toLong() ?: 0)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(isSuccessful: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(
                color = if (isSuccessful) Color(0xFF4CAF50) else Color(0xFFF44336),
                shape = CircleShape
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkRequestDetailDialog(
    request: DebugNetworkRequest,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusIndicator(isSuccessful = request.isSuccessful)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${request.code} ${getStatusCodeName(request.code)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Tabs
            var selectedTabIndex by remember { mutableIntStateOf(0) }
            val tabs = listOf("Request", "Response")

            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Content
            when (selectedTabIndex) {
                0 -> RequestTab(request)
                1 -> ResponseTab(request)
            }
        }
    }
}

@Composable
private fun RequestTab(request: DebugNetworkRequest) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            SectionTitle("URL:")
            SelectableText(request.url)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SectionTitle("Request Headers")
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(request.headers.entries.toList()) { (key, value) ->
            HeaderItem(key = key, value = value)
        }

        if (request.body.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Body")
                Spacer(modifier = Modifier.height(8.dp))
                CodeBlock(request.body)
            }
        }
    }
}

@Composable
private fun ResponseTab(request: DebugNetworkRequest) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(label = "Status", value = request.code.toString())
                InfoChip(label = "Time", value = "${request.durationMs}ms")
                InfoChip(label = "Size", value = formatBytes(request.response?.length?.toLong() ?: 0))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SectionTitle("Response Headers")
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(request.responseHeaders.entries.toList()) { (key, value) ->
            HeaderItem(key = key, value = value)
        }

        if (!request.response.isNullOrEmpty()) {
            item {
                SectionTitle("Response Body")
                Spacer(modifier = Modifier.height(8.dp))
                CodeBlock(request.response)
            }
        }

        if (request.error != null) {
            item {
                SectionTitle("Error")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = request.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun SelectableText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun HeaderItem(key: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$key : [$value]",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CodeBlock(code: String) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(12.dp)
                .padding(end = 32.dp)
        )
        IconButton(
            onClick = { clipboardManager.setText(AnnotatedString(code)) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
                .padding(4.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_copy_app),
                contentDescription = "Copy",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/* Utils */

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

private fun shareSelectedRequests(context: Context, selectedRequests: List<DebugNetworkRequest>) {
    if (selectedRequests.isEmpty()) return

    val requestsText = selectedRequests.joinToString("\n\n" + "=".repeat(80) + "\n\n") { request ->
        buildString {
            append("${request.method} ${request.code} ${getStatusCodeName(request.code)}\n")
            append("URL: ${request.url}\n")
            append("Duration: ${request.durationMs}ms\n")
            append("Request Size: ${formatBytes(request.requestSize)}\n")
            append("Response Size: ${formatBytes(request.response?.length?.toLong() ?: 0)}\n")
            append("\n--- Request Headers ---\n")
            request.headers.forEach { (key, value) ->
                append("$key: $value\n")
            }
            if (request.body.isNotEmpty()) {
                append("\n--- Request Body ---\n")
                append(request.body)
                append("\n")
            }
            if (!request.response.isNullOrEmpty()) {
                append("\n--- Response Body ---\n")
                append(request.response)
                append("\n")
            }
            if (request.error != null) {
                append("\n--- Error ---\n")
                append(request.error)
                append("\n")
            }
        }
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, requestsText)
        putExtra(Intent.EXTRA_SUBJECT, "Network Requests (${selectedRequests.size} items)")
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share Network Requests"))
}


private fun getStatusCodeName(code: Int): String {
    return when (code) {
        // 1xx Informational
        100 -> "Continue"
        101 -> "Switching Protocols"
        102 -> "Processing"
        103 -> "Early Hints"
        // 2xx Success
        200 -> "OK"
        201 -> "Created"
        202 -> "Accepted"
        203 -> "Non-Authoritative Information"
        204 -> "No Content"
        205 -> "Reset Content"
        206 -> "Partial Content"
        207 -> "Multi-Status"
        208 -> "Already Reported"
        226 -> "IM Used"
        // 3xx Redirection
        300 -> "Multiple Choices"
        301 -> "Moved Permanently"
        302 -> "Found"
        303 -> "See Other"
        304 -> "Not Modified"
        305 -> "Use Proxy"
        307 -> "Temporary Redirect"
        308 -> "Permanent Redirect"
        // 4xx Client Error
        400 -> "Bad Request"
        401 -> "Unauthorized"
        402 -> "Payment Required"
        403 -> "Forbidden"
        404 -> "Not Found"
        405 -> "Method Not Allowed"
        406 -> "Not Acceptable"
        407 -> "Proxy Authentication Required"
        408 -> "Request Timeout"
        409 -> "Conflict"
        410 -> "Gone"
        411 -> "Length Required"
        412 -> "Precondition Failed"
        413 -> "Payload Too Large"
        414 -> "URI Too Long"
        415 -> "Unsupported Media Type"
        416 -> "Range Not Satisfiable"
        417 -> "Expectation Failed"
        418 -> "I'm a teapot"
        421 -> "Misdirected Request"
        422 -> "Unprocessable Entity"
        423 -> "Locked"
        424 -> "Failed Dependency"
        425 -> "Too Early"
        426 -> "Upgrade Required"
        428 -> "Precondition Required"
        429 -> "Too Many Requests"
        431 -> "Request Header Fields Too Large"
        451 -> "Unavailable For Legal Reasons"
        // 5xx Server Error
        500 -> "Internal Server Error"
        501 -> "Not Implemented"
        502 -> "Bad Gateway"
        503 -> "Service Unavailable"
        504 -> "Gateway Timeout"
        505 -> "HTTP Version Not Supported"
        506 -> "Variant Also Negotiates"
        507 -> "Insufficient Storage"
        508 -> "Loop Detected"
        510 -> "Not Extended"
        511 -> "Network Authentication Required"
        else -> "Unknown"
    }
}
