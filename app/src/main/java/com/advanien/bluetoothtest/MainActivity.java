package com.advanien.bluetoothtest;



import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.widget.ToggleButton;

import com.advanien.bluetoothtest.model.MyConstants;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.advanien.bluetoothtest.lib.BluetoothInterface;

import java.util.Map;


public class MainActivity extends AppCompatActivity {

    BluetoothInterface bluetoothInterface;
    TextView bluetoothDisplay;
    Button discoverBluetoothButton;
    Button discoverBluetoothAppButton;
    SwitchCompat bluetoothModeSwitch;
    TableLayout deviceListTable;
    LinearLayout discoverMode;
    LinearLayout openBTMode;
    Button discoverModeButton;


    // 1. Declare and register the launcher at the class level (before STARTED state)
    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    this::handlePermissionResults);

    private void triggerAllPermissionRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Group all permissions into a single execution array
            String[] permissionsToRequest = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };

            // Safe: Requests everything concurrently via a single system flow
            requestPermissionsLauncher.launch(permissionsToRequest);
        } else {
            // Apparently bluetooth permission explicitly does not request below API level 34
            String[] permissionsToRequest = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            requestPermissionsLauncher.launch(permissionsToRequest);
        }
    }

    // 3. Process the returned results map safely
    private void handlePermissionResults(Map<String, Boolean> result) {
        boolean allGranted = true;

        // Iterate through the results map to check statuses
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            String permissionName = entry.getKey();
            boolean isGranted = entry.getValue();
            switch (permissionName) {
                case Manifest.permission.CAMERA:
                    if (!isGranted) {
                        allGranted = false;
                        Toast.makeText(this, "Camera access rejected", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Camera permission", "ok");
                    }
                    break;
                case Manifest.permission.BLUETOOTH_CONNECT:
                    if (!isGranted) {
                        allGranted = false;
                        Toast.makeText(
                                this, "Bluetooth permission denied",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(
                                this, "Bluetooth permission granted",
                                Toast.LENGTH_SHORT).show();
                        bluetoothInterface.grantedCallback(permissionName);
                    }
                    break;
                case Manifest.permission.BLUETOOTH_SCAN:
                    if (!isGranted) {
                        allGranted = false;
                        Toast.makeText(
                                this, "Bluetooth scan permission denied",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(
                                this, "Bluetooth scan permission granted",
                                Toast.LENGTH_SHORT).show();
                        bluetoothInterface.grantedCallback(permissionName);
                    }
                    break;
                case Manifest.permission.ACCESS_FINE_LOCATION:
                case Manifest.permission.ACCESS_COARSE_LOCATION:
                    if (!isGranted) {
                        allGranted = false;
                        Toast.makeText(
                                this, "Bluetooth access location permission denied",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(
                                this, "Bluetooth access location permission granted",
                                Toast.LENGTH_SHORT).show();
                        bluetoothInterface.grantedCallback(permissionName);
                    }
                    break;
            }
        }

        if (allGranted) {
            proceedWithAppFeature();
        }
    }

    private void proceedWithAppFeature() {
        Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyConstants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case MyConstants.STATE_CONNECTED:
                            setBluetoothStatus("Connected to device");
                            break;
                        case MyConstants.STATE_CONNECTING:
                            setBluetoothStatus("Connecting...");
                            break;
                    }
                    break;
                case MyConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    updateBluetoothChatWindow(readMessage);
                    break;
            }
        }
    };

    // UI Update Methods
    private void setBluetoothStatus(String text) { /* Update TextView */ }
    private void updateBluetoothChatWindow(String text) { /* Append text to UI */ }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        bluetoothDisplay = findViewById(R.id.our_device_text);
        discoverMode = findViewById(R.id.discover_mode);
        openBTMode = findViewById(R.id.listener_mode);

        bluetoothModeSwitch = findViewById(R.id.switch_bluetooth_mode);
        // listen for user toggles
        bluetoothModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // handle toggle
                manageBluetoothMode(isChecked);
                // update UI or perform action
                Toast.makeText(
                        MainActivity.this,isChecked ?
                                "Bluetooth Open" : "Bluetooth Discover",
                        Toast.LENGTH_SHORT).show();
            }
        });

        discoverBluetoothButton = findViewById(R.id.list_bluetooth_dev_button);
        discoverBluetoothAppButton = findViewById(R.id.list_bluetooth_app_dev_button);
        deviceListTable = findViewById(R.id.discovered_device_table);
        deviceListTable.removeAllViews();


        // Encapsulated function for bluetooth interaction
        bluetoothInterface = new BluetoothInterface(this, deviceListTable, mHandler);

        triggerAllPermissionRequest();

        discoverBluetoothButton.setOnClickListener(v->{
            bluetoothInterface.discoveryAndRegisterFoundDevice();
        });

        discoverBluetoothAppButton.setOnClickListener(v->{
            bluetoothInterface.discoveryAndRegisterAppFoundDevice();
        });

    }

    public void manageBluetoothMode(boolean checked) {
        Log.d("select", bluetoothModeSwitch.getTextOn().toString());
        if(checked) {
            discoverMode.setVisibility(View.VISIBLE);
            openBTMode.setVisibility(View.GONE);
        } else {
            discoverMode.setVisibility(View.GONE);
            openBTMode.setVisibility(View.VISIBLE);
        }
    }

}

