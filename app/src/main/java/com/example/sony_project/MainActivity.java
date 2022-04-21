package com.example.sony_project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        ImageView play = findViewById(R.id.play);

        play.setOnClickListener(view -> {
            Intent otherActivity = new Intent(getApplicationContext(), androidcam.class);
            startActivity(otherActivity);
            finish();
        });
    }
}