package com.example.projet_sonification;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.math.MathUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private SensorManager sensorManager;

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager =  (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @SuppressLint("LongLogTag")

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)) {

            float axeX = Math.round(event.values[0]);
            float axeY = Math.round(event.values[1]);
            float axeZ = Math.round(event.values[2]);

            Log.d("vitesse Axe X",""+axeX);
            Log.d("vitesse Axe Y",""+axeY);
            Log.d("vitesse Axe Z",""+axeZ);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}