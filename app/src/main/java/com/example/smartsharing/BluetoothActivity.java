package com.example.smartsharing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_SELECT = 2;
    private static final String CHANNEL_ID = "bluetooth_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final String TARGET_DEVICE_NAME = "Constant Device";
    private static final String TARGET_DEVICE_ADDRESS = "00:11:22:33:44:55";

    Button settingBluetooth;
    Button infoBluetooth;


    Button bluetoothDeviceFinden;
    Button connectButton;
    private ListView deviceListView;

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    private List<BluetoothDeviceModel> bluetoothDevices;
    private ArrayAdapter<BluetoothDeviceModel> deviceAdapter;
    private Bitmap selectedImage;

    private Timer searchTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        bluetoothDeviceFinden = findViewById(R.id.bluetoothDeviceFinden);
        connectButton = findViewById(R.id.connectButton);
        deviceListView = findViewById(R.id.bluetoothDeviceList);
        settingBluetooth = findViewById(R.id.settingBluetooth);
        infoBluetooth = findViewById(R.id.infoBluetooth);

        checkBluetoothStatus();

        settingBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettingIntent();
            }
        });

        infoBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInfoIntent();
            }
        });

        if (getIntent().hasExtra("imageBitmap")) {
            selectedImage = getIntent().getParcelableExtra("imageBitmap");
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth wird auf diesem Gerät nicht unterstützt", Toast.LENGTH_SHORT).show();
            return;
        }

        bluetoothDeviceFinden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverBluetoothDevices();
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImageToSelectedDevice();
                shareImageViaBluetooth(selectedImage);
                openMainActivity();
            }
        });

        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(deviceAdapter);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDeviceModel selectedDevice = bluetoothDevices.get(i);
                connectToDevice(selectedDevice.getAddress());
            }
        });
    }

    //Share API
    private void shareImageViaBluetooth(Bitmap imageBitmap) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth wird auf diesem Gerät nicht unterstützt", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth ist nicht aktiviert", Toast.LENGTH_SHORT).show();
            return;
        }

        File imageFile = new File(getCacheDir(), "shared_image.jpg");
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Fehler beim Speichern des Bildes", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));

        shareIntent.setPackage("com.android.bluetooth");

        PackageManager packageManager = getPackageManager();
        if (shareIntent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, "Bluetooth-Teilen wird auf diesem Gerät nicht unterstützt", Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(Intent.createChooser(shareIntent, "Bild per Bluetooth senden"));
    }
    private void openMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

    private void openInfoIntent() {
        Intent infoViewIntent = new Intent(this, InfoActivity.class);
        startActivity(infoViewIntent);
    }

    private void checkBluetoothStatus() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth on", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSettingIntent() {
        Intent settingViewIntent = new Intent(this, SettingActivity.class);
        startActivity(settingViewIntent);
    }

    private void discoverBluetoothDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (bluetoothAdapter.isDiscovering()) {
            Toast.makeText(this, "Suche läuft bereits", Toast.LENGTH_SHORT).show();
            return;
        }

        int searchDuration = 10000;
        searchTimer = new Timer();
        searchTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopBluetoothDiscovery();
            }
        }, searchDuration);
        deviceAdapter.clear();
        bluetoothDevices = new ArrayList<>();

        BluetoothDeviceModel targetDevice = new BluetoothDeviceModel(TARGET_DEVICE_NAME, TARGET_DEVICE_ADDRESS);
        bluetoothDevices.add(targetDevice);
        deviceAdapter.add(targetDevice);

        bluetoothAdapter.startDiscovery();
    }

    private void stopBluetoothDiscovery() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(deviceDiscoveryReceiver);
        searchTimer.cancel();
        Toast.makeText(this, "Suche abgeschlossen", Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver deviceDiscoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                BluetoothDeviceModel deviceModel = new BluetoothDeviceModel(device.getName(), device.getAddress());
                bluetoothDevices.add(deviceModel);
                deviceAdapter.add(deviceModel);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context, "Suche abgeschlossen", Toast.LENGTH_SHORT).show();
                unregisterReceiver(deviceDiscoveryReceiver);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(deviceDiscoveryReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(deviceDiscoveryReceiver);
    }

    private void connectToDevice(String deviceAddress) {
        BluetoothDevice device;
        UUID uuid;
        if (deviceAddress.equals(TARGET_DEVICE_ADDRESS)) {
            Toast.makeText(this, "Verbindung hergestellt mit " + TARGET_DEVICE_NAME, Toast.LENGTH_SHORT).show();
        } else {
            device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            try {
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
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                Toast.makeText(this, "Verbindung hergestellt mit " + device.getName(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Verbindung konnte nicht hergestellt werden", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showNotification(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bluetooth Sharing")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendImageToSelectedDevice() {
        if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
            Toast.makeText(this, "Keine Verbindung zu einem Gerät hergestellt", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImage == null) {
            Toast.makeText(this, "Kein Bild ausgewählt", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            OutputStream outputStream = bluetoothSocket.getOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            showNotification("Bild erfolgreich gesendet");
        } catch (IOException e) {
            e.printStackTrace();
            showNotification("Fehler beim Senden des Bildes");
        }
    }


}
