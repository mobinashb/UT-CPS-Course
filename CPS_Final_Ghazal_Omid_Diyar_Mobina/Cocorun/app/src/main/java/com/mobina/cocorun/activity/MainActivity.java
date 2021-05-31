package com.mobina.cocorun.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.mobina.cocorun.R;
import com.mobina.cocorun.core.BluetoothService;
import com.mobina.cocorun.core.GameSurface;
import com.mobina.cocorun.core.GameThread;
import com.mobina.cocorun.utils.GameConfig;

import java.util.Set;

public class MainActivity extends Activity implements Runnable {

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

  ListView lv_paired_devices;
  Set<BluetoothDevice> pairedDevices;
  ArrayAdapter adapterPairedDevices;
  static GameSurface gameSurface;

  private static boolean running;
  private SurfaceHolder surfaceHolder;

  static GameConfig.COMMAND command = GameConfig.COMMAND.R;
  static int intensity = 1;

  static long timestamp = -1;

  private Thread thread;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set fullscreen
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    // Set No Title
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    gameSurface = new GameSurface(this);
    this.setContentView(gameSurface);
    surfaceHolder = gameSurface.getHolder();
    thread = new Thread(this);
    thread.start();
    MainActivity.setRunning(true);

    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    // If the adapter is null, then Bluetooth is not supported
    if (mBluetoothAdapter == null) {
      Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
      finish();
      return;
    }

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
        MainActivity.setRunning(true);
        Toast.makeText(getApplicationContext(),"device choosen "+device.getName(),Toast.LENGTH_SHORT).show();
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
    this.runOnUiThread(new Runnable() {
      public void run() {
        showDialog();
      }
    });
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
      Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
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
//          Toast.makeText(MainActivity.this, readMessage, Toast.LENGTH_SHORT).show();
          MainActivity.getCommand(readMessage);
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

  public static void getCommand(String msg) {
    String dir = String.valueOf(msg.charAt(0));
    intensity = Integer.valueOf(String.valueOf(msg.charAt(1)));
    command = dir == "L" ? GameConfig.COMMAND.L : GameConfig.COMMAND.R;
    timestamp = System.currentTimeMillis();
  }

  @Override
  public void run()  {
    long startTime = System.nanoTime();

    while(running)  {
      Canvas canvas = null;
      try {
        canvas = this.surfaceHolder.lockCanvas();

        synchronized (canvas)  {
          this.gameSurface.processCommand(command, intensity, timestamp);
          this.gameSurface.update();
          this.gameSurface.draw(canvas);
        }
      } catch (Exception e)  {
      } finally {
        if (canvas != null)  {
          this.surfaceHolder.unlockCanvasAndPost(canvas);
        }
      }
      long now = System.nanoTime() ;
      long waitTime = (now - startTime)/1000000;
      if (waitTime < GameConfig.SCREEN_REFRESH_INTERVAL)  {
        waitTime = GameConfig.SCREEN_REFRESH_INTERVAL;
      }
      System.out.print(" Wait Time="+ waitTime);

      try {
        thread.sleep(waitTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      startTime = System.nanoTime();
      System.out.print(".");
    }
  }

  public static void setRunning(boolean running)  {
    MainActivity.running = running;
  }
}

