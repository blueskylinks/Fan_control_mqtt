package com.blueskylinks.fan_control;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    byte sc1[];
    int st=-1;
    Button button;
    Button b1;
    int count=0;
    int lr[]=new int[6];
    private BluetoothGatt mGatt;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner scanner;
    private BluetoothDevice ble_device;
    ScanRecord scan_rec;
    String message;
    String message1;
    String message2;
    String message3;
    String message4;
    String message5;
    String message6;
    public static String nrf_service = "000000f1-0000-1000-8000-00805f9b34fb";
    public final static UUID NRF_UUID_SERVICE = UUID.fromString(nrf_service);

    public BluetoothGattCharacteristic characteristicTX;

    public static String nrf_tx = "0000f102-0000-1000-8000-00805f9b34fb";
    public final static UUID NRF_UUID_TX = UUID.fromString(nrf_tx);

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
              ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},200);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        initialize();
    }

    @Override
    protected void onResume(){
        super.onResume();
        startscand();
    }

    @Override
    protected void onPause() {
        super.onPause();
        button  =findViewById(R.id.button8);
        String buttontext= (String) button.getText();
        if (buttontext=="Connected") disconnect();
        else return;
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
        return true;
    }

    //================  Start BLE Scanning  ===============
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startscand() {
        Log.i("BLE------", "Start Scanning");
        //final ParcelUuid UID_SERVICE =
        ParcelUuid.fromString("000000f1-0000-1000-8000-00805f9b34fb");
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        ScanFilter beaconFilter = new ScanFilter.Builder() // this filter will be used to get only specific device based on service UUID
                //.setServiceUuid(UID_SERVICE)
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
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
            Log.i("Scan result",String.valueOf(rssi));
            Log.i("record",scan_rec.toString());

            sc1=scan_rec.getManufacturerSpecificData(0);
            for (int i=0;i<sc1.length; i++){
              //  Log.i("Data-----:", String.valueOf(sc1[i]));
                lr[i]=sc1[i];
            }
            ble_device = result.getDevice();
            if(st!=lr[2]){
                 fan_control_set();
            }
            st=lr[2];
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopscand() {
        Log.i("BLE-----", "Stop Scanning");
        scanner.stopScan(mcallback);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void connect_device(View view) {
        button  =findViewById(R.id.button8);
        stopscand();
        if (ble_device!=null){
            mGatt = ble_device.connectGatt(this, false, gattCallback);
            Log.i("BLE", "Device Connected...");
            button.setText("Connected");
        }
        else open();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
     mGatt.disconnect();
    }


    //===========================================================================
    public BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + newState);

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i("gattCallback", "STATE_DISCONNECTED");
                    Intent intent = new Intent();
                    intent.setAction("CUSTOM_INTENT");
                    intent.putExtra("D1", "STATE_DISCONNECTED");
                    sendBroadcast(intent);
                    break;
                default:
                    Log.i("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            BluetoothGattService service1;
            Log.i("onServicesDiscovered", services.toString());
            service1=gatt.getService(NRF_UUID_SERVICE);
            characteristicTX = services.get(2).getCharacteristic(NRF_UUID_TX);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            final byte b1[]=characteristic.getValue();
            for (int i=0;i<b1.length;i++){
                Log.i(":",String.valueOf(b1[i]));
            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicWrite??", characteristic.toString());
            final byte b1[]=characteristic.getValue();
            for (int i=0;i<b1.length;i++){
                Log.i(":",String.valueOf(b1[i]));
            }
        }

    };


    public void fan_control_up(View view) {
        button = findViewById(R.id.button8);
        String buttontext = (String) button.getText();
        if (buttontext == "Connected") {
            message = "x";
            characteristicTX.setValue(message);
            mGatt.writeCharacteristic(characteristicTX);
            Button id[] = new Button[6];
            id[0] = findViewById(R.id.id1);
            id[1] = findViewById(R.id.id2);
            id[2] = findViewById(R.id.id3);
            id[3] = findViewById(R.id.id4);
            id[4] = findViewById(R.id.id5);
            id[5] = findViewById(R.id.id6);
            final TextView textView = findViewById(R.id.textView4);
            if (count < 6) {
                for (int i = 0; i <= count; i++) {
                    if (count == i) {
                        id[i].getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                        textView.setText(String.valueOf(count+1));
                    }
                }
                count = count + 1;
            } else  return;
        } else  open();

    }


    public void fan_control_down(View view){
        button  =findViewById(R.id.button8);
        String buttontext= (String) button.getText();
        if (buttontext=="Connected") {
            message = "y";
            characteristicTX.setValue(message);
            mGatt.writeCharacteristic(characteristicTX);
            Button id[] = new Button[6];
            id[0] = findViewById(R.id.id1);
            id[1] = findViewById(R.id.id2);
            id[2] = findViewById(R.id.id3);
            id[3] = findViewById(R.id.id4);
            id[4] = findViewById(R.id.id5);
            id[5] = findViewById(R.id.id6);
            final TextView textView = findViewById(R.id.textView4);
            if (count >= 0) {
                count = count - 1;
                for (int i = 0; i <= count; i++) {
                    if (count == i) {
                        id[i].getBackground().clearColorFilter();
                        textView.setText(String.valueOf(count));
                    }
                }

            } else return;
        }
        else  open();

    }

    public void fan_control_set(){
        Button id[] = new Button[6];
        b1 = findViewById(R.id.button1);
        id[0] = findViewById(R.id.id1);
        id[1] = findViewById(R.id.id2);
        id[2] = findViewById(R.id.id3);
        id[3] = findViewById(R.id.id4);
        id[4] = findViewById(R.id.id5);
        id[5] = findViewById(R.id.id6);
        final TextView textView = findViewById(R.id.textView4);
        count=lr[2];
        if (count > 0) {
           for(int j=0;j<count;j++){
               id[j].getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
               textView.setText(String.valueOf(count));
           }

        } else return;
        if (sc1[0]==1)
        {
            message1="a";
            b1.setBackgroundColor(Color.TRANSPARENT);
        }
        else if(sc1[0]==0)  {message1="A";  b1.setBackgroundColor(Color.RED);}

    }


    public void change_color1(View view) {
        button = findViewById(R.id.button8);
        String buttontext = (String) button.getText();
        if (buttontext == "Connected") {
            b1 = findViewById(R.id.button1);
            if (message1=="a") {
                message1 = "A";
                characteristicTX.setValue(message1);
                mGatt.writeCharacteristic(characteristicTX);
                b1.setBackgroundColor(Color.RED);
            }
            else {
                message1 = "a";
                characteristicTX.setValue(message1);
                mGatt.writeCharacteristic(characteristicTX);
                b1.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        else open();
   }

    public void change_color2(View view){
        button  =findViewById(R.id.button8);
        String buttontext= (String) button.getText();
        if (buttontext=="Connected") {
            b1 = findViewById(R.id.button2);
            if (message2 == "b") {
                b1.setBackgroundColor(Color.RED);
                message2 = "B";
                characteristicTX.setValue(message2);
                mGatt.writeCharacteristic(characteristicTX);
            } else {
                b1.setBackgroundColor(Color.TRANSPARENT);
                message2 = "b";
                characteristicTX.setValue(message2);
                mGatt.writeCharacteristic(characteristicTX);
            }
        }
        else open();
    }

    public void change_color3(View view){
        button  =findViewById(R.id.button8);
        String buttontext= (String) button.getText();
        if (buttontext=="Connected") {
            b1 = findViewById(R.id.button3);
            if (message3 == "c") {
                b1.setBackgroundColor(Color.RED);
                message3 = "C";
                characteristicTX.setValue(message3);
                mGatt.writeCharacteristic(characteristicTX);
            } else {
                b1.setBackgroundColor(Color.TRANSPARENT);
                message3 = "c";
                characteristicTX.setValue(message3);
                mGatt.writeCharacteristic(characteristicTX);
            }
        }
        else open();
    }

    public void change_color4(View view){
        button  =findViewById(R.id.button8);
        String buttontext= (String) button.getText();
        if (buttontext=="Connected") {
            b1 = findViewById(R.id.button4);
            if (message4 == "d") {
                b1.setBackgroundColor(Color.RED);
                message4 = "D";
                characteristicTX.setValue(message4);
                mGatt.writeCharacteristic(characteristicTX);
            } else {
                b1.setBackgroundColor(Color.TRANSPARENT);
                message4 = "d";
                characteristicTX.setValue(message4);
                mGatt.writeCharacteristic(characteristicTX);
            }
        }
        else open();
    }

    public void change_color5(View view){
        button  =findViewById(R.id.button8);
        String buttontext= (String) button.getText();
        if (buttontext=="Connected") {
            b1 = findViewById(R.id.button5);
            if (message5 == "e") {
                b1.setBackgroundColor(Color.RED);
                message5 = "E";
                characteristicTX.setValue(message5);
                mGatt.writeCharacteristic(characteristicTX);

            } else {
                b1.setBackgroundColor(Color.TRANSPARENT);
                message5 = "e";
                characteristicTX.setValue(message5);
                mGatt.writeCharacteristic(characteristicTX);
            }
        }
        else open();
    }

    public void change_color6(View view){
        button  =findViewById(R.id.button8);
        String buttontext= (String) button.getText();
        if (buttontext=="Connected") {
            b1 = findViewById(R.id.button6);
            if (message6 == "f") {
                b1.setBackgroundColor(Color.RED);
                message6 = "F";
                characteristicTX.setValue(message6);
                mGatt.writeCharacteristic(characteristicTX);
            } else {
                b1.setBackgroundColor(Color.TRANSPARENT);
                message6 = "f";
                characteristicTX.setValue(message6);
                mGatt.writeCharacteristic(characteristicTX);
            }
        }
        else  open();
    }
    //Setting Button
    public void set_activity(View view){
        Intent it=new Intent(this,SettingActivity.class);
        startActivity(it);
        //Document this
    }

    //Dialog BOX
    public void open() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Device is not connected");
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        return;
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


}
