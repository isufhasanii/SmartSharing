package com.example.smartsharing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SettingActivity extends AppCompatActivity {

    private Switch colorSwitch;
    private ConstraintLayout layout;
    private SharedPreferences sharedPreferences;
    private Button exit;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        colorSwitch = findViewById(R.id.colorSwitch);
        layout = findViewById(R.id.layout);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        exit = findViewById(R.id.exit);

        boolean isBlackBackground = sharedPreferences.getBoolean("isBlackBackground", false);
        layout.setBackgroundColor(isBlackBackground ? Color.BLACK : Color.WHITE);
        colorSwitch.setChecked(isBlackBackground);

        colorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isBlackBackground", isChecked);
                editor.apply();

                layout.setBackgroundColor(isChecked ? Color.BLACK : Color.WHITE);
            }
        });

        exit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openMainIntent();
            }
        });
    }

    private void openMainIntent() {
        Intent mainViewIntent = new Intent(this, MainActivity.class);
        startActivity(mainViewIntent);
    }
}