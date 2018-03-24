package com.blueskylinks.fan_control;

import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;

import static android.app.PendingIntent.getActivity;

public class SettingActivity extends AppCompatActivity {

    String s2;
    Switch simpleswitch1;
    Switch simpleswitch2;
    Switch simpleswitch3;
    boolean value1 = true;
    boolean value2 = true;
    boolean value3 = true;
    String checked;
    String unchecked;
    SharedPreferences sharedPreferences1;
    SharedPreferences sharedPreferences2;
    SharedPreferences sharedPreferences3;
    public Bgscanning mBoundService=new Bgscanning();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        simpleswitch1=(Switch)findViewById(R.id.switch1);
        simpleswitch2=(Switch)findViewById(R.id.switch2);
        simpleswitch3=(Switch)findViewById(R.id.switch3);
        final Intent i=new Intent("Broadcast");

        sharedPreferences1 = getSharedPreferences("isChecked1", 0);
        value1 = sharedPreferences1.getBoolean("isChecked1", value1); // retrieve the value of your key

        sharedPreferences2 = getSharedPreferences("isChecked", 0);
        value2 = sharedPreferences2.getBoolean("isChecked", value2); // retrieve the value of your key

        sharedPreferences3= getSharedPreferences("isChecked2", 0);
        value3 = sharedPreferences3.getBoolean("isChecked2", value3);

        simpleswitch1.setChecked(value1);
        simpleswitch1.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) { isChecked=true; /*ma.start()*/;sharedPreferences1.edit().putBoolean("isChecked1", true).apply(); }
                        else {sharedPreferences1.edit().putBoolean("isChecked1", false).apply();}
                    }
                }
        );
        simpleswitch2.setChecked(value2);
        simpleswitch2.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) { isChecked=true; mqtt_sub();sharedPreferences2.edit().putBoolean("isChecked", true).apply();}
                        else sharedPreferences2.edit().putBoolean("isChecked", false).apply();
                    }
                }
        );
        //registerReceiver(broadcastReceiver, new IntentFilter("Broadcast"));
        simpleswitch3.setChecked(value3);
        simpleswitch3.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) { isChecked=true;sharedPreferences3.edit().putBoolean("isChecked2", true).apply();
                         i.putExtra("d2",isChecked);
                            sendBroadcast(i);
                        }
                        else {sharedPreferences3.edit().putBoolean("isChecked2", false).apply();
                            i.putExtra("d2",isChecked);
                            sendBroadcast(i);}
                    }
                }
        );

    }


//connect to internet button on
    public void mqtt_sub()  {
        MqttCallback mqtt_callback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

            }
            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                //t1.setText(mqttMessage.toString());
                Intent intent = new Intent("Broadcast");
                intent.setAction("CUSTOM_INTENT");
                intent.putExtra("D1", mqttMessage.toString());
                sendBroadcast(intent);

                String s1= mqttMessage.toString();

                Log.i(s,mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {


            }
        };

        String broker       = "tcp://13.126.9.228:1883";
        String clientId     = "4";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            Log.i("Connecting to broker: ", broker);
            sampleClient.connect(connOpts);
            Log.i("Connected", "C");
            sampleClient.setCallback(mqtt_callback);
            sampleClient.subscribe("home");
            Log.i("...","Subscribing on topic");


        } catch(MqttException me) {
            Log.i("reason ",String.valueOf(me.getReasonCode()));
            Log.i("msg ",String.valueOf(me.getMessage()));
            Log.i("loc ",String.valueOf(me.getLocalizedMessage()));
            Log.i("cause ",String.valueOf(me.getCause()));
            Log.i("excep ",String.valueOf(me));
            me.printStackTrace();
        }

    }

    BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
         //   b1=findViewById(R.id.button2);
            //Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
            String s1 = arg1.getStringExtra("D1");
            Log.i("BLE,,,,,,,",""+s1);
        }
           // TextView t1 = (TextView) findViewById(R.id.tv);
    };
    }






