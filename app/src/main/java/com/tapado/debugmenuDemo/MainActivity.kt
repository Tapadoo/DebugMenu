package com.tapado.debugmenuDemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.tapado.debugmenuDemo.data.demoDataStore
import com.tapado.debugmenuDemo.ui.DemoScreen
import com.tapado.debugmenuDemo.ui.DemoViewModel
import com.tapado.debugmenuDemo.ui.theme.DebugMenuTheme
import com.tapadoo.debugmenu.DebugMenuOverlay
import com.tapadoo.debugmenu.analytics.AnalyticsModule
import com.tapadoo.debugmenu.custom.DebugOption
import com.tapadoo.debugmenu.datastore.DataStoreModule

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: DemoViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = DemoViewModel.Companion.Factory(applicationContext)
        viewModel = ViewModelProvider(this, factory)[DemoViewModel::class.java]
        enableEdgeToEdge()

        // Demo: Attach the DebugMenu using the Attacher (no Compose dependency required in consumer app)
//        DebugMenuAttacher.attach(this, dataStores = listOf(repository.dataStore))

        setContent {
            DebugMenuTheme {
                Box(Modifier.fillMaxSize()) {
                    DemoScreen(viewModel = viewModel)
                    DebugMenuOverlay(
                        modules = listOf(
                            AnalyticsModule(),
                            DataStoreModule(
                                listOf(
                                    this@MainActivity.applicationContext.demoDataStore
                                )
                            ),
                        )
                    )
                }
            }
        }
    }
}