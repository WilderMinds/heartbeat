package com.expresspay.heartbeat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;

import com.expresspay.heartbeat.callbacks.LocationSettingsListener;
import com.expresspay.heartbeat.callbacks.TransactionListener;
import com.expresspay.heartbeat.controller.HeartbeatController;
import com.expresspay.heartbeat.models.ConfigParams;
import com.expresspay.heartbeat.models.Heartbeat;
import com.expresspay.heartbeat.models.Payload;
import com.expresspay.heartbeat.receivers.BatteryLevelBroadcastReceiver;
import com.expresspay.heartbeat.receivers.MyCellularStateListener;
import com.expresspay.heartbeat.callbacks.OnBroadcastDataReceived;
import com.expresspay.heartbeat.service.AlarmReceiver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketState;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class HeartbeatConfig implements OnBroadcastDataReceived {

    private static final int HEARTBEAT_INTERVAL_MILLIS = 10 * 60 * 1000;
    private static final String DEFAULT_LOCATION = "0.0,0.0";
    private static final int LOCATION_INTERVAL_MILLIS = 30 * 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private final Context context;
    private static ConfigParams configParams = new ConfigParams();
    private Map<String, String> additionalParams = new HashMap<>();

    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private TelephonyManager telephonyManager;
    private LocationManager locationManager;

    private int signalStrength = 0;
    private double batteryLevel = 0;
    private String currentLocation = DEFAULT_LOCATION;

    private MyCellularStateListener cellularStateListener;
    private BatteryLevelBroadcastReceiver batteryLevelBroadcastReceiver;
    private WebSocket webSocket;

    private boolean isAwaitingReconnection = false;

    private Payload payload = new Payload();
    private Heartbeat heartbeat = new Heartbeat();
    private LocationSettingsListener locationSettingsListener;


    public HeartbeatConfig(Context context, LocationSettingsListener locationSettingsListener) {
        this.context = context.getApplicationContext();
        this.locationSettingsListener = locationSettingsListener;
        configParams = HeartbeatController.getInstance().getConfigParams();

        HeartbeatController.getInstance().initSharedPrefs(context);
        initSystemServices();
        initWebSocketConnection();
        listenForLocationUpdates();
    }

    private void initWebSocketConnection() {
        webSocket = HeartbeatController.getInstance().getWebSocket();

        HeartbeatController.getInstance().getSocketListener()
                .setTransactionListener(new TransactionListener() {
                    @Override
                    public void onTextReceived(String text) {}

                    @Override
                    public void onDisconnected() {

                        if (configParams.isPersistSocketConnection()) {
                            if (webSocket.getState() == WebSocketState.CREATED) {
                                webSocket.connectAsynchronously();
                            } else {
                                webSocket = HeartbeatController.getInstance().reconnectWebSocket();
                            }
                        }
                    }

                    @Override
                    public void onMessageSent() {}

                    @Override
                    public void onConnected() {

                        Log.e("TAG", "Internal connected callback triggered, awaitingConnectionToSendHeartbeat => " + isAwaitingReconnection);
                        // if reconnect has been triggered while attempting to send a message
                        if (isAwaitingReconnection) {
                            isAwaitingReconnection = false;

                            Handler handler = new Handler(Looper.getMainLooper());
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("TAG", "2nd attempt to send message after socket reconnection");
                                    sendMessageHandler();
                                }
                            };

                            // allow 5 seconds for attempting a retry
                            handler.postDelayed(runnable, 3000);
                        }
                    }
                });
    }

    private void initSystemServices() {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        cellularStateListener = new MyCellularStateListener(this);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public String getIpAddress() {
        return getIp(); //Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }


    private String getIp() {
        String ipAddress = "0.0.0.0";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        Log.e("IP ADDRESS"," found address " + ipAddress);
                        ipAddress = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("IP ADDRESS"," Here is the final Address " + ipAddress);
        return ipAddress;
    }

    public String getNetworkType() {
       try {
           boolean onWifi;

           NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
           onWifi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
           Heartbeat.Connectivity connectivity = onWifi ? Heartbeat.Connectivity.WIFI : Heartbeat.Connectivity.MOBILE_DATA;

           return connectivity.name();
       }catch (Exception e) {
           e.printStackTrace();
           return Heartbeat.Connectivity.UNKNOWN.name();
       }
    }

    public String getSignalStrength() {
        // signal strength being returned is cellular, so check if connected network is wifi
        if (getNetworkType().equalsIgnoreCase(Heartbeat.Connectivity.WIFI.name())) {
            return getWifiSignalStrength();
        }

        return signalStrength + " dbm";
    }

    public String getBatteryLevel() {
        return batteryLevel + "";
    }

    private String getWifiSignalStrength() {

        String connectedBSSID = "";
        int signalStrength = -1;

        try {
            // get connected network
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                connectedBSSID = wifiInfo.getBSSID();
            }

            List<ScanResult> scanResults = wifiManager.getScanResults();
            for (ScanResult result : scanResults) {
                if (Objects.equals(result.BSSID, connectedBSSID)) {
                    signalStrength = WifiManager.calculateSignalLevel(result.level, 100);
                    break;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return String.valueOf(signalStrength);
    }

    public boolean isLocationDisabled() {
        int locationMode;

        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return true;
        }

        return locationMode == Settings.Secure.LOCATION_MODE_OFF;
    }

    @SuppressLint("MissingPermission")
    private String getDeviceLocation() {
        String result = DEFAULT_LOCATION;

        if (isLocationDisabled()) {
            // trigger callback back to device
            locationSettingsListener.onLocationDisabled();
            Log.e("TAG", "location turned off");
            return result;
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location != null) {
            result = String.format("%s,%s", location.getLatitude(), location.getLongitude());
            Log.e("Network Location", result);

        } else {


            Log.e("TAG", "network provider location null, attempt gps");
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                result = String.format("%s,%s", location.getLatitude(), location.getLongitude());
                Log.e("GPS Location", result);

            } else {

                Log.e("TAG", "GPS provider location null, attempting passive");
                location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                if (location != null) {
                    result = String.format("%s,%s", location.getLatitude(), location.getLongitude());
                    Log.e("Passive Location", result);
                }
            }
        }

        Log.e("TAG", "result => " + result);
        return result;
    }

    public void collectHeartbeatData() throws Exception {

        if (isLocationDisabled()) {
            // trigger callback back to device
            locationSettingsListener.onLocationDisabled();
            Log.e("TAG", "location turned off");
        }

        // get from controller
        configParams = HeartbeatController.getInstance().getConfigParams();
        additionalParams = HeartbeatController.getInstance().getAdditionalParams();

        // config params
        payload = new Payload();
        payload.appId = configParams.getAppId();
        payload.appName = configParams.getAppName();
        payload.appVer = configParams.getAppVer();
        payload.deviceId = configParams.getDeviceId();
        payload.apiVersion = configParams.getApiVersion(); // apiVersion

        // heartbeat params
        heartbeat = new Heartbeat();
        heartbeat.ip_address = getIpAddress();
        heartbeat.network_type = getNetworkType();
        heartbeat.network_signal_strength = getSignalStrength();
        heartbeat.battery_life = getBatteryLevel();
        heartbeat.gps_address = isLocationDisabled() ? DEFAULT_LOCATION : currentLocation; /*getDeviceLocation();*/
        heartbeat.heartbeat_version = BuildConfig.VERSION_NAME;
    }

    public void sendMessageHandler() {

        if (webSocket == null) {
            webSocket = HeartbeatController.getInstance().getWebSocket();
        }

        if (webSocket.isOpen()) {
            // send message to the websocket controller
            sendMessage();
        } else {
            isAwaitingReconnection = true;

            if (webSocket.getState() == WebSocketState.CREATED) {
                webSocket.connectAsynchronously();
                Log.e("TAG", "websocket not open, connecting asynchronously");
                return;
            }

            // reconnect
            webSocket = HeartbeatController.getInstance().reconnectWebSocket();
            Log.e("TAG", "websocket not open, initializing reconnect");
        }
    }

    private void sendMessage() {
        Log.e("Tag", "attempting send message");

        try {
            byte[] message = buildHeartbeatBinary();
            if (message != null) {
                Log.e("TAG", new String(message));
                webSocket.sendBinary(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is for picking additional data params from persistence. It is
     * necessary because, when the POS restarts and we loose some data
     */
    private void reconcileAdditionalData() {

        try {

            // get persisted object
            String persistedPayload = HeartbeatController.getInstance().getLastPayload();

            // if empty do nothing
            if (persistedPayload.isEmpty()) {
                Log.e("TAG", "No persisted payload found");
                return;
            }

            Log.e("TAG", "persisted payload => " + persistedPayload);
            JSONObject persistedHeartbeat = new JSONObject(persistedPayload).optJSONObject("hb");

            // check if we have a valid persisted payload
            if (persistedHeartbeat == null){
                Log.e("TAG", "persisted payload has no \"hb\" object hence exiting reconciliation");
                return;
            }

            // check if we are dealing with the same merchant
            String persistedMerchantId = persistedHeartbeat.optString("ms_id", "");
            String currentMerchantId = additionalParams.get("ms_id") == null ? "" : additionalParams.get("ms_id");

            // if current merchant id is empty, it means the POS is not logged in yet.
            if (!currentMerchantId.isEmpty() && currentMerchantId.equalsIgnoreCase(persistedMerchantId)) {

                // proceed with reconciliation
                for (String key : additionalParams.keySet()) {
                    String value = additionalParams.get(key);

                    // do not reconcile error
                    if (key.equalsIgnoreCase("err")){
                        continue;
                    }

                    // if current payload additional data has values that are empty
                    if (value.isEmpty()) {

                        // check if the persisted heartbeat has the value we want
                        String persistedValue = persistedHeartbeat.optString(key, "");

                        // replace the current values with values from persistence if they exist
                        if (!persistedValue.isEmpty()){
                            additionalParams.put(key, persistedValue);
                            Log.e("Heartbeat", "reconciling " + key);
                        }
                    }
                }
            } else {
                // different merchant. Do no not reconcile
                Log.e("Heartbeat", "No reconcile necessary, User not logged in or different merchant detected");
            }

        } catch (Exception e) {
            Log.e("TAG", "reconcile failed");
            e.printStackTrace();
        }
    }

    private JSONObject mergeHeartbeatAndAdditionalParams() {

        JSONObject result = new JSONObject();

        if (heartbeat == null) {
            Log.e("TAG", "heartbeat is null");
            return result;
        }

        // convert heartbeat object into json
        Gson gsonBuilder = new GsonBuilder().create();
        String jsonStr = gsonBuilder.toJson(heartbeat);


        // reconcile additional data first
        reconcileAdditionalData();

        try {
            JSONObject object = new JSONObject(jsonStr);
            for (String key : additionalParams.keySet()) {
                object.put(key, additionalParams.get(key));
            }

            // add a timestamp
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            object.put("ts", date);

            result = object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getPayloadAsString() {

        if (payload == null) {
            Log.e("TAG", "Payload is null");
            return "";
        }

        String message = "";
        try {
            Gson gsonBuilder = new GsonBuilder().create();
            message = gsonBuilder.toJson(payload);

            // add heartbeat to json
            JSONObject o = new JSONObject(message);
            o.put("hb", mergeHeartbeatAndAdditionalParams());


            message = o.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return message;
    }

    private byte[] buildHeartbeatBinary() {
        try {
            // determine whether one-off connection or subscription
            byte[] buffer1 = new byte[1];
            buffer1[0] = 0; // change to 1 if more messages will coming through
            //buffer1[1] = (subscribe ? (byte) '1' : (byte) '0');

            // convert object into json
            String message = getPayloadAsString();

            if (message.isEmpty()) {
                throw new Exception("getPayloadAsString returned an empty string");
            }

            HeartbeatController.getInstance().saveLastPayload(message);

            // get message in bytes
            byte[] buffer2 = message.getBytes();

            // object to compile message
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byteArrayOutputStream.write(buffer1); // defines mode of transfer
            byteArrayOutputStream.write(buffer2); // defines the message to be sent

            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void startService() {

        // listen on telephony service
        telephonyManager.listen(cellularStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);

        // start broadcast receiver
        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryLevelBroadcastReceiver = new BatteryLevelBroadcastReceiver(this);
        context.registerReceiver(batteryLevelBroadcastReceiver, batteryIntentFilter);

        // useWorkManager()
        initAlarmManager();
    }

    private void useWorkManager() {
        // set up constraints
//        Constraints constraints = new Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .setRequiresBatteryNotLow(false)
////                .setRequiresCharging(false)
////                .setRequiresStorageNotLow(false)
//                .build();

//        PeriodicWorkRequest heartbeatWork  = new PeriodicWorkRequest
//                .Builder(HeartbeatService.class, 15, TimeUnit.MINUTES)
////                .setInitialDelay(5, TimeUnit.SECONDS)
////                .setConstraints(constraints)
//                .build();
//
//        WorkManager.getInstance()
//                .enqueueUniquePeriodicWork("HEARTBEAT", ExistingPeriodicWorkPolicy.KEEP, heartbeatWork);

    }

    private void initAlarmManager() {

        // default interval is 5 minutes
        Log.e("TAG", "trigger time = " + configParams.getTriggerIntervalMillis());
        int intervalMillis = configParams.getTriggerIntervalMillis() == 0 ? HEARTBEAT_INTERVAL_MILLIS : configParams.getTriggerIntervalMillis();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMillis, pendingIntent);


        try {
            Log.e("TAG", "attempt immediate send");
            HeartbeatController.getInstance().sendImmediately();
        } catch (Exception e){
            Log.e("TAG", "could not send initial heartbeat");
            e.printStackTrace();
        }
    }

    public void destroyService() {
        if (telephonyManager != null) {
            telephonyManager.listen(cellularStateListener, PhoneStateListener.LISTEN_NONE);
        }
        context.unregisterReceiver(batteryLevelBroadcastReceiver);
        additionalParams.clear();
        locationManager.removeUpdates(locationListener);


        // WorkManager.getInstance().cancelAllWork();
    }

    // signal strength being returned is cellular
    @Override
    public void onSignalStrengthChanged(int value) {
        signalStrength = value;
    }

    @Override
    public void onBatteryLevelChanged(int level) {
        batteryLevel = ((double) level / 100);
    }

    @SuppressLint("MissingPermission")
    private void listenForLocationUpdates() {

        if (locationManager == null) {
            Log.e("TAG", "location manager null, recreating");
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_INTERVAL_MILLIS,
                LOCATION_DISTANCE,
                locationListener
        );
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                currentLocation = String.format("%s,%s", location.getLatitude(), location.getLongitude());
                Log.e("Location Update", "location = " + currentLocation);
            } else {
                Log.e("Location Update", "returning null");
            }
        }

        @Override
        public void onStatusChanged(String provider, int i, Bundle bundle) {

            String state = "";
            switch (i) {
                case 0 :
                    state = "OUT OF SERVICE";
                    break;

                case 1:
                    state = "TEMPORARILY UNAVAILABLE";
                    break;

                case 2:
                    state = "AVAILABLE";
                    break;
            }

            Log.e("TAG", "onStatusChanged: " + provider + "current state: " + state);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e("TAG", "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e("TAG", "onProviderDisabled: " + provider);
        }
    };
}
