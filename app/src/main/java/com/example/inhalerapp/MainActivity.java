package com.example.inhalerapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import androidx.core.content.ContextCompat;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import java.util.UUID;
import androidx.annotation.NonNull;
import android.widget.TextView;
import java.util.Arrays;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.content.Context;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private TextView connectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate", "onCreate.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permissions for BLE scanning in Android 12 and above
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
        connectionStatus = findViewById(R.id.connection_status);


    }

    private void startBleScan() {

        // Initialize Bluetooth manager and adapter
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        // Check if Bluetooth is available and on
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            // Handle the case where device does not support Bluetooth or it is not enabled
            connectionStatus.setText("Bluetooth not available, please turn on bluetooth");
            return;
        } else {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            connectionStatus.setText("Bluetooth available");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            Log.d("BLE", "Start scanning.");
            connectionStatus.setText("Bluetooth available");

            bluetoothLeScanner.startScan(new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    // Check for the specific service UUID
                    if (result.getScanRecord() != null &&
                            result.getScanRecord().getServiceUuids() != null &&
                            result.getScanRecord().getServiceUuids().contains(UUID.fromString("fd09f5b1-5ebe-4df9-b2ef-b6d778ece98c"))) {
                        // Update the connection status
                        runOnUiThread(() -> connectionStatus.setText("connected"));
                    }
                }
            });
        } else {
            Log.d("BLE", "Not start scanning.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                for(int i = 0; i < grantResults.length; i++)
                {
                    Log.d("Permission", "Permission is granted. " + grantResults[i]);

                }
                startBleScan();
            } else {
                Log.d("Permission", "Permission is denied.");
                runOnUiThread(() -> connectionStatus.setText("Bluetooth permission is denied."));
            }
        }
    }
}
