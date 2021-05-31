package com.mobina.cocoruncontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class GameControlActivity extends BluetoothService {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set fullscreen
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    // Set No Title
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);

    setContentView(R.layout.activity_game_control);

    ImageView rightButton = findViewById(R.id.button_right);
    ImageView leftButton = findViewById(R.id.button_left);

    rightButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        try {
          System.out.println("Right");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    leftButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        try {
          System.out.println("Left");
          sendMessage("L6");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    initialize();

  }

}