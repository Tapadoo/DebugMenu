package com.tapadoo.debugmenu.ui

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheetDefaults.properties
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.launch


private enum class DebugTab(val label: String) { Analytics("Analytics"), DataStore("DataStore") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DebugMenuSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    dataStores: List<DataStore<Preferences>>,
) {
    val scope = rememberCoroutineScope()
    var selected by remember { mutableIntStateOf(DebugTab.Analytics.ordinal) }
    ModalBottomSheet(
        onDismissRequest = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    },
        sheetState = sheetState,
        tonalElevation = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                PrimaryTabRow(selectedTabIndex = selected) {
                    DebugTab.entries.forEachIndexed { index, tab ->
                        Tab(
                            selected = selected == index,
                            onClick = { selected = index },
                            text = { Text(tab.label, maxLines = 1) })
                    }
                }
                Surface {
                    when (DebugTab.entries[selected]) {
                        DebugTab.Analytics -> AnalyticsScreen()
                        DebugTab.DataStore -> DataStoreScreen(dataStores)
                    }
                }
            }
        }
    }
}
