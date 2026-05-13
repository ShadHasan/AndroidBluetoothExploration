package com.advanien.bluetoothtest.lib;


import static androidx.core.content.ContextCompat.registerReceiver;
import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.advanien.bluetoothtest.R;

public class BluetoothInterface implements PermissionCallback {
    private TableLayout deviceLister;
    // Get the BluetoothManager service
    private BluetoothManager bluetoothManager;
    // Get the adapter from the manager
    private BluetoothAdapter bluetoothAdapter;
    private String adapterName;
    Context context;

    public BluetoothInterface(Context context, TableLayout deviceLister) {
        bluetoothManager = context.getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        this.context = context;
        this.deviceLister = deviceLister;
    }

    public void addDeviceMainScreenToDeviceLister(String name) {
        TextView textView = new TextView(context);
        textView.setText(name);
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                4.0f
        ));
        textView.setBackgroundColor(Color.WHITE);
        textView.setTextColor(Color.BLACK);
        textView.setVisibility(View.VISIBLE);
        TableRow tableRow = new TableRow(context);
        tableRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tableRow.addView(textView);
        deviceLister.addView(tableRow);
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

    public void discoveryAndRegisterFoundDevice() {
        Log.d("called", "Not called");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.d("called", "Here 1");
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (bluetoothAdapter.isEnabled()) {
            Log.d("Bluetooth function", "starting discovery");
            bluetoothAdapter.startDiscovery();
            Log.d("Bluetooth function", "discovery is " + bluetoothAdapter.isDiscovering());
        } else {
            Toast.makeText(context, "Turn on bluetooth device", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Handle results in BroadcastReceiver
        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.BLUETOOTH_SCAN) !=
                            PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Log.d("Listing device", "======");
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.d("Device discovered", deviceName + ", " + deviceHardwareAddress);
                    addDeviceMainScreenToDeviceLister(deviceName + ", " + deviceHardwareAddress);
                }
            }
        };

        // 2. Register for ACTION_FOUND
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        /*
        context: The context to register the receiver
        (e.g., this in an Activity).receiver: The BroadcastReceiver instance.filter:
        The IntentFilter defining the actions to listen for.flags: Crucial on SDK 34+, must be
        either ContextCompat.RECEIVER_EXPORTED or ContextCompat.RECEIVER_NOT_EXPORTED.
         */

        context.registerReceiver(receiver, filter);

    }

    private void saveAndDisplayBluetoothName() {
        Activity activity = (Activity) context;
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT) !=
                PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        setAdapterName(getBluetoothAdapter().getName());
        ((TextView)activity.findViewById(R.id.our_device_text)).setText(getAdapterName());
    }

    @Override
    public void grantedCallback() {
        Log.d("Implemented", "Pseudo empty method");
    }

    @Override
    public void grantedCallback(String permission) {
        switch (permission) {
            case Manifest.permission.BLUETOOTH_CONNECT:
                saveAndDisplayBluetoothName();
                break;
            case Manifest.permission.BLUETOOTH_SCAN:
                // May be we will not call it. since it should be only initial debugging code
                // on permission granted
                discoveryAndRegisterFoundDevice();
                break;
        }
    }
}
