package com.example.inhalerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattDescriptor;
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    private TextView gBLE;
    private TextView bBLE;
    private TextView luminanceBLE;
    private TextView rollBLE;
    private TextView pitchBLE;
    private TextView gxBLE;
    private TextView gyBLE;
    private TextView gzBLE;
    private TextView temperatureBLE;
    private TextView soundBLE;
    private TextView usageStatus;
    private TextView dumpingStatus;

    BluetoothGattCharacteristic rBLEChar;
    BluetoothGattCharacteristic gBLEChar;
    BluetoothGattCharacteristic bBLEChar;
    BluetoothGattCharacteristic luminanceBLEChar;
    BluetoothGattCharacteristic rollBLEChar;
    BluetoothGattCharacteristic pitchBLEChar;
    BluetoothGattCharacteristic gxBLEChar;
    BluetoothGattCharacteristic gyBLEChar;
    BluetoothGattCharacteristic gzBLEChar;
    BluetoothGattCharacteristic temperatureBLEChar;
    BluetoothGattCharacteristic soundBLEChar;
    private List<BluetoothGattCharacteristic> characteristicsToEnable;
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
        gBLE = findViewById(R.id.gBLE);
        bBLE = findViewById(R.id.bBLE);
        luminanceBLE = findViewById(R.id.luminanceBLE);
        rollBLE = findViewById(R.id.rollBLE);
        pitchBLE = findViewById(R.id.pitchBLE);
        gxBLE = findViewById(R.id.gxBLE);
        gyBLE = findViewById(R.id.gyBLE);
        gzBLE = findViewById(R.id.gzBLE);
        temperatureBLE = findViewById(R.id.temperatureBLE);
        soundBLE = findViewById(R.id.soundBLE);
        usageStatus = findViewById(R.id.usage);
        dumpingStatus = findViewById(R.id.dumping);

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
//                    Log.d("onConnectionStateChange", "onConnectionStateChange");
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
//                        Log.d("1", "1");
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
                        characteristicsToEnable = new ArrayList<>();

                        for (BluetoothGattService service : services) {
                            Log.d("UUID", service.getUuid().toString());
                            if (service.getUuid().toString().equals("fd09f5b1-5ebe-4df9-b2ef-b6d778ece98c")) {
                                Log.d("UUID", "equals");
                                runOnUiThread(() -> connectionStatus.setText("Reading characteristics!"));




                                rBLEChar = service.getCharacteristic(UUID.fromString("69ef4849-ed83-4665-9fe0-852f3fc9f330"));
                                gBLEChar = service.getCharacteristic(UUID.fromString("1a7a4154-bf0b-40a5-820e-0307aaf259b7"));
                                bBLEChar = service.getCharacteristic(UUID.fromString("a5807b3f-8de8-4916-aa32-b7d4f82cd7d6"));
                                luminanceBLEChar = service.getCharacteristic(UUID.fromString("1d3430e9-675a-4e8a-a2ce-2d9b3ca7edc2"));
                                rollBLEChar = service.getCharacteristic(UUID.fromString("355ade2a-3451-4455-bf04-436f3c70af2b"));
                                pitchBLEChar = service.getCharacteristic(UUID.fromString("6164171a-e232-407d-885f-e373cfc24554"));
                                gxBLEChar = service.getCharacteristic(UUID.fromString("7c4cca54-3033-490a-a2ac-cb4b8c82fc8b"));
                                gyBLEChar = service.getCharacteristic(UUID.fromString("ba581012-f1a4-4ecc-b226-5a5d0f8ab22b"));
                                gzBLEChar = service.getCharacteristic(UUID.fromString("7c2e28e8-830d-4e16-aa96-fc0f4bcbcc67"));
                                temperatureBLEChar = service.getCharacteristic(UUID.fromString("d8fb2c21-5808-4bd8-b178-a8c587de4286"));
                                soundBLEChar = service.getCharacteristic(UUID.fromString("125dd222-6a88-4f3f-bde8-4f428c54c4e0"));



                                characteristicsToEnable.add(rBLEChar);
                                characteristicsToEnable.add(gBLEChar);
                                characteristicsToEnable.add(bBLEChar);
                                characteristicsToEnable.add(luminanceBLEChar);
                                characteristicsToEnable.add(rollBLEChar);
                                characteristicsToEnable.add(pitchBLEChar);
                                characteristicsToEnable.add(gxBLEChar);
                                characteristicsToEnable.add(gyBLEChar);
                                characteristicsToEnable.add(gzBLEChar);
                                characteristicsToEnable.add(temperatureBLEChar);
                                characteristicsToEnable.add(soundBLEChar);

//                                enableNotifications(gatt, rBLEChar);
                                enableNextCharacteristicNotification(gatt);
/*
                                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                                    if (rBLE != null) {
//                                        byte[] value = rBLE.getValue();
//                                        if (value != null) {
//                                            String rBLEValue = new String(value, StandardCharsets.UTF_8);
//                                            Log.d("rBLE", rBLEValue);
//                                        }
                                        enableNotifications(gatt, rBLE);
                                        Log.d("rBLE", rBLE.toString());
                                    }

                                    if (gBLE != null) {
                                        enableNotifications(gatt, gBLE);
                                        Log.d("gBLE", gBLE.toString());
                                    }

                                    if (bBLE != null) {
                                        enableNotifications(gatt, bBLE);
                                        Log.d("bBLE", bBLE.toString());
                                    }

                                    if (luminanceBLE != null) {
                                        enableNotifications(gatt, luminanceBLE);
                                        Log.d("luminanceBLE", luminanceBLE.toString());
                                    }

//                                    Log.d("Characteristics", "" + rBLE + " " + gBLE + " " + bBLE + " " + luminanceBLE);
//                                    enableNotifications(gatt, rBLE);
//                                    enableNotifications(gatt, gBLE);
//                                    enableNotifications(gatt, bBLE);
//                                    enableNotifications(gatt, luminanceBLE);
//                                    Log.d("Characteristics", "" + rBLE + " " + gBLE + " " + bBLE + " " + luminanceBLE);

                                    if (rollBLE != null) {
                                        enableNotifications(gatt, rollBLE);
                                        Log.d("rollBLE", rollBLE.toString());
                                    }

                                    if (pitchBLE != null) {
                                        enableNotifications(gatt, pitchBLE);
                                        Log.d("pitchBLE", pitchBLE.toString());
                                    }

                                    if (gxBLE != null) {
                                        enableNotifications(gatt, gxBLE);
                                        Log.d("gxBLE", gxBLE.toString());
                                    }

                                    if (gyBLE != null) {
                                        enableNotifications(gatt, gyBLE);
                                        Log.d("gyBLE", gyBLE.toString());
                                    }

                                    if (gzBLE != null) {
                                        enableNotifications(gatt, gzBLE);
                                        Log.d("gzBLE", gzBLE.toString());
                                    }

                                    if (temperatureBLE != null) {
                                        enableNotifications(gatt, temperatureBLE);
                                        Log.d("temperatureBLE", temperatureBLE.toString());
                                    }

                                    if (soundBLE != null) {
                                        enableNotifications(gatt, soundBLE);
                                        Log.d("soundBLE", soundBLE.toString());
                                    }*/


                                // }

                            }
                        }
                    }
                }


                private void enableNextCharacteristicNotification(BluetoothGatt gatt) {
                    if (!characteristicsToEnable.isEmpty()) {
                        BluetoothGattCharacteristic characteristic = characteristicsToEnable.remove(0);
                        enableNotifications(gatt, characteristic);
                    }
                }

                private void enableNotifications(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                    }
                    gatt.setCharacteristicNotification(characteristic, true);

                    // Enable the notification descriptor
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
                /*

                private boolean enableNotifications(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
                    Log.d("enableNotifications", "enableNotifications");
                    final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    if (descriptor != null) {
                        Log.d("enableNotifications 2 ", "enableNotifications 2");

                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                        }
                        Log.d("enableNotifications 3", "enableNotifications 3");
                        gatt.setCharacteristicNotification(characteristic, true);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        return gatt.writeDescriptor(descriptor);
                    }
                    return false;
                }*/

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
                {
//                    Log.d("onCharacteristicChanged", "onCharacteristicChanged " + characteristic.getUuid());
//                    Log.d("RRR", String.valueOf(rBLEChar.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0)));

                    if (characteristic.getUuid().equals(UUID.fromString("69ef4849-ed83-4665-9fe0-852f3fc9f330"))) {
                        int characteristicValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                        Log.d("Characteristic Changed", "rBLE value: " + characteristicValue);
                        runOnUiThread(() -> rBLE.setText("red " + String.valueOf(characteristicValue)));
//                        if (characteristicValue > 3 && characteristicValue < 12) {
//                            runOnUiThread(() -> dumpingStatus.setText("No Dumping"));
//                        } else {
//                            runOnUiThread(() -> dumpingStatus.setText("Dumping!"));
//                        }
                    }

                    else if (characteristic.getUuid().equals(UUID.fromString("1a7a4154-bf0b-40a5-820e-0307aaf259b7"))) {
                        int characteristicValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                        Log.d("Characteristic Changed", "gBLE value: " + characteristicValue);
                        runOnUiThread(() -> gBLE.setText("green " + String.valueOf(characteristicValue)));
//                        if (characteristicValue > 1 && characteristicValue < 5) {
//                            runOnUiThread(() -> dumpingStatus.setText("No Dumping"));
//                        } else {
//                            runOnUiThread(() -> dumpingStatus.setText("Dumping!"));
//                        }
                    }

                    else if (characteristic.getUuid().equals(UUID.fromString("a5807b3f-8de8-4916-aa32-b7d4f82cd7d6"))) {
                        int characteristicValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                        Log.d("Characteristic Changed", "bBLE value: " + characteristicValue);
                        runOnUiThread(() -> bBLE.setText("blue " + String.valueOf(characteristicValue)));
//                        if (characteristicValue > 1 && characteristicValue < 5) {
//                            runOnUiThread(() -> dumpingStatus.setText("No Dumping"));
//                        } else {
//                            runOnUiThread(() -> dumpingStatus.setText("Dumping!"));
//                        }
                    }

//                    else if (characteristic.getUuid().equals(UUID.fromString("1d3430e9-675a-4e8a-a2ce-2d9b3ca7edc2"))) {
//                        int characteristicValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
//                        Log.d("Characteristic Changed", "luminanceBLE value: " + characteristicValue);
////                        runOnUiThread(() -> luminanceBLE.setText(characteristicValue));
//                    }

                    else if (characteristic.getUuid().equals(UUID.fromString("355ade2a-3451-4455-bf04-436f3c70af2b"))) {
                        byte[] byteValue = characteristic.getValue();
                        if (byteValue != null && byteValue.length >= 8) {
                            double characteristicValue = ByteBuffer.wrap(byteValue).order(ByteOrder.LITTLE_ENDIAN).getDouble();
                            Log.d("Characteristic Changed", "roll value: " + characteristicValue);
                            runOnUiThread(() -> rollBLE.setText("roll " + String.valueOf(characteristicValue)));
                        }
                    }

                    else if (characteristic.getUuid().equals(UUID.fromString("6164171a-e232-407d-885f-e373cfc24554"))) {
                        byte[] byteValue = characteristic.getValue();
                        if (byteValue != null && byteValue.length >= 8) {
                            double characteristicValue = ByteBuffer.wrap(byteValue).order(ByteOrder.LITTLE_ENDIAN).getDouble();
                            Log.d("Characteristic Changed", "pitch value: " + characteristicValue);
                            runOnUiThread(() -> pitchBLE.setText("pitch " + String.valueOf(characteristicValue)));
                        }
                    }

                    else if (characteristic.getUuid().equals(UUID.fromString("7c4cca54-3033-490a-a2ac-cb4b8c82fc8b"))) {
                        byte[] byteValue = characteristic.getValue();
                        if (byteValue != null && byteValue.length >= 4) {
                            float characteristicValue = ByteBuffer.wrap(byteValue).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            Log.d("Characteristic Changed", "gx value: " + characteristicValue);
                            runOnUiThread(() -> gxBLE.setText("gx " + String.valueOf(characteristicValue)));
                        }
                    }

                    else if (characteristic.getUuid().equals(UUID.fromString("ba581012-f1a4-4ecc-b226-5a5d0f8ab22b"))) {
                        byte[] byteValue = characteristic.getValue();
                        if (byteValue != null && byteValue.length >= 4) {
                            float characteristicValue = ByteBuffer.wrap(byteValue).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            Log.d("Characteristic Changed", "gy value: " + characteristicValue);
                            runOnUiThread(() -> gyBLE.setText("gy " + String.valueOf(characteristicValue)));
                        }
                    }

                    else if (characteristic.getUuid().equals(UUID.fromString("7c2e28e8-830d-4e16-aa96-fc0f4bcbcc67"))) {
                        byte[] byteValue = characteristic.getValue();
                        if (byteValue != null && byteValue.length >= 4) {
                            float characteristicValue = ByteBuffer.wrap(byteValue).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            Log.d("Characteristic Changed", "gz value: " + characteristicValue);
                            runOnUiThread(() -> gzBLE.setText("gz " + String.valueOf(characteristicValue)));
                        }
                    }

//                    else if (characteristic.getUuid().equals(UUID.fromString("d8fb2c21-5808-4bd8-b178-a8c587de4286"))) {
//                        byte[] byteValue = characteristic.getValue();
//                        if (byteValue != null && byteValue.length >= 4) {
//                            float characteristicValue = ByteBuffer.wrap(byteValue).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//                            Log.d("Characteristic Changed", "temperature value: " + characteristicValue);
//                            runOnUiThread(() -> temperatureBLE.setText("temperature " + String.valueOf(characteristicValue)));
//                        }
//                    }
//
//                    else if (characteristic.getUuid().equals(UUID.fromString("125dd222-6a88-4f3f-bde8-4f428c54c4e0"))) {
//                        byte[] byteValue = characteristic.getValue();
//                        if (byteValue != null && byteValue.length >= 4) {
//                            float characteristicValue = ByteBuffer.wrap(byteValue).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//                            Log.d("Characteristic Changed", "sound value: " + characteristicValue);
//                            runOnUiThread(() -> soundBLE.setText("sound " + String.valueOf(characteristicValue)));
//                        }
//                    }

                    int red = rBLEChar.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                    int green = gBLEChar.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                    int blue = bBLEChar.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                    byte[] rollByteValue = rollBLEChar.getValue();
                    double roll = ByteBuffer.wrap(rollByteValue).order(ByteOrder.LITTLE_ENDIAN).getDouble();
                    byte[] pitchByteValue = pitchBLEChar.getValue();
                    double pitch = ByteBuffer.wrap(pitchByteValue).order(ByteOrder.LITTLE_ENDIAN).getDouble();
                    byte[] gxByteValue = gxBLEChar.getValue();
                    float gx = ByteBuffer.wrap(gxByteValue).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    byte[] gyByteValue = gyBLEChar.getValue();
                    float gy = ByteBuffer.wrap(gyByteValue).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    byte[] gzByteValue = gzBLEChar.getValue();
                    float gz = ByteBuffer.wrap(gzByteValue).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    float angularVelocitySquared = gx * gx + gy * gy + gz * gz;
//                    byte[] temperatureByteValue = temperatureBLEChar.getValue();
//                    float temperature = ByteBuffer.wrap(temperatureByteValue).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                    // Decision tree for medication usage detection
                    if (red > 2 && red < 12 && green < 5 && blue < 5) {
                        if (angularVelocitySquared < 12.0) {
                            if (roll > -10.0 && roll < 10.0 && pitch > -10.0 && pitch < 10.0) {
                                runOnUiThread(() -> usageStatus.setText("Using..."));
                            } else {
                                runOnUiThread(() -> usageStatus.setText("Not in a right angle!"));
                            }
                        } else {
                            runOnUiThread(() -> usageStatus.setText("Not static!"));
                        }
                    } else {
                        runOnUiThread(() -> usageStatus.setText("Not holding!"));
                    }

                    // Decision tree for medication dumping detection

                }

                @Override
                public void onDescriptorWrite (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    Log.d("OnDescriptorWrite", "OnDescriptorWrite");
//                    enableNotifications(gatt, gBLEChar);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        enableNextCharacteristicNotification(gatt);
                    }
                }

//                @Override
//                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//                    Log.d("onCharacteristicRead", "onCharacteristicRead");
//
//                    // Read characteristic value
//                    int characteristicValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
//                    Log.d("Characteristic Value", "Read value: " + characteristicValue);
//                }

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