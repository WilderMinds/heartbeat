## Setup
In your Activity class, 

### Initialize the [`AppIdentifier`](https://github.com/WilderMinds/heartbeat/blob/master/heartbeat/src/main/java/com/samdev/heartbeat/models/AppIdentifier.kt) class
This class contains data that will help identify the device and app instance sending the payload
```
val appIdentifier = AppIdentifier(
    appId = BuildConfig.APPLICATION_ID,
    appName = getString(R.string.app_name),
    appVer = BuildConfig.VERSION_NAME
)

// set a device id, if the device Id is not set, a random string will be generated for you
appIdentifier.deviceId = "0000001234567890"
```




### Initialize your [`ConfigParams`](https://github.com/WilderMinds/heartbeat/blob/master/heartbeat/src/main/java/com/samdev/heartbeat/models/ConfigParams.kt) class
```
val configParams = ConfigParams()

// Interval with which to send heartbeat payloads
configParams.triggerIntervalMillis = 10 * 60 * 1000 //10 mins

// Set whether or not the heartbeat library should track gps coordinates
configParams.trackDeviceLocation = true

// include your appIdentifier object
configParams.addIdentifier = initAppIdentifier()
```


The library is configured to be able to send payloads via either websocket or API calls.
If the protocol/scheme of the **`networkUrl`** provided is either "**http**" or "**https**", it will automatically 
attempt to send payloads via API calls using Retrofit.
But if the protocol/scheme is "**ws**" or "**wss**" it send payloads via a websocket connection

```
val ws_url = "wss://your.domain.com/"
val http_url = "https://api.your.domain/"
configParams.networkUrl = "..."
```


User also has an option to override either Networking Method
```
// force system to use API calls regardless of url scheme/protocol
configParams.forceSendViaApiCall = true

// force system to use Websockets regardless or url scheme/protocol
configParams.forceSendViaWebSocket = true

// set this flag to keep websocket connection open
// Only set if using websockets to send payload
configParams.isPersistSocketConnection = false
```



## Finally Start the service
```
// assign previously initialized config params
HeartbeatController.instance.configParams = configParams

// start the heartbeat service
try {
    HeartbeatController.instance.startService(context)
} catch (e: Exception) {
    e.printStackTrace()
}
```



### If you want to include & track additional data from your app via the heartbeat payload
Create a **`Map`** and set the new values you want to track as the keys
```
val additionalParams: MutableMap<String, String> = HashMap()
additionalParams["last_db_sync"] = "0123456789"
additionalParams["some_other_data"] = "set here"
```


Include them using the [`HeartbeatController`](https://github.com/WilderMinds/heartbeat/blob/master/heartbeat/src/main/java/com/samdev/heartbeat/HeartbeatController.kt).
```
 HeartbeatController.instance.setAdditionalParams(additionalParams)
```
If at any point you want to remove your newly added params from the heartbeat payload, you can
call the same method above and set an empty map.


The values of the additional params can also be updated anywhere in the app using
```
HeartbeatController.instance.addOrUpdateAdditionalParams("key", "value")
```
New params can also be added using the same method above.


### Callbacks
Includes some convenient callbacks
```
class MainActivity : AppCompatActivity(), ApplicationCallbacks {

   ...
   
   override fun onLocationDisabled() { ... }
   override fun onLocationEnabled() { ... }
   override fun onNetworkSuccess(payload: Any?) { ... }
   
   override fun onNetworkError(throwable: Throwable) { // Heartbeat upload failed, handle error }
   override fun onHeartbeatPayloadReceived(payload: String?) {
       // do something with received payload
   }
}
```



### Sample payload
```
{
  "appid": "com.samdev.heartbeatapplication",
  "appname": "Heartbeat Demo",
  "appver": "1.0",
  "device-id": "A3w&WRV*DS43qfH0",
  "heartbeat": {
    "battery": "0.68",
    "gps": "47.6063889,-122.3308333",
    "version": "1.0",
    "ip_address": "10.14.0.2",
    "rssi": "55",
    "network": "WIFI",
    "last_db_sync": "0123456789",
    "some_other_data": "set here",
    "timestamp": "2021-02-13 15:48:14"
  }
}
```

check out full implementation [here](https://github.com/WilderMinds/heartbeat/blob/master/app/src/main/java/com/samdev/heatbeatapplication/MainActivity.kt)

