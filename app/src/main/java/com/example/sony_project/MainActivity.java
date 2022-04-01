package com.example.sony_project;

import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

private ImageView play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.play = (ImageView) findViewById(R.id.play);

        if(Build.VERSION.SDK_INT >= 30){
            if (!Environment.isExternalStorageManager()) {
                Intent getPermission = new Intent();
                getPermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getPermission);
            }
        }

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent otherActivity = new Intent(getApplicationContext(), androidcam.class);
                startActivity(otherActivity);
                finish();
            }
        });
    }
}