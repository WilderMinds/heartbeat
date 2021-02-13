package com.expresspay.heatbeatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.expresspay.heartbeat.callbacks.LocationSettingsListener;
import com.expresspay.heartbeat.models.ConfigParams;
import com.expresspay.heartbeat.HeartbeatConfig;
import com.expresspay.heartbeat.controller.HeartbeatController;
import com.expresspay.heartbeat.receivers.SocketListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationSettingsListener {

    private static final int LOCATION_PERMISSION_REQUEST = 195;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions((Activity) this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            }, LOCATION_PERMISSION_REQUEST);

            Log.e("TAG", "no permission granted");

        } else {
            Log.e("TAG", "permission granted, we move");
            initHeartbeat();
        }


        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map<String, String> map = new HashMap<>();
                map.put("ms_id", "");
                map.put("ms_name", "");
                map.put("card_ts", "");
                map.put("mm_ts", "");

                HeartbeatController.getInstance().setAdditionalParams(map);
            }
        });

    }

    private Map<String, String> initAdditionalData(){

        Map<String, String> map = new HashMap<>();
        map.put("ms_id", "0123456789");
        map.put("ms_name", "Network Test Emulator");
        map.put("card_ts", "");
        map.put("mm_ts", "");
        map.put("err", "");
        map.put("err_ts", "");

        return map;
    }

    private ConfigParams initConfigParams() {
        String url = "wss://your.domain.com/wsapp/hb/sub/";
        ConfigParams configParams = new ConfigParams();

        configParams.setSocketUrl(url);
        configParams.setDeviceId("0000001234567890");
        configParams.setTriggerIntervalMillis(2 * 60 * 1000);
        configParams.setPersistSocketConnection(false);

        configParams.setAppId("POS-ANDROID");
        configParams.setAppName("APP_NAME");
        configParams.setAppVer("1.0.2.1");
        configParams.setApiVersion("4.0");

        return configParams;
    }

    private void initHeartbeat() {

        // set up merchant dependent params
        ConfigParams configParams = initConfigParams();

        // get configs values
        Map<String, String> additionalParams = initAdditionalData();

        HeartbeatController.getInstance().setConfigParams(configParams);
        HeartbeatController.getInstance().setAdditionalParams(additionalParams);

        // start listening to socket messages
//        HeartbeatController.getInstance().getSocketListener()
//                .setTransactionListener(new SocketListener.TransactionListener() {
//                    @Override
//                    public void onTextReceived(String text) {
//                        // do something with commands received here
//                    }
//
//                    @Override
//                    public void onDisconnected() {}
//
//                    @Override
//                    public void onMessageSent() {}
//
//                    @Override
//                    public void onConnected() {}
//                });

        // start service
        try {
            HeartbeatController.getInstance().startService(this, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        HeartbeatController.getInstance().destroyService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            boolean allGranted = true;

            // if either is not granted
            for (int result : grantResults) {
                getPackageManager();
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // we good
                initHeartbeat();
            } else {

            }
        }
    }

    @Override
    public void onLocationDisabled() {
        Toast.makeText(this, "User location is disabled, Logging you out", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationEnabled() {

    }
}