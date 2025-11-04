package com.tapadoo.debugmenu.dynamic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner


data class DynamicAction(
    val title: String,
    val description: String? = null,
    val onClick: () -> Unit
)


object DynamicModuleState {

    val action: SnapshotStateList<DynamicAction> = mutableStateListOf()

    fun addAction(title: String, description: String? = null, onClick: () -> Unit) {
        action.add(DynamicAction(title, description, onClick))
    }

    fun remove(option: DynamicAction) = action.remove(option)

    fun clear() = action.clear()
}

@Composable
fun DynamicModuleActions(lifecycleOwner: LifecycleOwner, vararg options: DynamicAction) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                options.forEach { DynamicModuleState.addAction(it.title, it.description, it.onClick) }
            }
            if (event == Lifecycle.Event.ON_PAUSE) {
                options.forEach { DynamicModuleState.remove(it) }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            options.forEach { DynamicModuleState.remove(it) }
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}