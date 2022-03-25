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
                //sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), // version 1 deprecated
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @SuppressLint("LongLogTag")
    /*public void onSensorChanged(SensorEvent event) {
        float orientation = (float) Math.toDegrees(event.values[2]);
        Log.d("TYPE_ORIENTATION",""+orientation);

        //float rotation = getRoll(event.values[3], event.values[0], event.values[1], event.values[2]);
        //Log.d("TYPE_GAME_ROTATION_VECTOR",""+rotation);
    }*/

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)) {
            float[] rotationVector = event.values;
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(
                    rotationMatrix, rotationVector);

            float[] orientation = new float[3];

            SensorManager.getOrientation(rotationMatrix, orientation);

            // Convert radians to degrees
            long pitch = Math.abs(Math.round(Math.toDegrees(orientation[1])));
            Log.d("Rotation téléphone",""+pitch);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /** Get the roll euler angle in radians, which is the rotation around the z axis.
     * @return the rotation around the z axis in radians (between -PI and +PI) */
    public float getRollRad (float w, float x, float y, float z) {
        return (float) Math.atan2(2f * (w * z + y * x), 1f - 2f * (x * x + z * z));
    }

    /** Get the roll euler angle in degrees, which is the rotation around the z axis. Requires that this quaternion is normalized.
     * @return the rotation around the z axis in degrees (between -180 and +180) */
    public float getRoll (float w, float x, float y, float z) {
        return Math.round((Math.toDegrees(getRollRad( w,  x, y, z)+Math.PI+Math.PI/2))%360);
    }
}