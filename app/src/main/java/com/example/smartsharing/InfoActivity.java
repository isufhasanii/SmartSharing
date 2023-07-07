package com.example.smartsharing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class InfoActivity extends AppCompatActivity {

    private Button exit1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        exit1.findViewById(R.id.exitInfo);
        exit1.setOnClickListener(new View.OnClickListener(){
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

