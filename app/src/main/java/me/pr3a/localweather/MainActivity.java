package me.pr3a.localweather;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    Toolbar toolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    TextView weatherIcon;
    Typeface weatherFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set fond
        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weather.ttf");
        weatherIcon = (TextView) findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(weatherFont);

        this.initToolbar();
        this.initInstances();
        // NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        String url = "http://128.199.210.91/weather";
        final UrlApi uriapi01 = new UrlApi();
        uriapi01.setUri(url);
        Timer timer = new Timer();
        TimerTask tasknew = new TimerTask() {
            public void run() {
                LoadJSON task = new LoadJSON();
                task.execute(uriapi01.getUrl());
            }
        };
        timer.scheduleAtFixedRate(tasknew, 5 * 100, 360 * 1000);

    }

    private class UrlApi {
        protected String url;

        protected void setUri(String url) {
            this.url = url;
        }

        protected String getUrl() {
            return url;
        }
    }

    private class LoadJSON extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return getText(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {

            String temp = "";
            String humidity = "";
            String dewpoint = "";
            String pressure = "";
            String light = "";
            String rain = "";
            String updated_at = "";
            String icon ;
            String SerialNumber ="";
            try {
                JSONObject json = new JSONObject(result);

                temp += String.format("%s", json.getString("temp"));
                humidity += String.format("%s", json.getString("humidity"));
                dewpoint += String.format("%s", json.getString("dewpoint"));
                pressure += String.format("%s", json.getString("pressure"));
                light += String.format("%s", json.getString("light"));
                rain += String.format("%s", json.getString("rain"));
                updated_at += String.format("%s", json.getString("updated_at"));
                SerialNumber += String.format("%s", json.getString("SerialNumber"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            TextView serialNumber = (TextView) findViewById(R.id.textview_SerialNumber);
            serialNumber.setText("SerialNumber : " + SerialNumber);

            TextView statusUpdate = (TextView) findViewById(R.id.textview_statusUpdate);
            statusUpdate.setText("Last update " + updated_at);

            TextView weatherTemp = (TextView) findViewById(R.id.weather_temperature);
            weatherTemp.setText(temp + " ℃");

            TextView weatherHumidity = (TextView) findViewById(R.id.weather_humidity);
            weatherHumidity.setText("Humidity: " + humidity + " %");

            TextView weatherPressure = (TextView) findViewById(R.id.weather_pressure);
            weatherPressure.setText("Pressure: " + pressure);

            TextView weatherDewpoint = (TextView) findViewById(R.id.weather_dewpoint);
            weatherDewpoint.setText("DewPoint: " + dewpoint + " ℃");

            TextView weatherLight = (TextView) findViewById(R.id.weather_light);
            weatherLight.setText("Light: " + light);

            double tempdouble = Double.parseDouble(temp);

            if(tempdouble >=35.0){
                icon = getString(R.string.weather_hot);
                weatherIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 150);
                weatherIcon.setText(icon);
            }else if(tempdouble>=18.1){
                icon = getString(R.string.weather_sunny);
                weatherIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 150);
                weatherIcon.setText(icon);
            }else if(tempdouble <=18){
                icon = getString(R.string.weather_cold);
                weatherIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 200);
                weatherIcon.setText(icon);
            }
        }
    }

    private String getText(String strUrl) {
        String strResult = "";
        try {
            URL url = new URL(strUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            strResult = readStream(con.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strResult;
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("LocalWeatherNow");
        //toolbar.setSubtitle("สภาวะอากาศปัจจุบัน");
        setSupportActionBar(toolbar);
    }

    private void initInstances() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        //drawerToggle.syncState();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        String url = "http://128.199.210.91/weather";
        final UrlApi uriapi01 = new UrlApi();
        uriapi01.setUri(url);

        if (id == R.id.action_refresh) {
            LoadJSON task = new LoadJSON();
            task.execute(uriapi01.getUrl());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String icon;

        switch (id) {
            case R.id.nav_profile:
                /*icon = getString(R.string.weather_cold);
                weatherIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 150);
                weatherIcon.setText(icon);*/
                break;
            case R.id.nav_setting:
                /*icon = getString(R.string.weather_hot);
                weatherIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 150);
                weatherIcon.setText(icon);*/
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
