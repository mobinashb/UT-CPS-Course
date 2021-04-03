package com.mobina.legendofbounca.activity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.mobina.legendofbounca.R;
import com.mobina.legendofbounca.core.config.GameConfig;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        TextView tx = findViewById(R.id.main_title);
        Typeface customFont = Typeface.createFromAsset(getAssets(),
            "whatever_it_takes.ttf");
        tx.setTypeface(customFont);

        Button gyroscopeButton = findViewById(R.id.button_gyroscope);
        Button gravityButton = findViewById(R.id.button_gravity);
        gyroscopeButton.setTypeface(customFont);
        gravityButton.setTypeface(customFont);

        gyroscopeButton.setOnClickListener(new View.OnClickListener() {
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

        gravityButton.setOnClickListener(new View.OnClickListener() {
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
