package me.pr3a.localweather;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class LogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        //set fond
        Typeface weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weather.ttf");
        TextView weatherIcon = (TextView) findViewById(R.id.logo);
        weatherIcon.setTypeface(weatherFont);
        weatherIcon.setText(getString(R.string.weather_rain));

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                /*Intent intent = new Intent(LogoActivity.this, MainActivity.class);
                startActivity(intent);*/
                startActivity(new Intent(LogoActivity.this, ConnectDeviceActivity.class));
                finish();
            }
        }, 1500);
    }
}
