package com.blueskylinks.fan_control;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

/**
 * Created by omsai on 1/23/2018.
 */

public class MqttConnectionManagerService extends Service {

    private MqttAndroidClient client;
    private MqttConnectOptions options;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    MqttConnectOptions mqttConnectOptions;
    BluetoothLeScanner scanner;
    int ms[];
    byte sc1[];
    BluetoothDevice ble_device;
    ScanRecord scan_rec;
     MqttMessage Mmessage;

    @Nullable
    public void onCreate() {

        options = createMqttConnectOptions();
        client = createMqttAndroidClient();
        super.onCreate();

    }

    private MqttConnectOptions createMqttConnectOptions() {
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession( true );
      mqttConnectOptions.setKeepAliveInterval(10);
        return mqttConnectOptions;
    }

    private MqttAndroidClient createMqttAndroidClient() {
        // "Create new client"
        client = new MqttAndroidClient( this, "tcp://13.126.9.228:1883", "");
        return client;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (client.isConnected() == false) {

            this.connect();
            Log.i(":","needs to connect to client");
        }
        initialize();
        return START_STICKY;
    }


    /**
     * Connect to broker.
     */
    public void connect(final MqttAndroidClient client, MqttConnectOptions options ) {
        try {
            if ( client != null ) {
                IMqttToken token = client.connect( options );
                client.setTraceEnabled( true );
               // token.setActionCallback( actionCallback );
               // client.setCallback( mqttCallback );

               
            }
        } catch ( MqttException e ) {
            //handle e
            Log.i("connect: " + e.getMessage(), String.valueOf(e));
        } catch ( NullPointerException ne ) {
            Log.i("connect: " + ne.getMessage(), String.valueOf(ne));
        } catch ( Exception uknw ) {
            Log.i( "connect: " + uknw.getMessage(), String.valueOf(uknw));
        }
    }

    /**
     * Connect to broker.
     * If client is not null, it will be reused instead of creating new one
     */
    public void connect() {
        Log.i("Connected: " , String.valueOf(client));
        if ( client == null ) {
            client = createMqttAndroidClient();
        }
        this.connect( client, options );
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        Log.i("BleScanning:", "initilizing.......");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.i("BleScanning:", "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.i("BleScanning:", "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            mBluetoothAdapter.enable();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        startscand();
        return true;

    }


    //================  Start BLE Scanning  ===============
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startscand() {
        Log.i("BLE------", "Start Scanning");
        //final ParcelUuid UID_SERVICE =
        ParcelUuid.fromString("000000f1-0000-1000-8000-00805f9b34fb");
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        ScanFilter beaconFilter = new ScanFilter.Builder()
                //.setServiceUuid(UID_SERVICE)
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<>();
        filters.add(beaconFilter);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        scanner.startScan(filters, settings, mcallback);
    }



    // =============== BLE Callback =======================
    // This callback method will be automatically called every time the scanner get the device adv data
    public ScanCallback mcallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            int rssi;
            rssi = result.getRssi();
            scan_rec = result.getScanRecord();
            Toast.makeText(MqttConnectionManagerService.this,String.valueOf(rssi), Toast.LENGTH_SHORT).show();
            Log.i("..",String.valueOf(rssi));
            sc1=scan_rec.getManufacturerSpecificData(0);
            for(int i=0;i<=2;i++) {
                ms[i] = sc1[i];
            }
           if(ms[0]== 1) {
                      try {
                          Thread.sleep(10000);
                          pub_msg();
                          Log.i("..","BackGround Message sc1[0]=1");
                      } catch (InterruptedException e) {
                          e.printStackTrace();
                      } catch (MqttException e) {
                          e.printStackTrace();
                      }
           }
               else return;
            ble_device = result.getDevice();
        }


    };
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopscand() {
        Log.i("BLE-----", "Stop Scanning");
        scanner.stopScan(mcallback);
    }

    public void pub_msg() throws MqttException {
        String topic = "home";
        Mmessage = new MqttMessage();
        // int variable1=1;
        for(int i=0;i<=2;i++) {
            String jsonobj = String.valueOf(ms[i]);
            Mmessage.setPayload(jsonobj.getBytes());
            client.publish(topic, Mmessage);
        }

    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onDestroy() {
        stopscand();/*
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }*/
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }
}
