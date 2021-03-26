package com.mobina.legendofbounca.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.mobina.legendofbounca.R;
import com.mobina.legendofbounca.core.components.Ball;
import com.mobina.legendofbounca.core.components.Box;
import com.mobina.legendofbounca.core.components._3dVector;
import com.mobina.legendofbounca.core.config.GameConfig;

import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends Activity {
  private Boolean gameStarted = false;
  private SensorManager sensorManager;
  private Sensor gravitySensor;
  private Sensor gyroscopeSensor;
  private Ball ball;
  private ImageView ballImageView;
  private GameConfig.sensor sensor;
  private double lastEventTimestamp;
  private Box box;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_game);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          WindowManager.LayoutParams.FLAG_FULLSCREEN);
      sensor = (GameConfig.sensor) getIntent().getExtras().get("sensor");
      ballImageView = findViewById(R.id.image_ball);
      box = new Box();
      ball = new Ball(new _3dVector(0, 0, 0),
          new _3dVector(0, 0, 0),
          new _3dVector(0, 0, 0),
          ballImageView,
          box);
      sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
      gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
      gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
      sensorManager.registerListener(listener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
      sensorManager.registerListener(listener, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
      final View playButton = findViewById(R.id.button_play);
      playButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          gameStarted = true;
          playButton.setVisibility(View.GONE);
        }
      });

//      Timer timer = new Timer();
//      timer.schedule(new TimerTask() {
//        @Override
//        public void run() {
//          ball.updateImgView();
//        }
//      }, 0, GameConfig.REFRESH_RATE);
    }

  private SensorEventListener listener = new SensorEventListener() {
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
      if (gameStarted) {
        System.out.println("sensor event");
        double deltaT = (sensorEvent.timestamp - lastEventTimestamp) / 1e9;
        if (lastEventTimestamp > 0) {
          ball.handleSensorEvent(new _3dVector(sensorEvent.values[0],
                  sensorEvent.values[1],
                  sensorEvent.values[2]),
              sensor,
              deltaT);
        }
        lastEventTimestamp = sensorEvent.timestamp;
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
  };

}

