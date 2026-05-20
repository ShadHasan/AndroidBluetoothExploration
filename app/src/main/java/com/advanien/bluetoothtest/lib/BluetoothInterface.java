package com.advanien.bluetoothtest.lib;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.advanien.bluetoothtest.R;
import com.advanien.bluetoothtest.model.MyConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BluetoothInterface implements PermissionCallback {
    private final UUID uuid = UUID.fromString("21989a0f-8f92-4534-8400-1688ffbd6d0a");
    private final String AppName= "BluetoothApp01";
    private TableLayout deviceLister;
    // Get the BluetoothManager service
    private BluetoothManager bluetoothManager;
    BluetoothDevice ListenerPairingDevice;

    // Get the adapter from the manager
    private BluetoothAdapter bluetoothAdapter;
    private String adapterName;
    private Handler handler;
    Context context;
    Map<String, BluetoothDevice> discoveredBluetoothDeviceList;
    private ConnectedThread mConnectedThread;
    private OpenConnection serverConnection = null;
    private BluetoothClient bluetoothClient = null;

    public BluetoothInterface(Context context, TableLayout deviceLister, Handler handler) {
        bluetoothManager = context.getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        this.context = context;
        this.deviceLister = deviceLister;
        this.handler = handler;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    public void addDeviceMainScreenToDeviceLister(String name, String deviceHardwareAddress) {
        TextView textView = new TextView(context);
        textView.setText(name);
        textView.setLayoutParams(new TableRow.LayoutParams(
                dpToPx(0),
                TableRow.LayoutParams.WRAP_CONTENT,
                1.0f
        ));
        textView.setBackgroundColor(Color.WHITE);
        textView.setTextColor(Color.BLACK);
        textView.setVisibility(View.VISIBLE);
        TableRow tableRow = new TableRow(context);
        tableRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button button = new Button(context);
        button.setText("connect");
        button.setLayoutParams(new TableRow.LayoutParams(
                dpToPx(0),
                TableRow.LayoutParams.WRAP_CONTENT,
                1.0f
        ));
        button.setTag(deviceHardwareAddress);

        // On click connect button, start connecting picked discover device as client.
        button.setOnClickListener(v -> {
            ListenerPairingDevice = discoveredBluetoothDeviceList.get(deviceHardwareAddress);
            openBluetoothClient();
        });

        tableRow.addView(textView);
        tableRow.addView(button);
        deviceLister.addView(tableRow);
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public String getAdapterName() {
        return adapterName;
    }
    public String getAppName() {
        return AppName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public BluetoothDevice getListenerPairingDevice() {
        return ListenerPairingDevice;
    }

    public void setAdapterName(String name) {
        this.adapterName = name;
    }

    public void discoveryAndRegisterFoundDevice() {
        Log.d("Discovery for device", "started");
        deviceLister.removeAllViews();
        discoveredBluetoothDeviceList = new HashMap<>();
        if (ActivityCompat
                .checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
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
                    if (discoveredBluetoothDeviceList.get(deviceHardwareAddress) == null) {
                        discoveredBluetoothDeviceList.put(deviceHardwareAddress, device);
                        addDeviceMainScreenToDeviceLister(
                                deviceName + ", " + deviceHardwareAddress,
                                deviceHardwareAddress
                        );
                    }

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

    public void discoveryAndRegisterAppFoundDevice() {
        Log.d("Discovery for app device", "started");
        deviceLister.removeAllViews();
        discoveredBluetoothDeviceList = new HashMap<>();
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
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
                String action = intent.getAction();
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
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // 1. Get the device from the discovered intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (device != null) {
                        Log.d("BT", "Found Device: " + device.getName() + " [" + device.getAddress() + "]");

                        // 2. CRITICAL STEP: Explicitly request UUID service discovery
                        device.fetchUuidsWithSdp();
                    }
                }
                else if (BluetoothDevice.ACTION_UUID.equals(action)) {
                    // 3. This block will now successfully trigger!
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

                    if (uuidExtra != null) {
                        for (Parcelable p : uuidExtra) {
                            Log.d("BT", "Device " + device.getName() + " Has UUID: " + p.toString());
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address
                            if (discoveredBluetoothDeviceList.get(deviceHardwareAddress) == null
                            && p.toString().equals(uuid.toString())) {
                                discoveredBluetoothDeviceList.put(deviceHardwareAddress, device);
                                addDeviceMainScreenToDeviceLister(
                                        deviceName + ", " + deviceHardwareAddress,
                                        deviceHardwareAddress
                                );
                            }
                        }
                    }
                }
            }
        };

        // 2. Register for ACTION_FOUND
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND); // Must be explicitly added
        filter.addAction(BluetoothDevice.ACTION_UUID); // Must be explicitly added

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
        ((TextView) activity.findViewById(R.id.our_device_text)).setText(getAdapterName());
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
                Log.d("Adapter ready", "Adapter ready for discovery");
                //discoveryAndRegisterFoundDevice(); // This just for debugging explicitly discovering
                break;
        }
    }

    public void manageMyConnectedSocket(BluetoothSocket socket) {
        // Optional: Send a status update message back to your UI Activity/Fragment
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Cancel any existing running transfer thread first
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Initialize the thread to manage the socket and start data transfer
        mConnectedThread = new ConnectedThread(socket, handler);
        mConnectedThread.start();


        Message message = handler.obtainMessage(MyConstants.MESSAGE_STATE_CHANGE, MyConstants.STATE_CONNECTED, -1, socket.getRemoteDevice().getName());
        message.sendToTarget();
    }

    public void connectionFailed(String endpoint, String description) {
        Message message;
        // Optional: Send a status update message back to your UI Activity/Fragment
        switch (endpoint) {
            case "client":
                message = handler.obtainMessage(
                    MyConstants.CONNECTION_FAILED, MyConstants.CLIENT_FAILURE, -1, description);
            message.sendToTarget();
            break;
            case "server":
                message = handler.obtainMessage(
                        MyConstants.CONNECTION_FAILED, MyConstants.SERVER_FAILURE, -1, description);
                message.sendToTarget();
                break;
        }

    }

    public void makeMeDiscoverable() {
        // This makes YOUR device visible to others
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300); // 300 seconds
        if (ActivityCompat
                .checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("Not enough permission", "Making discoverable");
            return;
        }
        Log.d("Discoverable state", "starting");
        context.startActivity(discoverableIntent);
        Log.d("Discoverable state", "started");
    }

    public synchronized void openServerConnection() {
        serverConnection = new OpenConnection(this);
        serverConnection.start();
    }

    public synchronized void closeServerConnection() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (serverConnection != null) {
            serverConnection.cancel();
            serverConnection = null;
        }
    }

    public synchronized void openBluetoothClient(){
        bluetoothClient = new BluetoothClient(this);
        bluetoothClient.start();
    }
    public synchronized void closeBluetoothClient() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (bluetoothClient != null) {
            bluetoothClient.cancel();
            bluetoothClient = null;
        }
    }

    public void sendMessageToConnectedThread(String message) {
        if (mConnectedThread != null && mConnectedThread.isAlive()) {
            // Invoke the write method directly from the UI thread
            mConnectedThread.write(message);
        } else {
            Toast.makeText(context, "Device not connected", Toast.LENGTH_SHORT).show();
        }
    }

}

class OpenConnection extends Thread {
    private BluetoothServerSocket serverSocket;
    BluetoothInterface bluetoothInterface;

    //Context context, BluetoothAdapter bluetoothAdapter, String AppName, UUID MY_UUID
    public OpenConnection(BluetoothInterface bluetoothInterface) {
        this.bluetoothInterface = bluetoothInterface;
        BluetoothServerSocket tmp = null;
        try {
            // UUID must match the client's UUID
            if (ActivityCompat.checkSelfPermission(
                    bluetoothInterface.context, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            tmp = bluetoothInterface.getBluetoothAdapter()
                    .listenUsingRfcommWithServiceRecord(
                            bluetoothInterface.getAppName(),
                            bluetoothInterface.getUuid());
        } catch (IOException e) { }
        serverSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                socket = serverSocket.accept(); // Blocks until connection accepted
            } catch (IOException e) { // if socket closed IOException break the loop.
                break;
            }
            if (socket != null) {
                bluetoothInterface.manageMyConnectedSocket(socket); // Pass socket to data transfer
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    Log.d("Server socket IO exception", e.toString());
                }
                break;
            }
        }
    }

    public void cancel() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.e("Bluetooth server socker error", "Could not close the connect socket", e);
        }

    }

}

class BluetoothClient extends Thread {
    private BluetoothSocket mmSocket;
    BluetoothInterface bluetoothInterface;

    // Context context, BluetoothDevice device, BluetoothAdapter bluetoothAdapter, UUID MY_UUID
    public BluetoothClient(BluetoothInterface bluetoothInterface) {
        this.bluetoothInterface = bluetoothInterface;
        BluetoothSocket tmp = null;
        try {
// Get a socket to connect with the given device
            if (ActivityCompat.checkSelfPermission(
                    bluetoothInterface.context,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            tmp = bluetoothInterface
                    .getListenerPairingDevice()
                    .createRfcommSocketToServiceRecord(bluetoothInterface.getUuid());
        } catch (IOException e) { }
        mmSocket = tmp;
    }
    public void run() {
        if (ActivityCompat.checkSelfPermission(
                bluetoothInterface.context,
                Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // Always cancel discovery before connecting
        bluetoothInterface.getBluetoothAdapter().cancelDiscovery();
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and clean up
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e("Bluetooth", "Could not close the client socket", closeException);
            }
            bluetoothInterface.connectionFailed("client", "Socket IOConnection failed"); // Handle error (e.g., notify UI via Handler)
            return;
        }

        // Connection attempt succeeded! Pass the socket to your ConnectedThread
        bluetoothInterface.manageMyConnectedSocket(mmSocket);
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e("Bluetooth", "Could not close the client socket during cancel", e);
        }
    }
}

// Simplified ConnectedThread example
class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler handler;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        this.handler = handler;
        try {
            mmInStream = socket.getInputStream();
            mmOutStream = socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        // Bytes reading sample
        StringBuilder msgStr = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytes;
        while (true) {
            try {
                bytes = mmInStream.read(buffer); // Read data
                msgStr.append(new String(buffer, StandardCharsets.UTF_8));
                // Check for End of Stream and break
                if (bytes == -1) {
                    break;
                }

            } catch (IOException e) {
                Log.e("Socket data reading error",new String(buffer, StandardCharsets.UTF_8), e);
                break;
            }
            Message message = handler
                    .obtainMessage(
                            MyConstants.MESSAGE_READ,-1,-1,new String(msgStr)
                            );
            handler.sendMessage(message);
        }
    }

    public void write(String str) { // Send data
        String str2 = str + -1;
        try {
            mmOutStream.write(str2.getBytes(StandardCharsets.UTF_8));
            Log.d("sent successfully", str2);
        } catch (IOException e) {
            Log.e("sending msg failed", str2, e);
        }
    }

    public void cancel() {
        try {
            // Closing the socket immediately breaks the read() loop in the thread run() method
            mmSocket.close();
        } catch (IOException e) {
            Log.e("Bluetooth", "Could not close the connect socket", e);
        }
    }

}
