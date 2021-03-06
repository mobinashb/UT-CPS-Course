package com.mobina.cocorun.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.mobina.cocorun.R;
import com.mobina.cocorun.core.Game.GameSurface;
import com.mobina.cocorun.utils.GameConfig;
import com.mobina.cocorun.utils.Helper;

public class MainActivity extends FragmentActivity implements Runnable {

  static GameSurface gameSurface;

  private static boolean running;
  private SurfaceHolder surfaceHolder;

  static GameConfig.COMMAND command = GameConfig.COMMAND.R;
  static int intensity = 1;

  static long timestamp = -1;

  private Thread thread;

  private static MainActivity instance;

  private String mode;

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

    Helper.overrideFonts(getApplicationContext(), findViewById(R.id.main));

  }


  public void setupGame(){
    gameSurface = new GameSurface(this);
    this.setContentView(gameSurface);
    surfaceHolder = gameSurface.getHolder();
    thread = new Thread(this);
    thread.start();
    MainActivity.setRunning(true);
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
        MainActivity.this.mode = "W";
        runWifiFragment();
      }
    });

    Button bluetoothButton = findViewById(R.id.button_bluetooth);
    bluetoothButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        MainActivity.this.mode = "B";
        runBluetoothFragment();
      }
    });
  }

  public void hitWallNotif(){
    if(this.mode.equals("W"))
      WifiFragment.getInstance().sendVibration();
    else
      BluetoothFragment.getInstance().sendVibration();
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

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  public static void getCommand(String msg) {
    if(msg == null)
      return;
    if(msg.length() < 2)
      return;
    if (msg.contains("N"))
      return;
    String dir = String.valueOf(msg.charAt(0));
    intensity = Integer.parseInt(String.valueOf(msg.charAt(1)));
    command = dir.equals("L") ? GameConfig.COMMAND.L : GameConfig.COMMAND.R;
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
      try {
        thread.sleep(waitTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      startTime = System.nanoTime();
    }
  }

  public static void setRunning(boolean running)  {
    MainActivity.running = running;
  }
}

