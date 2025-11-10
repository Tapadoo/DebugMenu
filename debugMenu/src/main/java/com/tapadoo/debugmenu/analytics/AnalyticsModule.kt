package com.tapadoo.debugmenu.analytics

import androidx.compose.runtime.Composable
import com.tapadoo.debugmenu.module.DebugMenuModule
import com.tapadoo.debugmenu.ui.AnalyticsScreen

class AnalyticsModule : DebugMenuModule {

    override val title = "Analytics"

    @Composable
    override fun Content() {
        AnalyticsScreen()
    }

}