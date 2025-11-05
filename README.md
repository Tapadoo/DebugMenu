## Android Debug Menu

A module-based debug menu to debug your Android application

A simple, modular library that adds a floating debug menu to your Android app, allowing you to view analytics events,
inspect DataStore preferences, monitor logs, and create custom debugging modules.

### Why?

Currently, there are a couple of other debug menu libraries,
like [Lens-Logger](https://github.com/farhazulMullick/Lens-Logger/tree/feat/log-datastore)
and [Beagle](https://github.com/pandulapeter/beagle), however, they were either focused on a limited set of features or
interfered with the LayoutInpector in Android Studio.

This library offers a lightweight, easy-to-use, and modular debug menu that you can easily integrate into your app and
customise to your needs.

<details>
<summary>Demo</summary>
    <img src="https://github.com/Tapadoo/DebugMenu/blob/main/DEMO.gif"  alt="DebugMenu demo"/>
<br/>
</details>

## Getting Started

### Installation

1. Declare the library as a dependency in your app's `build.gradle`

```kotin
    implementation("com.tapadoo:debugmenu:<version>")
```

### Basic Usage

In order to use the library, you first need to add the FAB to your Activity/Composable; this changes depending on the
project.

<details>
<summary>Single-Activity Composable App</summary>
<br/>
If you're using a single-activity Composable, you can add the `DebugMenuOverlay` to your top-level composable.

```kotlin
setContent {
    AppTheme {
        NavigationComposable()
        // Only show the debug menu in debug builds
        if (BuildConfig.DEBUG) {
            DebugMenuOverlay(
                modules = listOf(
                    // Your Modules...
                ),
            )
        }
    }
}
```

</details>

<details>
<summary>Multi-Activity App</summary>
<br/>
If your project is a multi-activity navigation, you can use the `DebugMenuAttacher.attachToApplication` method to your
`Activity.onCreate()` to attach the debug menu to your whole application. 

This also has the advantage of not having to use Compose in your project.

```kotlin
class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            DebugMenuAttacher.attachToApplication(
                this,
                listOf(
                    // Your Modules...
                )
            )
        }
    }
}
```

</details>

<details>
<summary>Single Activity Fragment App</summary>
<br/>
If your project is a single activity but uses Fragments for navigation, you can use the `DebugMenuAttacher.attach` method to your `Activity.onCreate()` to attach the debug menu to your whole application. 

This also has the advantage of not having to use Compose in your project.

```kotlin
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            DebugMenuAttacher.attach(
                this,
                listOf(
                    // Your Modules...
                )
            )
        }
    }
}
```

</details>

## Integrating Modules

> Note: Modules determine the order in which they are displayed in the debug menu.

<details>
<summary>Analytics Module</summary>
<br/>

**Adding Analytics Module**

Just add the `DebugAnalytics` singleton to your list of Modules, and you're good to go.

```kotlin
DebugMenuOverlay(
    modules = listOf(
        AnalyticsModule(),
        // Rest of your modules...
    ),
)
```

**Logging Analytics Events**

Most analytics libraries have a common schema, like `event_name` and `event_properties` that usually is a map of
key-value pairs, you can
just add the event to the `DebugAnalytics` singleton, and it will be logged in the debug menu.

> **Note:** The signature of the `logEvent` method is the same as Firebase Analytics, so if you're using Firebase
> Analytics,
> you can just call it with the same signature.

```kotlin
class AnalyticsManager {
    fun logEvent(event: AnalyticsEvent) {
        // create a bundle or map from an event
        DebugAnalytics.logEvent(event.name, bundle) // <-- Add the event to the DebugMenu
        firebaseAnalytics.logEvent(event.name, bundle)
    }
}

```

</details>

<details>
<summary>DataStore Module</summary>
<br/>

**Adding DataStore Module**

Just add the `DataStoreModule` class to your list of Modules, and pass the list of DataStores and you're good to go. The
UI Will automatically generate the UI for every entry.

```kotlin
DebugMenuOverlay(
    modules = listOf(
        DataStoreModule(
            listOf(
                this@MainActivity.applicationContext.demoDataStore
            )
        ),
        // Rest of your modules...
    ),
)
```

**Exposing DataStores to the DebugMenu**

Most Repositories use DataStore privately to store data, you can convert it into an extension function on top of the
`Context` type:

```kotlin
val Context.demoDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_datastore_key")
``` 
And then pass it to the `DebugMenuOverlay` or `DebugMenuAttacher`

```kotlin
DebugMenuAttacher.attach(
    this,
    modules = listOf(
        DataStoreModule(listOf(this@MainActivity.applicationContext.demoDataStore)),
        // Rest of your modules...
    ),
)
// or
DebugMenuOverlay(
    modules = listOf(
        DataStoreModule(
            listOf(this@MainActivity.applicationContext.demoDataStore)
        ),
        // Rest of your modules...
    ),
)

```

</details>

<details>
<summary>Logging Module</summary>
<br/>
The logging module captures log messages from your app and displays them in the debug menu. 

**Adding the module**

```kotlin
DebugMenuAttacher.attach(
    this,
    modules = listOf(
        LoggingModule(),
        // Rest of your modules...
    ),
)
// or
DebugMenuOverlay(
    modules = listOf(
        LoggingModule(),
        // Rest of your modules...
    ),
)
```

**Integrating Logs**
Integrate it by wrapping your existing logger. (Example with Timber)

```kotlin
class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(
                object : Timber.DebugTree() {
                    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                        super.log(priority, tag, message, t)
                        DebugLogs.log(priority = priority, tag = "$tag", message = message, t = t)
                    }
                }
            )
        }
    }
}
```

</details>

<details>
<summary>Dynamic Module</summary>
<br/>
The dynamic module allows you to add custom actions to the debug menu. They can either be:

- Global actions: These actions are displayed in the debug menu, and can be triggered from anywhere in the app.
- Dynamic Actions: These actions are only displayed when the user is in a specific screen and get automatically removed
  when the user navigates away from that screen.

**Adding the module & Global Actions**

```kotlin
DebugMenuAttacher.attach(
    this,
    modules = listOf(
        DynamicModule(
            globalActions = listOf(
                DynamicAction("Global Action 1") {
                    // Perform global action
                }
            )
        ),
        // Rest of your modules...
    ),
)
// or
DebugMenuOverlay(
    modules = listOf(
        DynamicModule(
            globalActions = listOf(
                DynamicAction("Global Action 1") {
                    // Perform global action
                }
            )
        ),
        // Rest of your modules...
    )
)
```

**Adding Dynamic Actions**

These will only be displayed when the user is in the screen where they are added, ideal for actions that are only
relevant to that screen.

Compose:
```kotlin
@Composable
fun YourScreenComposable() {
    val lifecycleOwner = LocalLifecycleOwner.current
    DynamicModuleActions(
        lifecycleOwner, DynamicAction("Name of the action", "Description of the action (Optional)") {
            // Do something when the user clicks the action (i.e. talk to ViewModel)
        }
    )
}
```

Fragment/Activity:
```kotlin
 class MyFragment : Fragment() {
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         registerDynamicModuleActions(
             DynamicAction("Action 1") { /* action 1 */ },
         )
     }
 }

```

</details>

### Creating your own Module

You can create custom modules to display any debugging information specific to your app. For example, a network request
monitor, feature flags viewer, or app configuration inspector. Just extend the `DebugMenuModule` class and implement its
methods.


Made with Love by [Tapadoo](https://tapadoo.com)