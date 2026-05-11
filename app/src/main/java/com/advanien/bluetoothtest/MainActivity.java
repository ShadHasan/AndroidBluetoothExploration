package com.advanien.bluetoothtest;


import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.advanien.bluetoothtest.lib.BluetoothInterface;


public class MainActivity extends AppCompatActivity {

    BluetoothInterface bluetoothInterface;
    TextView bluetoothDisplay;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Success! Start the camera now.
                    Log.i("Bluetooth permission", "ok");
                } else {
                    // Permission denied. Show a message explaining why you need it.
                    Toast.makeText(
                            this, "Bluetooth permission is required for the preview.", Toast.LENGTH_SHORT).show();
                }
            });

    private void requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    // 1. Define the launcher (do this as a class member)
    ActivityResultLauncher<String> requestBluetoothPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted! Call your name-getting logic here
                    displayBluetoothName();
                }
            });

    // 2. Your trigger method
    private void checkPermissionAndGetBluetoothName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED) {
                displayBluetoothName();
            } else {
                // This pops the dialog and the launcher above handles the "waiting"
                requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {
            // Legacy: Just call it
            displayBluetoothName();
        }
    }

    // 3. The actual logic
    private void displayBluetoothName() {
        bluetoothInterface = new BluetoothInterface(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothInterface.setAdapterName(bluetoothInterface.getBluetoothAdapter().getName());
        bluetoothDisplay.setText(bluetoothInterface.getAdapterName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestCameraPermission();
        setContentView(R.layout.main_activity);
        bluetoothDisplay = findViewById(R.id.our_device_text);
        checkPermissionAndGetBluetoothName();
    }

}
