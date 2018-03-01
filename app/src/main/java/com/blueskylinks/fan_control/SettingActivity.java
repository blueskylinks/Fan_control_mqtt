package com.blueskylinks.fan_control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

//connect to internet button on
    public void mqtt_sub(View v1)  {
        MqttCallback mqtt_callback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

            }
            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                //t1.setText(mqttMessage.toString());
                Intent intent = new Intent();
                intent.setAction("CUSTOM_INTENT");
                intent.putExtra("D1", mqttMessage.toString());
                intent.putExtra("D2", mqttMessage.toString());
                intent.putExtra("D3", mqttMessage.toString());
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


        } catch(MqttException me) {
            Log.i("reason ",String.valueOf(me.getReasonCode()));
            Log.i("msg ",String.valueOf(me.getMessage()));
            Log.i("loc ",String.valueOf(me.getLocalizedMessage()));
            Log.i("cause ",String.valueOf(me.getCause()));
            Log.i("excep ",String.valueOf(me));
            me.printStackTrace();
        }

    }

    public class MyReceiver1 extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
         //   b1=findViewById(R.id.button2);
            //Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
            String s1 = arg1.getStringExtra("D1");
            String s2 = arg1.getStringExtra("D2");
            String s3 = arg1.getStringExtra("D3");
            Log.i("BLE,,,,,,,",s1);
            Log.i("BLE,,,,,,,",s2);
            Log.i("BLE,,,,,,,",s3);
        }
           // TextView t1 = (TextView) findViewById(R.id.tv);

    }
}

