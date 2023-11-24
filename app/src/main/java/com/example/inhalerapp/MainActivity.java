package com.example.inhalerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;

import android.widget.TextView;

import java.util.Arrays;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.content.Context;
import android.bluetooth.le.ScanRecord;
import android.os.ParcelUuid;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean isScanning = false;
    BluetoothDevice device;
    private TextView connectionStatus;
    private TextView rBLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate", "onCreate.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permissions for BLE scanning in Android 12 and above
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
        connectionStatus = findViewById(R.id.connection_status);
        rBLE = findViewById(R.id.rBLE);

    }

    private void startBleScan() {
        Log.d("startBleScan", "startBleScan");
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
//            Log.d("BLE", "Start scanning.");

            if (!isScanning) {
                connectionStatus.setText("Scanning...");
                isScanning = true;

                Log.d("Start Scanning", "Start Scanning");
                bluetoothLeScanner.startScan(new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
//                    Log.d("BLE", "onScanResult");
                        super.onScanResult(callbackType, result);
                        ScanRecord scanRecord = result.getScanRecord();
                        // Check for the specific service UUID
//                    if (scanRecord != null &&
//                            scanRecord.getServiceUuids() != null &&
//                            scanRecord.getServiceUuids().contains(UUID.fromString("fd09f5b1-5ebe-4df9-b2ef-b6d778ece98c"))) {
//                        // Update the connection status
//                        runOnUiThread(() -> connectionStatus.setText("connected"));
//                    }


                        if (scanRecord != null) {
                            // Get Service UUIDs
                            List<ParcelUuid> serviceUuids = scanRecord.getServiceUuids();
                            if (serviceUuids != null && !serviceUuids.isEmpty()) {
                                // Print all Service UUIDs
                                for (ParcelUuid uuid : serviceUuids) {
                                    Log.d("BLE Service", "Scanned UUID: " + uuid.toString());
                                    if (uuid.toString().equals("fd09f5b1-5ebe-4df9-b2ef-b6d778ece98c")) {
                                        Log.d("BLE Service", "Find target BLE Service!");
                                        BluetoothDevice device = result.getDevice();
                                        connectToDevice(device);
                                        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                                            // TODO: Consider calling
                                            //    ActivityCompat#requestPermissions
                                            // here to request the missing permissions, and then overriding
                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            //                                          int[] grantResults)
                                            // to handle the case where the user grants the permission. See the documentation
                                            // for ActivityCompat#requestPermissions for more details.

                                        }
                                        bluetoothLeScanner.stopScan(this);
                                        Log.d("Stop Scan", "Stop Scan");
                                        isScanning = false;
                                    }
                                }
                            }
                        } else {
                            Log.d("BLE", "ScanRecord is null.");
                        }

//                        device = result.getDevice();
//                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
//                            Log.d("Stop Scanning", "Stop Scanning");
//                            bluetoothLeScanner.stopScan(this);
//                            isScanning = false;
//                        }
//                        connectToDevice(device);
                    }
                });
            }

        } else {
            Log.d("BLE", "Not start scanning.");
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
//            Log.d("connectToDevice", "connectToDevice");
            BluetoothGatt bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    Log.d("onConnectionStateChange", "onConnectionStateChange");
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.d("1", "1");
                        // Connected successfully to the device
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                            Log.d("discoverServices", "discoverServices");
                            gatt.discoverServices();
//                            Log.d("discoverServices", "discoverServices");
                        }
                        runOnUiThread(() -> connectionStatus.setText("Connected to device!"));
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                    Log.d("onServicesDiscovered", "onServicesDiscovered");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        List<BluetoothGattService> services = gatt.getServices();
                        // Service discovery is successful and characteristics can be read
                        Log.d("GATT_SUCCESS", "GATT_SUCCESS");



                             for(BluetoothGattService service : services){
                                Log.d("UUID", service.getUuid().toString());
                                if (service.getUuid().toString().equals("fd09f5b1-5ebe-4df9-b2ef-b6d778ece98c"))
                                {
                                    Log.d("UUID", "equals");

                                    BluetoothGattCharacteristic rBLE = service.getCharacteristic(UUID.fromString("69ef4849-ed83-4665-9fe0-852f3fc9f330"));
                                    BluetoothGattCharacteristic gBLE = service.getCharacteristic(UUID.fromString("1a7a4154-bf0b-40a5-820e-0307aaf259b7"));
                                    BluetoothGattCharacteristic bBLE = service.getCharacteristic(UUID.fromString("a5807b3f-8de8-4916-aa32-b7d4f82cd7d6"));

                                    Log.d("GATT CHARs", "" + rBLE + " " + gBLE + " " + bBLE);
                                    if (rBLE != null) {
                                        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                            // TODO: Consider calling
                                            //    ActivityCompat#requestPermissions
                                            // here to request the missing permissions, and then overriding
                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            //                                          int[] grantResults)
                                            // to handle the case where the user grants the permission. See the documentation
                                            // for ActivityCompat#requestPermissions for more details.
                                        }
                                        gatt.readCharacteristic(rBLE);
                                    }
                                    if (gBLE != null) {
                                        Log.d("gBLE", gBLE.toString());
                                    }
                                    if (bBLE != null) {
                                        Log.d("bBLE", bBLE.toString());
                                    }


                                }
                            }

                    }



                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Log.d("onCharacteristicRead", "onCharacteristicRead");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        // Read characteristic value
                        int intValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                        Log.d("SUCC", "Read value: " + intValue);

                        /*final byte[] data = characteristic.getValue();

                        final String rBLEValue = new String(data, StandardCharsets.UTF_8);

                        // Detect medication dumping
                        int counterAll = 7;
                        int counterSatisfied = 0;
                        if (Integer.parseInt(rBLEValue) >= 3 && Integer.parseInt(rBLEValue) <= 12) {
                            counterSatisfied++;
                        }
//                        if (Integer.parseInt(gBLEValue) >= 1 && Integer.parseInt(gBLEValue) <= 5) {
//                            counterSatisfied++;
//                        }

                        if ((float) counterSatisfied / (float) counterAll > 0.5) {
                            Log.d("Warning", "Medication dumping!");
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                rBLE.setText(rBLEValue);
                            }

                        });*/
                    }
                }
            });
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
