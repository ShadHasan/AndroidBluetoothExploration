package com.advanien.bluetoothtest.model;

public interface MyConstants {
    // Types of messages sent to the Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Constants that indicate the current connection state
    int STATE_NONE = 0;       // Doing nothing
    int STATE_LISTEN = 1;     // Listening for incoming connections
    int STATE_CONNECTING = 2; // Initiating an outgoing connection
    int STATE_CONNECTED = 3;  // Connected to a remote device
    int CONNECTION_FAILED = 6;
    int CLIENT_FAILURE = 7;
    int SERVER_FAILURE = 8;
}
