package com.example.musicplayer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.net.MalformedURLException;
import java.net.URL;

public class RadioActivity extends AppCompatActivity {
    Button setRadioButton;
    EditText radioUrlEt;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);

        setRadioButton = findViewById(R.id.setRadioButton);
        radioUrlEt = findViewById(R.id.radioUrl);

        setRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    URL url = new URL(radioUrlEt.getText().toString());
                    stopService(App.getPlayerService());
                    App.setSource(url.toString());
                    Toast.makeText(RadioActivity.this, "url has setted", Toast.LENGTH_SHORT).show();
                    startService(App.getPlayerService());
                } catch (MalformedURLException e) {
                    Toast.makeText(RadioActivity.this, "Неверный формат url", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }
}
