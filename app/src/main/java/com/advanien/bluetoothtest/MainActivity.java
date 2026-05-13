package com.advanien.bluetoothtest;


import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.advanien.bluetoothtest.lib.BluetoothInterface;

import java.util.Map;


public class MainActivity extends AppCompatActivity {

    BluetoothInterface bluetoothInterface;
    TextView bluetoothDisplay;
    Button discoverBluetooth;
    TableLayout deviceListTable;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        bluetoothDisplay = findViewById(R.id.our_device_text);
        discoverBluetooth = findViewById(R.id.list_card_button);
        deviceListTable = findViewById(R.id.discovered_device_table);
        // deviceListTable.removeAllViews();


        // Encapsulated function for bluetooth interaction
        bluetoothInterface = new BluetoothInterface(this, deviceListTable);

        triggerAllPermissionRequest();

    }

}
