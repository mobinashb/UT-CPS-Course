package com.mobina.legendofbounca.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.mobina.legendofbounca.R;
import com.mobina.legendofbounca.util.GameConfig;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Button buttonGyro = findViewById(R.id.button_gyroscope);
        Button buttonGravity = findViewById(R.id.button_gravity);

        buttonGyro.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            try {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("sensor", GameConfig.sensor.GYROSCOPE);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        });

        buttonGravity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            try {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("sensor", GameConfig.sensor.GRAVITY);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        });

    }

}
