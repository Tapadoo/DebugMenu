package com.tapadoo.debugmenu.module

import androidx.compose.runtime.Composable

interface DebugMenuModule {
    val title: String


    @Composable
    fun Content()

}