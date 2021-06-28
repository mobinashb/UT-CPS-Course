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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobina.cocorun.R;
import com.mobina.cocorun.core.Bluetooth.BluetoothService;
import com.mobina.cocorun.core.BluetoothFragment;
import com.mobina.cocorun.core.GameSurface;
import com.mobina.cocorun.utils.GameConfig;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements Runnable {

  static GameSurface gameSurface;

  private static boolean running;
  private SurfaceHolder surfaceHolder;

  static GameConfig.COMMAND command = GameConfig.COMMAND.R;
  static int intensity = 1;

  static long timestamp = -1;

  private Thread thread;

  private static MainActivity instance;

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
    ImageView playButton = findViewById(R.id.button_play);
    playButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
            setFragment();
//        setActivity();
      }
    });
  }
  private void setFragment() {
    getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragment_wifi, WifiActivity.class, null)
            .commit();
  }

  private void setActivity(){
    System.out.println("HKHKHK");
    Intent intent = new Intent(MainActivity.this, WifiActivity.class);
    startActivity(intent);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  public static void getCommand(String msg) {
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

