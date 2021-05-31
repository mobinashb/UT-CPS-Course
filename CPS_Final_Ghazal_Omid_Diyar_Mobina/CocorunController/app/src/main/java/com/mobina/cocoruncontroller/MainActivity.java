package com.mobina.cocoruncontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobina.cocoruncontroller.core.BluetoothService;
import com.mobina.cocoruncontroller.core._3dVector;
import com.mobina.cocoruncontroller.utils.GameConfig;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService mService = null;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter adapterPairedDevices;

    private Button button;
    private Vibrator vibrator;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor, magnetometerSensor, gyroscopeSensor, gravitySensor, gameRotationSensor;
    private double accLastEventTimestamp, magLastEventTimestamp, gyroscopeLastEventTimestamp, gravityLastEventTimestamp, gameRotationLastEventTimestamp;;
    private _3dVector accelerometerTheta, magnetometerTheta, gyroscopeTheta, gravityTheta, gameRotationTheta;

    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Set fullscreen
      this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          WindowManager.LayoutParams.FLAG_FULLSCREEN);

      // Set No Title
      this.requestWindowFeature(Window.FEATURE_NO_TITLE);

      setContentView(R.layout.activity_main);

      ImageView playButton = findViewById(R.id.button_play);

      playButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          showDialog();
        }
      });

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

      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      // If the adapter is null, then Bluetooth is not supported
      if (mBluetoothAdapter == null) {
        Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        finish();
        return;
      }

      initializeSensors();

    }

  private void showDialog() {
    AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
    builderSingle.setTitle("Select a device: ");

    builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    builderSingle.setAdapter(adapterPairedDevices, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Object[] objects = pairedDevices.toArray();
        BluetoothDevice device = (BluetoothDevice) objects[which];
        mService.connect(device);
        Toast.makeText(getApplicationContext(),"device chosen "+device.getName(),Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        }
    });
    builderSingle.show();
  }

  public void initializeDevices()
  {
    pairedDevices = mBluetoothAdapter.getBondedDevices();

    if (pairedDevices.size() > 0) {

      for (BluetoothDevice device : pairedDevices) {
        String deviceName = device.getName();
        String deviceHardwareAddress = device.getAddress(); // MAC address

        adapterPairedDevices.add(deviceName + "\n" + deviceHardwareAddress);
      }
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    // If BT is not on, request that it be enabled.
    // initialize() will then be called during onActivityResult
    if (!mBluetoothAdapter.isEnabled()) {
      Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
      // Otherwise, setup the chat session
    } else {
      if (mService == null) initialize();
      adapterPairedDevices = new ArrayAdapter(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item);
      initializeDevices();
    }
    timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        double steerAngle = Math.asin(gameRotationTheta.y) * 360;
        String command = "";
        if (steerAngle > -5 && steerAngle < 5)
          command = "N";
        else if (steerAngle >= 5 && steerAngle < 65)
        {
          command = String.format("R%d", (int) ((steerAngle - 5)/12)+1);
        }
        else if (steerAngle <= -5 && steerAngle > -60)
          command = String.format("L%d", (int) (( (-steerAngle) - 5)/12)+1);
        else
          command = "N";
          sendMessage(command);
      }
    }, 1000, GameConfig.REFRESH_INTERVAL);
  }
  @Override
  public synchronized void onResume() {
    super.onResume();
    // Performing this check in onResume() covers the case in which BT was
    // not enabled during onStart(), so we were paused to enable it...
    // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
    if (mService != null) {
      // Only if the state is STATE_NONE, do we know that we haven't started already
      if (mService.getState() == BluetoothService.STATE_NONE) {
        // Start the Bluetooth chat services
        mService.start();
      }
    }
  }
  private void initialize() {
    // Initialize the BluetoothService to perform bluetooth connections
    mService = new BluetoothService(this, mHandler);
    // Initialize the buffer for outgoing messages
    mOutStringBuffer = new StringBuffer("");
  }
  @Override
  public void onDestroy() {
    super.onDestroy();
    // Stop the Bluetooth chat services
    if (mService != null) mService.stop();
  }
  private void ensureDiscoverable() {
    if (mBluetoothAdapter.getScanMode() !=
        BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
      Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
      discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
      startActivity(discoverableIntent);
    }
  }
  /**
   * Sends a message.
   * @param message  A string of text to send.
   */
  private void sendMessage(String message) {
    // Check that we're actually connected before trying anything
    if (mService.getState() != BluetoothService.STATE_CONNECTED) {
//      Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
      return;
    }
    // Check that there's actually something to send
    if (message.length() > 0) {
      // Get the message bytes and tell the BluetoothService to write
      byte[] send = message.getBytes();
      mService.write(send);
      // Reset out string buffer to zero and clear the edit text field
      mOutStringBuffer.setLength(0);
    }
  }
  // The Handler that gets information back from the BluetoothService
  @SuppressLint("HandlerLeak")
  private final Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case MESSAGE_STATE_CHANGE:
          switch (msg.arg1) {
            case BluetoothService.STATE_CONNECTED:
              Toast.makeText(MainActivity.this, "connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
              break;
            case BluetoothService.STATE_CONNECTING:
              Toast.makeText(MainActivity.this, "connecting", Toast.LENGTH_SHORT).show();
              break;
            case BluetoothService.STATE_LISTEN:
            case BluetoothService.STATE_NONE:
              Toast.makeText(MainActivity.this, "not connected", Toast.LENGTH_SHORT).show();
              break;
          }
          break;
        case MESSAGE_WRITE:
          byte[] writeBuf = (byte[]) msg.obj;
          // construct a string from the buffer
          String writeMessage = new String(writeBuf);
          break;
        case MESSAGE_READ:
          byte[] readBuf = (byte[]) msg.obj;
          // construct a string from the valid bytes in the buffer
          String readMessage = new String(readBuf, 0, msg.arg1);
          Toast.makeText(MainActivity.this, readMessage, Toast.LENGTH_SHORT).show();
          break;
        case MESSAGE_DEVICE_NAME:
          // save the connected device's name
          mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
          Toast.makeText(getApplicationContext(), "Connected to "
              + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
          break;
        case MESSAGE_TOAST:
          Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
              Toast.LENGTH_SHORT).show();
          break;
      }
    }
  };

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
