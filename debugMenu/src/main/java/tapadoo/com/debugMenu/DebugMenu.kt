package com.tapadoo.debugmenu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tapadoo.debugmenu.ui.DebugFab
import com.tapadoo.debugmenu.ui.DebugMenuSheet

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DebugMenuOverlay(
    modifier: Modifier = Modifier,
    dataStores: List<DataStore<Preferences>> = emptyList(),
    showFab: Boolean = true,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    colorScheme: ColorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
) {
    var showContent by remember { mutableStateOf(false) }
    MaterialTheme(
        colorScheme = colorScheme,
    ) {
        Box(modifier = Modifier.fillMaxSize().then(modifier)) {
            if (showFab) {
                DebugFab { showContent = !showContent }
            }
        }
        if (showContent) {
            DebugMenuSheet(
                onDismiss = { showContent = !showContent },
                sheetState = sheetState,
                dataStores = dataStores
            )
        }
    }
}
