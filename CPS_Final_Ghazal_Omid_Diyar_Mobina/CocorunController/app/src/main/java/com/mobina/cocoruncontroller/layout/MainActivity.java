package com.mobina.cocoruncontroller.layout;

import android.content.Context;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.mobina.cocoruncontroller.R;
import com.mobina.cocoruncontroller._3dVector;

public class MainActivity extends FragmentActivity {

    private Button button;
    private Vibrator vibrator;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor, magnetometerSensor, gyroscopeSensor, gravitySensor, gameRotationSensor;
    private double accLastEventTimestamp, magLastEventTimestamp, gyroscopeLastEventTimestamp, gravityLastEventTimestamp, gameRotationLastEventTimestamp;;
    private _3dVector accelerometerTheta, magnetometerTheta, gyroscopeTheta, gravityTheta, gameRotationTheta;

    private static MainActivity instance;


    public _3dVector getGameRotationTheta() {
        return gameRotationTheta;
    }

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      instance = this;

      // Set fullscreen
      this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          WindowManager.LayoutParams.FLAG_FULLSCREEN);

      // Set No Title
      this.requestWindowFeature(Window.FEATURE_NO_TITLE);

      setContentView(R.layout.activity_main);

      overrideFonts(getApplicationContext(), findViewById(R.id.main));


      accelerometerTheta = new _3dVector(0, 0, 0);
      magnetometerTheta = new _3dVector(0, 0, 0);
      gyroscopeTheta = new _3dVector(0, 0, 0);
      gravityTheta = new _3dVector(0, 0, 0);
      gameRotationTheta = new _3dVector(0, 0, 0);

      initializeSensors();

    }

  private void overrideFonts(final Context context, final View v) {
    try {
      if (v instanceof ViewGroup) {
        ViewGroup vg = (ViewGroup) v;
        for (int i = 0; i < vg.getChildCount(); i++) {
          View child = vg.getChildAt(i);
          overrideFonts(context, child);
        }
      } else if (v instanceof TextView) {
        ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/whatever_it_takes.ttf"));
      } if (v instanceof Button) {
        ((Button) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/whatever_it_takes.ttf"));
      }
    } catch (Exception e) {
    }
  }

  @Override
  public void onStart() {
    super.onStart();
  }
  @Override
  public synchronized void onResume() {
    super.onResume();
      Button wifiButton = findViewById(R.id.button_wifi);
      wifiButton.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
              runWifiFragment();
          }
      });

      Button bluetoothButton = findViewById(R.id.button_bluetooth);
      bluetoothButton.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
              runBluetoothFragment();
          }
      });
  }

  public void vibrate(String data){
      vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
      if (Build.VERSION.SDK_INT >= 26) {
          vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
      } else {
          vibrator.vibrate(200);
      }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    instance = null;
  }

  private void runWifiFragment() {
      findViewById(R.id.main).setVisibility(View.GONE);
      getSupportFragmentManager().beginTransaction()
              .setReorderingAllowed(true)
              .add(R.id.fragment_wifi, WifiFragment.class, null)
              .commit();
  }
private void runBluetoothFragment() {
    findViewById(R.id.main).setVisibility(View.GONE);
    getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragment_bluetooth, BluetoothFragment.class, null)
            .commit();
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