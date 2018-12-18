package com.example.urvish.rxbledemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName() + "Ble";
    private RxBleClient mRxBleClient;
    private Disposable scanSub;
    private boolean isScanning = false;
    private ArrayList<RxBleDevice> mRxBleDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRxBleDevices = new ArrayList<>();

        grantPermission();
        //startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        enableBluetooth();
        ((Button) findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScanning) {
                    stratDoWork();
                    isScanning = true;
                    ((Button) findViewById(R.id.button)).setText("Stop Scanning");
                } else {
                    isScanning = false;
                    stopScanning();
                    ((Button) findViewById(R.id.button)).setText("Start Scannig");
                    ((TextView) findViewById(R.id.textView)).setText("");
                }

            }
        });


    }

    private void stopScanning() {
        scanSub.dispose();
        for (RxBleDevice mDevice : mRxBleDevices) {
            Log.d(TAG, "stopScanning: " + mDevice.getName());
            //need to enter the device name to which you want to connect with.
            if (mDevice.getName() != null) if (mDevice.getName().equals("Urvish's pixel 2 XL")) {
                Log.d(TAG, "stopScanning: " + mDevice.getName());
                connectToDevice(mDevice);
            }
        }

    }

    private void grantPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
    }

    private void enableBluetooth() {
        Intent enbleBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            startActivityForResult(enbleBluetooth, 1);
        }
    }

    private void stratDoWork() {
        mRxBleClient = RxBleClient.create(this);
        scanSub = mRxBleClient.scanBleDevices(new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), new ScanFilter.Builder().build()).subscribe(rxBleScanResult -> {
            Log.d(TAG, "onCreate: " + rxBleScanResult.toString());
            Log.d(TAG, "stratDoWork: " + rxBleScanResult.getBleDevice());
            //need to enter the name of device you want to connect with.
            if (rxBleScanResult.getBleDevice().getName() != null && rxBleScanResult.getBleDevice().getName().equals("Urvish's pixel 2 XL")) {
                ((TextView) findViewById(R.id.textView)).setText("\n Devices available press stop button to connect to device");
            }
            if (!mRxBleDevices.contains(rxBleScanResult.getBleDevice())) {
                mRxBleDevices.add(rxBleScanResult.getBleDevice());
                Log.d(TAG, "stratDoWork: " + mRxBleDevices.size());
                Log.d(TAG, "stratDoWork: " + mRxBleDevices);
            }
        }, throwable -> {
            Log.e(TAG, "onCreate: ", throwable);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        scanSub.dispose();
    }

    private void connectToDevice(RxBleDevice rxBleDevice) {
        Disposable disposable = rxBleDevice.establishConnection(false).subscribe(bleConncetion -> {
            bleConncetion.discoverServices().subscribe(rxBleDeviceServices -> {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       ((TextView) findViewById(R.id.textView)).setText("\n Connected to Device: " + rxBleDevice.getName());
                   }
               });

                for (BluetoothGattService service : rxBleDeviceServices.getBluetoothGattServices()) {
                    Log.d(TAG, "connectToDevice: " + service.getUuid());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) findViewById(R.id.textView)).append("\nUUID for service:" + service.getUuid());
                        }
                    });

                }
            }, throwable -> {
                Log.e(TAG, "connectToDevice: ", throwable);
            });

        }, throwable -> {
            Log.e(TAG, "connectToDevice: ", throwable);
        });
    }
}
