package com.mobina.legendofbounca.activity;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import com.mobina.legendofbounca.R;
import com.mobina.legendofbounca.core.utils.RandomGenerator;
import com.mobina.legendofbounca.core.components.Ball;
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
  private _3dVector theta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_game);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          WindowManager.LayoutParams.FLAG_FULLSCREEN);
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      ballImageView = findViewById(R.id.image_ball);
      Pair<Integer, Integer> displaySize = getDisplaySize();
      float ballRadius = dpTopx(GameConfig.BALL_RADIUS);

      _3dVector randomPosition = RandomGenerator.random3dVector(0,
          displaySize.first-(2*ballRadius),
          0, displaySize.second-(2*ballRadius),
          0, 0);

      ball = new Ball(randomPosition,
          new _3dVector(0, 0, 0),
          new _3dVector(0, 0, 0),
          ballImageView,
          displaySize,
          ballRadius);

      final View playButton = findViewById(R.id.button_play);
      final View jumpButton = findViewById(R.id.button_jump);
      playButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          gameStarted = true;
          playButton.setVisibility(View.GONE);
          jumpButton.setVisibility(View.VISIBLE);
        }
      });

      jumpButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (gameStarted)
            ball.generateRandomVelocity();
        }
      });

      theta = new _3dVector(0, 0, 0);
      Timer timer = new Timer();
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          ball.handleSensorEvent(theta,
              sensor,
              ((double) GameConfig.REFRESH_RATE) / 1000);
        }
      }, 0, GameConfig.REFRESH_RATE);

      initializeSensors();
  }

  private void initializeSensors() {
    sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    sensor = (GameConfig.sensor) getIntent().getExtras().get("sensor");
    if (sensor == GameConfig.sensor.GYROSCOPE)
      sensorManager.registerListener(listener, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
    else
      sensorManager.registerListener(listener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
  }

  private SensorEventListener listener = new SensorEventListener() {
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
      if (gameStarted) {
        double deltaT = (sensorEvent.timestamp - lastEventTimestamp) / 1e9;
        if (deltaT > 0) {
          theta = new _3dVector(sensorEvent.values[0],
              sensorEvent.values[1],
              sensorEvent.values[2]);
        }
        lastEventTimestamp = sensorEvent.timestamp;
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
  };

  private Pair<Integer, Integer> getDisplaySize() {
    DisplayMetrics dimension = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(dimension);
    int width = dimension.widthPixels;
    int height = dimension.heightPixels;
    return new Pair<>(width, height);
  }

  private float dpTopx(float dp) {
    return (int) (dp * 1.6);
  }

}

