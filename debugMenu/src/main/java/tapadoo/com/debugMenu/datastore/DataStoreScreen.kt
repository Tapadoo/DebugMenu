package com.tapadoo.debugmenu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tapadoo.debugmenu.datastore.BasePrefs
import kotlinx.coroutines.launch

private enum class DataType { INT, LONG, FLOAT, DOUBLE, BOOLEAN, STRING, NONE }
private data class Entry(
    val key: String,
    val prefs: BasePrefs,
    val type: DataType,
    var value: String?,
) {
    val keyNameWithType: String get() = "$key (${type.name})"
}

@Composable
internal fun DataStoreScreen(stores: List<DataStore<Preferences>>) {
    val items = remember { mutableStateListOf<Entry>() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(stores) {
        items.clear()
        stores.forEach { store ->
            val prefs = BasePrefs(store)
            prefs.entriesAsMap()?.forEach { (k, v) ->
                val type = when (v) {
                    is Int -> DataType.INT
                    is Long -> DataType.LONG
                    is Float -> DataType.FLOAT
                    is Double -> DataType.DOUBLE
                    is Boolean -> DataType.BOOLEAN
                    is String -> DataType.STRING
                    else -> DataType.NONE
                }
                items.add(
                    Entry(
                        key = k.name, prefs = prefs, type = type, value = v?.toString()
                    )
                )
            }
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items) { index, item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Text(
                    text = item.keyNameWithType,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface
                )
                when (item.type) {
                    DataType.BOOLEAN -> {
                        val checked = (item.value?.toBooleanStrictOrNull()) ?: false
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Checkbox(
                                checked = checked, onCheckedChange = { new ->
                                    items[index] = item.copy(value = new.toString())
                                })
                            IconButton(onClick = {
                                scope.launch {
                                    item.prefs.setBoolean(item.key, items[index].value?.toBooleanStrictOrNull())
                                }
                            }) {
                                Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                            }
                        }
                    }
                    DataType.STRING, DataType.INT, DataType.LONG, DataType.FLOAT, DataType.DOUBLE, DataType.NONE -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(0.8f),
                                value = item.value ?: "",
                                onValueChange = { new -> items[index] = item.copy(value = new) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = when (item.type) {
                                        DataType.INT, DataType.LONG -> androidx.compose.ui.text.input.KeyboardType.Number
                                        DataType.FLOAT, DataType.DOUBLE -> androidx.compose.ui.text.input.KeyboardType.Decimal
                                        else -> androidx.compose.ui.text.input.KeyboardType.Text
                                    }
                                ),
                                label = { Text("${item.key}") })
                            IconButton(onClick = {
                                scope.launch {
                                    val v = items[index].value?.trim()
                                    when (item.type) {
                                        DataType.INT -> item.prefs.setInt(item.key, v?.toIntOrNull())
                                        DataType.LONG -> item.prefs.setLong(item.key, v?.toLongOrNull())
                                        DataType.FLOAT -> item.prefs.setFloat(item.key, v?.toFloatOrNull())
                                        DataType.DOUBLE -> item.prefs.setDouble(item.key, v?.toDoubleOrNull())
                                        DataType.STRING, DataType.NONE -> item.prefs.setString(item.key, v)
                                        DataType.BOOLEAN -> { /* handled above */
                                        }
                                    }
                                }
                            }) {
                                Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}
