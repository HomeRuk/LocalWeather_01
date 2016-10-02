package me.pr3a.localweather;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import me.pr3a.localweather.Helper.MyAlertDialog;
import me.pr3a.localweather.Helper.UrlApi;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private TextView weatherIcon;
    private static final String url = "http://128.199.210.91/weather/";
    private static final String FILENAME = "data.txt";
    private UrlApi urlApi = new UrlApi();
    private MyAlertDialog dialog = new MyAlertDialog();
    private String DataSerialNumber = "";

    // Event onStop
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("APP", "onDestroy");
    }

    // Event onStop
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("APP", "onStop");
    }

    // Event onResume
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("APP", "onResume");
    }

    // Event onPause
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("APP", "onPause");
    }

    // Event onStart
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("APP", "onStart");
    }

    // Event onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("APP", "OnCreate");
        //set fond
        Typeface weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weather.ttf");
        weatherIcon = (TextView) findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(weatherFont);

        this.initToolbar();
        this.initInstances();

        // NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            DataSerialNumber = bundle.getString("Data_SerialNumber");
            //set url
            urlApi.setUri(url, DataSerialNumber);
        }

        // Connect loadJson choice 1 setTime
        this.conLoadJSON(1);
    }

    public void onClickDisconnect(View view) {
        try {
            FileOutputStream fOut = openFileOutput(FILENAME, MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fOut);
            writer.write("");
            writer.flush();
            writer.close();

            Toast.makeText(MainActivity.this, "Disconnect Device", Toast.LENGTH_SHORT).show();
            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // SyncState icon draweToggle
    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.d("APP", "onPostCreate");
        drawerToggle.syncState();
    }

    // Create MenuBar on Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_toolbar, menu);
        return true;
    }

    // Click button refresh
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            conLoadJSON(0);
        }
        return super.onOptionsItemSelected(item);
    }

    // Select Menu Navigation
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_main:
               /* Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);*/
                finish();
                startActivity(getIntent());
                break;
            case R.id.nav_DeviceProfile:
                if (DataSerialNumber != null) {
                    Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
                    intent.putExtra("P_SerialNumber", DataSerialNumber);
                    startActivity(intent);
                    //finish();
                }
                break;
            case R.id.nav_location:
                break;
            case R.id.nav_setting:
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Show  Toolbar
    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("LocalWeatherNow");
        //toolbar.setSubtitle("สภาวะอากาศปัจจุบัน");
        setSupportActionBar(toolbar);
    }

    //Show DrawerLayout and drawerToggle
    private void initInstances() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        //drawerToggle.syncState();
    }

    // Connect Load Json
    private void conLoadJSON(int choice) {
        // Check Network Connected
        if (isNetworkConnected()) {
            // choice 1 setTime
            if (choice == 1) {
                TimerTask taskNew = new TimerTask() {
                    public void run() {
                        // series
                        new LoadJSON().execute(urlApi.getUrl());
                        // Parallel
                        // new LoadJSON().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,urlApi.getUrl());
                    }
                };
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(taskNew, 2 * 100, 300 * 1000);
            } else {
                // series
                new LoadJSON().execute(urlApi.getUrl());
            }
        } else {
            dialog.showProblemDialog(this, "Problem", "Not Connected Network1");
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    //Button back
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            finish();
    }

    private class LoadJSON extends AsyncTask<String, Void, String> {

        private TextView statusUpdate = (TextView) findViewById(R.id.textview_statusUpdate);
        private TextView weatherStatusName = (TextView) findViewById(R.id.weather_statusName);
        private TextView weatherTemp = (TextView) findViewById(R.id.weather_temperature);
        private TextView weatherHumidity = (TextView) findViewById(R.id.weather_humidity);
        private TextView weatherPressure = (TextView) findViewById(R.id.weather_pressure);
        private TextView weatherDewPoint = (TextView) findViewById(R.id.weather_dewpoint);
        private TextView weatherLight = (TextView) findViewById(R.id.weather_light);
        private TextView deviceSerialNumber = (TextView) findViewById(R.id.device_serialNumber);

        private String temp = "";
        private String humidity = "";
        private String dewPoint = "";
        private String pressure = "";
        private String light = "";
        private String rain = "";
        private String updated_at = "";
        // private String SerialNumber = "";
        private String icon;
        private double tempDouble;
        private int rainInt, timeInt;

        @Override
        protected String doInBackground(String... urls) {
            Log.d("APP", "doInBackground");
            String strResult = "";
            if (isNetworkConnected()) {
                try {
                    URL url = new URL(urls[0]);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    strResult = readStream(con.getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                dialog.showProblemDialog(MainActivity.this, "Problem", "Not Connected Network");
            }
            return strResult;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("APP", "onPostExecute");
            try {
                JSONObject json = new JSONObject(result);
                temp += String.format("%s", json.getString("temp"));
                humidity += String.format("%s", json.getString("humidity"));
                dewPoint += String.format("%s", json.getString("dewpoint"));
                pressure += String.format("%s", json.getString("pressure"));
                light += String.format("%s", json.getString("light"));
                rain += String.format("%s", json.getString("rain"));
                updated_at += String.format("%s", json.getString("updated_at"));
                String time = updated_at.substring(11, 13);

                tempDouble = Double.parseDouble(temp);
                rainInt = Integer.parseInt(rain);
                timeInt = Integer.parseInt(time);
                if (rainInt == 1) {
                    if (timeInt >= 6 && timeInt < 18) {
                        icon = getString(R.string.weather_day_rain);
                        weatherIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 120);
                        weatherIcon.setText(icon);
                        weatherStatusName.setText(R.string.Text_Daytime_Rain);
                    } else {
                        icon = getString(R.string.weather_night_rain);
                        weatherIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 140);
                        weatherIcon.setText(icon);
                        weatherStatusName.setText(R.string.Text_Nighttime_Rain);
                    }
                } else if (tempDouble >= 35.0) {
                    icon = getString(R.string.weather_hot);
                    weatherIcon.setText(icon);
                    weatherStatusName.setText(R.string.Text_hot);
                } else if (tempDouble > 22.9) {
                    if (timeInt >= 6 && timeInt < 18) {
                        icon = getString(R.string.weather_sunny);
                        weatherIcon.setText(icon);
                        weatherStatusName.setText(R.string.Text_Daytime_Neutral);
                    } else {
                        icon = getString(R.string.weather_night_clear);
                        weatherIcon.setText(icon);
                        weatherStatusName.setText(R.string.Text_Nighttime_Neutral);
                    }
                } else if (tempDouble <= 22.9) {
                    icon = getString(R.string.weather_cold);
                    weatherIcon.setText(icon);
                    weatherStatusName.setText(R.string.Text_cold);
                }

                statusUpdate.setText(String.format("Last update %s", updated_at));
                weatherTemp.setText(String.format("%s ℃", tempDouble));
                weatherHumidity.setText(String.format("Humidity: %s %%", humidity));
                weatherPressure.setText(String.format("Pressure: %s", pressure));
                weatherDewPoint.setText(String.format("DewPoint: %s ℃", dewPoint));
                weatherLight.setText(String.format("Light: %s", light));
                deviceSerialNumber.setText(String.format("%s", DataSerialNumber));

            } catch (JSONException e) {
                dialog.showProblemDialog(MainActivity.this, "Problem", "Data Not Found");
                e.printStackTrace();
            } catch (Exception e) {
                dialog.showProblemDialog(MainActivity.this, "Problem", "Program Stop");
                e.printStackTrace();
            }

        }

        // Read text json first to last
        private String readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuilder sb = new StringBuilder();
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
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
    }
}