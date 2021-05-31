package com.mobina.cocoruncontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;


import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.lang.Math;

import com.mobina.cocoruncontroller.GameConfig;


public class SensorActivity extends AppCompatActivity {

    private Button button;
    private Vibrator vibrator;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor, magnetometerSensor, gyroscopeSensor, gravitySensor, gameRotationSensor;
    private double accLastEventTimestamp, magLastEventTimestamp, gyroscopeLastEventTimestamp, gravityLastEventTimestamp, gameRotationLastEventTimestamp;;
    private _3dVector accelerometerTheta, magnetometerTheta, gyroscopeTheta, gravityTheta, gameRotationTheta;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(200);
                }
            }
        });
        accelerometerTheta = new _3dVector(0, 0, 0);
        magnetometerTheta = new _3dVector(0, 0, 0);
        gyroscopeTheta = new _3dVector(0, 0, 0);
        gravityTheta = new _3dVector(0, 0, 0);
        gameRotationTheta = new _3dVector(0, 0, 0);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                TextView tv = findViewById(R.id.textView);
                double steerAngle = Math.asin(gameRotationTheta.x) * 360;
                if (steerAngle > 85 && steerAngle < 95)
                    tv.setText("Steady");
                else if (steerAngle >= 95 && steerAngle < 150)
                    tv.setText(String.format("Steer to Left: %f degrees", steerAngle - 90));
                else if (steerAngle <= 85 && steerAngle > 30)
                    tv.setText(String.format("Steer to Right: %f degrees", steerAngle - 90));
                else
                    tv.setText(String.format("Injuri bazi nakon"));



            }
        }, 0, GameConfig.REFRESH_RATE);

        initializeSensors();

    }

    private void initializeSensors() {
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        gameRotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(magnetometerListener, magnetometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(gyroscopeListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(gameRotationListener, gameRotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            double deltaT = (sensorEvent.timestamp - accLastEventTimestamp) / 1e9;
            if (deltaT > 0.1) {
                accelerometerTheta = new _3dVector(sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]);
                accLastEventTimestamp = sensorEvent.timestamp;

            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    private SensorEventListener magnetometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            double deltaT = (sensorEvent.timestamp - magLastEventTimestamp) / 1e9;

            if (deltaT > 0.1) {
                magnetometerTheta = new _3dVector(sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]);
                magLastEventTimestamp = sensorEvent.timestamp;

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };


    private SensorEventListener gyroscopeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            double deltaT = (sensorEvent.timestamp - gyroscopeLastEventTimestamp) / 1e9;

            if (deltaT > 0.1) {
                gyroscopeTheta = new _3dVector(sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]);
                gyroscopeLastEventTimestamp = sensorEvent.timestamp;

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };


    private SensorEventListener gravityListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            double deltaT = (sensorEvent.timestamp - gravityLastEventTimestamp) / 1e9;

            if (deltaT > 0.1) {
                gravityTheta = new _3dVector(sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]);
                gravityLastEventTimestamp = sensorEvent.timestamp;

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };


    private SensorEventListener gameRotationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            double deltaT = (sensorEvent.timestamp - gameRotationLastEventTimestamp) / 1e9;

            if (deltaT > 0.1) {
                gameRotationTheta = new _3dVector(sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2],
                        sensorEvent.values[3]);
                gameRotationLastEventTimestamp = sensorEvent.timestamp;

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

}