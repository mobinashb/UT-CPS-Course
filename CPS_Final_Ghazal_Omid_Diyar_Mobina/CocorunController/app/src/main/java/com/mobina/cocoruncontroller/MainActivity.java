package com.mobina.cocoruncontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class MainActivity extends Activity {

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
        try {
          Intent intent = new Intent(getApplicationContext(), GameControlActivity.class);
          startActivity(intent);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }
}
