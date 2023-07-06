package com.example.smartsharing;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BluetoothActivity extends AppCompatActivity {

    private Button discoverDevicesButton;
    private Button connectButton;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
    }
}
