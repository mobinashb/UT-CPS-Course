package com.mobina.cocorun.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.mobina.cocorun.R;
import com.mobina.cocorun.utils.GameConfig;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BluetoothService {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set fullscreen
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    // Set No Title
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);

    setContentView(R.layout.activity_main);

    initialize();

    ImageView playButton = findViewById(R.id.button_play);

    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        receiveMessage();
        if (readMessage != null)
            System.out.println(readMessage);
      }
    }, 0, GameConfig.SCREEN_REFRESH_INTERVAL);

    playButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        try {
//          receiveMessage();
//          System.out.println(readMessage);
          Intent intent = new Intent(getApplicationContext(), GameActivity.class);
          startActivity(intent);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }
}

