package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {
    Button back, red, green, blue, clear;
    AppColor appColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        init();
    }

    private void init() {
        back = findViewById(R.id.settingsBack);
        red = findViewById(R.id.redButton);
        green = findViewById(R.id.greenButton);
        blue = findViewById(R.id.blueButton);
        clear = findViewById(R.id.clearButton);

        appColor = App.getApp().getAppColor();

        updateColors();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.getApp().getAppColor().setRed();
                updateColors();
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.getApp().getAppColor().setGreen();
                updateColors();
            }
        });
        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.getApp().getAppColor().setBlue();
                updateColors();
            }
        });
    }

    @SuppressLint("NewApi")
    void updateColors() {
        clear.setBackgroundResource(appColor.getBgColor());
        back.setBackgroundResource(appColor.getBackColor());
    }
}
