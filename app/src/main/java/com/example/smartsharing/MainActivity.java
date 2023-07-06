package com.example.smartsharing;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_SELECT = 2;

    Button aufnehmenFoto;
    Button auswaehlenFoto;
    Button setting;
    Button info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aufnehmenFoto = findViewById(R.id.aufnehmenFoto);
        auswaehlenFoto = findViewById(R.id.auswaehlenFoto);
        setting = findViewById(R.id.setting);
        info = findViewById(R.id.info);

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
        if (fotoaufnehmenIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(fotoaufnehmenIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void galleryIntent(){
        Intent fotoauswaehlenIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (fotoauswaehlenIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(fotoauswaehlenIntent, REQUEST_IMAGE_SELECT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                openBluetoothActivity(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_SELECT) {
                Uri selectedImageUri = data.getData();
                Bitmap imageBitmap = loadBitmapFromUri(selectedImageUri);
                openBluetoothActivity(imageBitmap);
            }
        }
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
        bluetoothIntent.putExtra("imageBitmap", imageBitmap);
        startActivity(bluetoothIntent);
    }
}