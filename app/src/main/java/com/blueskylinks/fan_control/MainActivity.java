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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.renderscript.ScriptGroup;
import android.renderscript.ScriptGroup.Binding;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.StaticLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public Bgscanning mBoundService=new Bgscanning();
    SharedPreferences sharedPreferences;
    Boolean mIsBound=false;
    int s4;
   static Button button;
   int red=Color.RED;
    int colorId;
    Button b1;
    static MainActivity instance = new MainActivity();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
              ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},200);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        sharedPreferences = getSharedPreferences("bound", MODE_PRIVATE);
      //mIsBound=sharedPreferences.getBoolean("bound",mIsBound);
        Log.i("mIsbound value",String.valueOf(mIsBound));
        if(!mIsBound) start();
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter("broadcastname"));
    }

    @Override
    public  void onPause(){
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void connect_device(View view) {
        button = findViewById(R.id.button8);
        String buttontext = (String) button.getText();
      if(buttontext=="connected") {mBoundService.disconnect();button.setText("Disconnected");}
       else {
          if (mIsBound) {
              BluetoothGatt Mgatt = mBoundService.connect();
              if (Mgatt != null) {button.setText("connected");}
          }
      }
    }

    public void change_color1(View view){
        button = findViewById(R.id.button8);
        String buttontext = (String) button.getText();
        b1=(Button)findViewById(R.id.button1);
        ColorDrawable buttonColor=(ColorDrawable)b1.getBackground();
        colorId = buttonColor.getColor();
        if (buttontext == "connected") {
           if(colorId ==  red){ mBoundService.change_color1();data_chage2();}
           else{ data_chage1(); mBoundService.change_color1();}
        }
        else open();
    }


    //Setting Button
    public void set_activity(View view){
        Intent it=new Intent(this,SettingActivity.class);
        startActivity(it);
    }

    // Alert message
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
//change in manufacture specific data changes button color
    public void data_chage1(){
         b1=findViewById(R.id.button1);
          b1.setBackgroundColor(Color.RED);
    }

    public void data_chage2(){
       b1=findViewById(R.id.button1);
        b1.setBackgroundColor(Color.TRANSPARENT);
    }


    public ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has
            // been established, giving us the service object we can use
            // to interact with the service.  Because we have bound to a
            // explicit service that we know is running in our own
            // process, we can cast its IBinder to a concrete class and
            // directly access it.
            mBoundService = ((Bgscanning.LocalBinder)service).getService();
            // Tell the user about this for our demo.
            Toast.makeText(mBoundService, "service connected", Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has
            // been unexpectedly disconnected -- that is, its process
            // crashed. Because it is running in our same process, we
            // should never see this happen.
            mBoundService = null;
            Toast.makeText(mBoundService, "service disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    public  void start() {
        bindService(new Intent(this, Bgscanning.class),
                mConnection,
                Context.BIND_AUTO_CREATE);
        mIsBound = true;
        sharedPreferences.edit().putBoolean("bound",mIsBound).apply();
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
         //   unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
        SharedPreferences preferences = this.getSharedPreferences("bound",MODE_PRIVATE);
        preferences.edit().remove("bound");
        preferences.edit().clear();
        preferences.edit().apply();
    }

    //Broadcast reciever
    BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            s4= arg1.getIntExtra("d1",0);
            Log.i("..",String.valueOf(s4));
           if(s4==1){
                data_chage1();
            }
            else data_chage2();
        }
    };
}
