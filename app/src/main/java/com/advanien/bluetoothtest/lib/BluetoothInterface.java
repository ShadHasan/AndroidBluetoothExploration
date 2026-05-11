package com.advanien.bluetoothtest.lib;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

public class BluetoothInterface {
    // Get the BluetoothManager service
    private BluetoothManager bluetoothManager;
    // Get the adapter from the manager
    private BluetoothAdapter bluetoothAdapter;
    private String adapterName;

    public BluetoothInterface(Context context) {
        bluetoothManager = context.getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }
    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String name) {
        this.adapterName = name;
    }

}
