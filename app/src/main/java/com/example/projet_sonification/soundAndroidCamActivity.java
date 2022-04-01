package com.example.projet_sonification;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class soundAndroidCamActivity extends AppCompatActivity implements SensorEventListener {
    private SoundPool soundPool;

    private AudioManager audioManager;
    private Button button;


    // Maximumn sound stream.
    private static final int MAX_STREAMS = 5;

    // Stream type.
    private static final int streamType = AudioManager.STREAM_MUSIC;

    private boolean loaded;

    private int soundId;

    private float volume;

    private int streamId;

    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sound_activity);

        this.button = (Button) this.findViewById(R.id.button);

        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound();
            }
        });


        // AudioManager audio settings for adjusting the volume
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Current volumn Index of particular stream type.
        //float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);

        // Get the maximum volume index for a particular stream type.
        //float maxVolumeIndex  = (float) audioManager.getStreamMaxVolume(streamType);

        // Volumn (0 --> 1)
        //this.volume = currentVolumeIndex / maxVolumeIndex;

        this.volume = 0;

        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        this.setVolumeControlStream(streamType);

        // For Android SDK >= 21
        if (Build.VERSION.SDK_INT >= 21 ) {
            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder= new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

            this.soundPool = builder.build();
        }
        // for Android SDK < 21
        else {
            // SoundPool(int maxStreams, int streamType, int srcQuality)
            this.soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        // When Sound Pool load complete.
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        // Load sound file (destroy.wav) into SoundPool.
        this.soundId = this.soundPool.load(this, R.raw.stringsound1,1);

    }



    // When users click on the button
    public void playSound()  {
        if(loaded)  {
            float leftVolumn = volume;
            float rightVolumn = volume;
            // Play sound. Returns the ID of the new stream.
            streamId = this.soundPool.play(this.soundId,leftVolumn, rightVolumn, 1, 10, 1f);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        sensorManager =  (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(
                this,
                //sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), // version 1 deprecated
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
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
            this.volume = (float) Math.sin(10*pitch/(91.19*Math.PI));
            soundPool.setVolume(streamId, this.volume, this.volume);
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