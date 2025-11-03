package com.tapadoo.debugmenu.datastore

import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import com.tapadoo.debugmenu.module.DebugMenuModule
import com.tapadoo.debugmenu.ui.DataStoreScreen
import androidx.datastore.preferences.core.Preferences

class DataStoreModule(
    val dataStores: List<DataStore<Preferences>>,
) : DebugMenuModule{
    override val title: String = "Preferences"

    @Composable
    override fun Content() {
        DataStoreScreen(dataStores)
    }

}