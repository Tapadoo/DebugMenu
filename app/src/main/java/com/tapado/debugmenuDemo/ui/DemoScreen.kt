package com.tapado.debugmenuDemo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tapadoo.debugmenu.dynamic.DynamicAction
import com.tapadoo.debugmenu.dynamic.DynamicModuleActions

@Composable
fun DemoScreen(viewModel: DemoViewModel, onLoggedIn: () -> Unit) {
    val uiState by viewModel.state.collectAsState()
    val nameInput by viewModel.userNameInput.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DynamicModuleActions(
        lifecycleOwner, DynamicAction("Log in") {
            viewModel.onUserNameChanged("fernando@gmail.com")
            viewModel.saveUserName()
            onLoggedIn()
        }
    )


    LaunchedEffect(uiState.userName) {
        // Sync input with current state when it changes externally (e.g., via DebugMenu)
        if (nameInput != uiState.userName) {
            viewModel.onUserNameChanged(uiState.userName)
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "DebugMenu Demo",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Observed from DataStore :", fontFamily = FontFamily.Monospace)
                    Text("user_name = ${uiState.userName}", fontFamily = FontFamily.Monospace)
                    Text("feature_enabled = ${uiState.featureEnabled}", fontFamily = FontFamily.Monospace)
                }
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = nameInput,
                onValueChange = viewModel::onUserNameChanged,
                label = { Text("User name") }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Feature enabled")
                Spacer(Modifier.weight(1f))
                Switch(checked = uiState.featureEnabled, onCheckedChange = viewModel::setFeatureEnabled)
            }
            Button(onClick = viewModel::saveUserName) { Text("Save name") }

            Spacer(Modifier.height(12.dp))
            Text(
                text = "Open the floating action button to view the Debug Menu. Try editing the DataStore values in the DataStore tab; changes will reflect here.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}