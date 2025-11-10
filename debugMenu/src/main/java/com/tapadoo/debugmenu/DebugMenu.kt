package com.tapadoo.debugmenu

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.tapadoo.debugmenu.analytics.AnalyticsModule
import com.tapadoo.debugmenu.module.DebugMenuModule
import com.tapadoo.debugmenu.ui.DebugFab
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenuOverlay(
    modules: List<DebugMenuModule> = listOf(AnalyticsModule()),
    modifier: Modifier = Modifier,
    showFab: Boolean = true,
    enableShake: Boolean = false,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    colorScheme: ColorScheme = getTheme()
) {
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current


    var showContent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf<DebugMenuModule>(modules.first()) }
    val selectedIndex by remember { derivedStateOf { modules.indexOf(selected) } }


    // Shake detector
    if (enableShake) {
        val shakeDetector = remember {
            ShakeDetector(context) {
                showContent = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }

        DisposableEffect(Unit) {
            shakeDetector.start()
            onDispose {
                shakeDetector.stop()
            }
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(modifier)
        ) {
            if (showFab) {
                DebugFab { showContent = !showContent }
            }
        }
        if (showContent) {

            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch {
                        showContent = !showContent
                        sheetState.hide()
                    }
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
                        if (modules.size > 1) {
                            PrimaryScrollableTabRow(
                                selectedTabIndex = selectedIndex,
                            ) {
                                modules.forEach { module ->
                                    Tab(
                                        selected = selected == module,
                                        onClick = { selected = module },
                                        text = { Text(module.title, maxLines = 1) })
                                }
                            }
                        }
                        Surface {
                            selected.Content()
                        }
                    }
                }
            }
        }


    }
}


@Composable
internal fun getTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
): ColorScheme {
    return when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

}