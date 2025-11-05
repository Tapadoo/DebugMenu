package com.tapadoo.debugmenu.dynamic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner


/**
 * Represents a dynamic action with a title, an optional description, and an action to be executed when clicked.
 *
 * This can be used to define actions that are dynamically added or removed within a module
 * or executed upon certain user interactions in a composable UI.
 *
 * @property title The title of the action.
 * @property description An optional description providing additional details about the action.
 * @property onClick A lambda function to be executed when the action is triggered.
 */
data class DynamicAction(
    val title: String,
    val description: String? = null,
    val onClick: () -> Unit
)


/**
 * A singleton object that manages the state of dynamic actions in a module.
 *
 * This object provides a centralized mechanism to dynamically add, remove, or clear actions
 * represented by `DynamicAction`. These actions can be used for scenarios where the list of
 * options or operations needs to be updated dynamically. Actions added to this state
 * are observable and can be reflected in a UI, such as within a Composable interface.
 *
 * Properties:
 * - `action`: A mutable state-backed list of `DynamicAction`, holding the currently active dynamic actions.
 */
object DynamicModuleState {

    val action: SnapshotStateList<DynamicAction> = mutableStateListOf()

    fun addAction(title: String, description: String? = null, onClick: () -> Unit) {
        action.add(DynamicAction(title, description, onClick))
    }

    fun remove(option: DynamicAction) = action.remove(option)

    fun clear() = action.clear()
}

/**
 * A composable function that dynamically manages and displays module actions
 * based on the lifecycle events of the provided `LifecycleOwner`.
 *
 * @param lifecycleOwner The lifecycle owner that controls the lifecycle of the dynamic actions.
 * @param options A variable number of `DynamicAction` objects representing the actions
 * to be added or removed dynamically based on lifecycle state.
 */
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

/**
 * Extension function for LifecycleOwner to register dynamic module actions.
 * This function automatically handles the lifecycle of the actions, adding them when
 * the owner is resumed and removing them when paused or destroyed.
 *
 * Usage example:
 * ```
 * class MyFragment : Fragment() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         registerDynamicModuleActions(
 *             DynamicAction("Action 1") { /* action 1 */ },
 *             DynamicAction("Action 2") { /* action 2 */ }
 *         )
 *     }
 * }
 * ```
 *
 * @param options A variable number of DynamicAction objects to be managed.
 */
fun LifecycleOwner.registerDynamicModuleActions(vararg options: DynamicAction) {
    val observer = LifecycleEventObserver { obser, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> options.forEach {
                DynamicModuleState.addAction(it.title, it.description, it.onClick)
            }

            Lifecycle.Event.ON_PAUSE -> options.forEach {
                DynamicModuleState.remove(it)
            }

            Lifecycle.Event.ON_DESTROY -> {
                options.forEach { DynamicModuleState.remove(it) }
            }

            else -> { /* no-op */
            }
        }
    }
    lifecycle.addObserver(observer)
}


