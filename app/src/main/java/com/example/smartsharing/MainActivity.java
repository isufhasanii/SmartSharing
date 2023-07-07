package com.example.smartsharing;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_SELECT = 2;

    Button aufnehmenFoto;
    Button auswaehlenFoto;
    Button setting;
    Button info;

    ActivityResultLauncher<Intent> kameraLauncher;
    ActivityResultLauncher<Intent> galerieLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aufnehmenFoto = findViewById(R.id.aufnehmenFoto);
        auswaehlenFoto = findViewById(R.id.auswaehlenFoto);
        setting = findViewById(R.id.setting);
        info = findViewById(R.id.info);

        kameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.hasExtra("data")) {
                    Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                    openBluetoothActivity(imageBitmap);
                } else {
                    Toast.makeText(this, "Fehler bei Bildübertrag", Toast.LENGTH_SHORT).show();
                }
            }
        });

        galerieLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                    Uri selectedImageUri = data.getData();
                    Bitmap imageBitmap = loadBitmapFromUri(selectedImageUri);
                    openBluetoothActivity(imageBitmap);
                } else {
                    Toast.makeText(this, "Fehler bei Bildübertrag", Toast.LENGTH_SHORT).show();
                }
            }
        });

        aufnehmenFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kameraIntent();
            }
        });

        auswaehlenFoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                galleryIntent();
            }
        });

        setting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openSettingIntent();
            }
        });

        info.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openInfoIntent();
            }
        });
    }

    private void openInfoIntent() {
        Intent infoViewIntent = new Intent(this, InfoActivity.class);
        startActivity(infoViewIntent);
    }

    private void openSettingIntent() {
        Intent settingViewIntent = new Intent(this, SettingActivity.class);
        startActivity(settingViewIntent);
    }

    private void kameraIntent() {
        Intent fotoaufnehmenIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        kameraLauncher.launch(fotoaufnehmenIntent);
    }

    private void galleryIntent() {
        Intent fotoauswaehlenIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galerieLauncher.launch(fotoauswaehlenIntent);
    }

    private Bitmap loadBitmapFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void openBluetoothActivity(Bitmap imageBitmap) {
        Intent bluetoothIntent = new Intent(this, BluetoothActivity.class);
        startActivity(bluetoothIntent);
    }
}