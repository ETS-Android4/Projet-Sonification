package com.example.sony_project;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.SoundPool;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class androidcam extends AppCompatActivity implements SensorEventListener{

    private TextureView textureView;

    private SoundPool soundPool;
    // Maximum sound stream.
    private static final int MAX_STREAMS = 2;
    // Stream type.
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private boolean loaded;
    private boolean isFirstTime;
    private float volume;
    private float volumeAlert;
    private int soundId;
    private int soundIdAlert;
    private int streamId = -1;
    private int streamIdAlert = -1;

    //Sensor
    private SensorManager sensorManager;

    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;

    // Save to file

    private File file;
    private static final int Request_Camera_Permission = 200 ;
    //private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();

        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            //cameraDevice = null;
        }
    };









    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_androidcam);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        textureView = findViewById(R.id.textureView);
        assert textureView != null ;
        textureView.setSurfaceTextureListener(textureListener);

        Button btnCapture = findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(view -> takePicture());

        //------------------------------------------------------------------

        // AudioManager audio settings for adjusting the volume
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Current volume Index of particular stream type.
        float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);

        // Get the maximum volume index for a particular stream type.
        float maxVolumeIndex  = (float) audioManager.getStreamMaxVolume(streamType);

        // Volume (0 --> 1)
        this.volumeAlert = currentVolumeIndex / maxVolumeIndex;

        this.volume = 0;

        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        this.setVolumeControlStream(streamType);

        AudioAttributes audioAttrib = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        SoundPool.Builder builder= new SoundPool.Builder();
        builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

        this.soundPool = builder.build();

        // When Sound Pool load complete.
        this.soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> loaded = true);

        // Load sound file into SoundPool.

        this.soundId = this.soundPool.load(this, R.raw.stringsound,1);
        this.soundIdAlert = this.soundPool.load(this, R.raw.bip_alerte_flou_image,1);

        //------------------------------------------------------------------
    }










    private void takePicture() {
        if(cameraDevice == null)
            return;
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{ CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
                Size[] jpegSizes = null;
                if(characteristics != null){
            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.JPEG);
        }


            // Capture image with custom size

        int width = 640 ;
        int height = 480 ;
        if(jpegSizes != null && jpegSizes.length > 0 ) {
            width = jpegSizes[0].getWidth();
            height = jpegSizes[0].getHeight();
        }

        file = new File(Environment.getExternalStorageDirectory()+"/Pictures/"+UUID.randomUUID()+".JPEG");

        ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
        List<Surface> outputSurface = new ArrayList<>(2);
        outputSurface.add(reader.getSurface());
        outputSurface.add(new Surface(textureView.getSurfaceTexture()));
        CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(reader.getSurface());
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        try{
            file.createNewFile();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {
                Image image = null;
                try{
                        image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                save(bytes);
                } catch (IOException e )
                {
                    e.printStackTrace();
                } finally{
                    {
                      if(image != null)
                          image.close();
                    }
                }
                }
          private void save(byte[] bytes) throws IOException {
              try (FileOutputStream outputStream = new FileOutputStream(file)) {
                  outputStream.write(bytes);
              }
            }
            };
        reader.setOnImageAvailableListener(readerListener,mBackgroundHandler);
        CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                Toast.makeText(androidcam.this, "saved"+file, Toast.LENGTH_SHORT).show();
                createCameraPreview();
            }
        };

        cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                try{
                    cameraCaptureSession.capture(captureBuilder.build(),captureListener,mBackgroundHandler);
                } catch (CameraAccessException e ) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

            }
        },mBackgroundHandler);

        } catch(CameraAccessException e ) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    UpdatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(androidcam.this, "Changed", Toast.LENGTH_SHORT).show();

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        }

    private void UpdatePreview() {
        if (cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


    private void openCamera() {

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            String cameraID = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP_MAXIMUM_RESOLUTION);
            }
            else {
                map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            }
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Check SelfPermission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, Request_Camera_Permission);
                return;
            }
            manager.openCamera(cameraID,stateCallBack, null);

        } catch (CameraAccessException cameraAccessException) {
            cameraAccessException.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == Request_Camera_Permission){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "You can't use the camera without the permission", Toast.LENGTH_SHORT).show();
                finish();
            }

        }

    }
    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundthread();
        if(textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);


        sensorManager =  (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_FASTEST);
}
    @Override
    protected void onPause(){
        stopBackgroundthread();
        sensorManager.unregisterListener(this);
        this.soundPool.pause(this.soundId);
        cameraDevice.close();
        super.onPause();


    }

    private void stopBackgroundthread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    private void startBackgroundthread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        //mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.isFirstTime = true;
    }

    @Override
    protected void onStop() {
        this.soundPool.stop(this.soundId);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        this.soundPool.stop(this.soundId);
        this.soundPool.unload(this.soundId);
        this.soundPool.unload(this.soundIdAlert);
        if(soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        super.onDestroy();
    }




    // When users click on the button
    public void playSound()  {
        if(loaded)  {
            float leftVolume = volume;
            float rightVolume = volume;
            Log.d("play sound","----------------------------------------------------------");
            // Play sound. Returns the ID of the new stream.

            do {
                streamId = this.soundPool.play(this.soundId,leftVolume, rightVolume, 1, -1, 1f);
            } while(streamId==0);
        }
    }

    public void playSoundAlert( )  {
        if(loaded)  {
            float leftVolume = volumeAlert;
            float rightVolume = volumeAlert;
            Log.d("play sound","----------------------------------------------------------");
            // Play sound. Returns the ID of the new stream.
            streamIdAlert = this.soundPool.play(this.soundIdAlert,leftVolume, rightVolume, 0, 0, 1f);
        }
    }


    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)) {
            if(isFirstTime && loaded){
                playSound();
                isFirstTime = false;
            }
            // Get rotation matrix
            float[] rotationMatrix = new float[16];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            // Remap coordinate system
            float[] remappedRotationMatrix = new float[16];
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z,
                    remappedRotationMatrix);

            // Convert to orientations
            float[] orientation = new float[3];
            SensorManager.getOrientation(remappedRotationMatrix, orientation);

            float pitch = Math.abs(orientation[2])*2;
            this.volume = (float) Math.sin(pitch);
            soundPool.setVolume(streamId, this.volume, this.volume);
        }
        if (event.sensor == sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) && !(event.sensor == sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR))) {

            float axeX = Math.abs(Math.round(event.values[0]));
            float axeY = Math.abs(Math.round(event.values[1]));
            float axeZ = Math.abs(Math.round(event.values[2]));

            Log.d("vitesse Axe X",""+axeX);
            Log.d("vitesse Axe Y",""+axeY);
            Log.d("vitesse Axe Z",""+axeZ);
            if(axeX>1 || axeY>1 || axeZ>1){
                playSoundAlert();
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}




