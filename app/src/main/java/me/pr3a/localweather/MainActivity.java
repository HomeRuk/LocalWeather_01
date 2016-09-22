package me.pr3a.localweather;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
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

import me.pr3a.localweather.Server.UrlApi;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private TextView weatherIcon;
    private final String url = "http://128.199.210.91/weather";
    private UrlApi urlApi = new UrlApi();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set fond
        Typeface weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weather.ttf");
        weatherIcon = (TextView) findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(weatherFont);

        this.initToolbar();
        this.initInstances();
        // NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set url
        urlApi.setUri(url);

        // Connect loadJson choice 1 setTime
        this.conLoadJSON(1);
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
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            conLoadJSON(0);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_profile:
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

    // connect Load Json
    private void conLoadJSON(int choice) {
        // Check Network Connected
        if (isNetworkConnected()) {
            // choice 1 setTime
            if (choice == 1) {
                Timer timer = new Timer();
                TimerTask tasknew = new TimerTask() {
                    public void run() {
                        new LoadJSON().execute(urlApi.getUrl());
                    }
                };
                timer.scheduleAtFixedRate(tasknew, 5 * 100, 300 * 1000);
            } else
                new LoadJSON().execute(urlApi.getUrl());
        } else {
            showProblemDialog("Not Connected Network");
        }
    }

    // ShoeAlertProblemDialog
    private void showProblemDialog(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Problem");
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setCancelable(true);
        dialog.setMessage(message);
        dialog.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("LocalWeatherNow");
        //toolbar.setSubtitle("สภาวะอากาศปัจจุบัน");
        setSupportActionBar(toolbar);
    }

    private void initInstances() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        //drawerToggle.syncState();
    }

    private class LoadJSON extends AsyncTask<String, Void, String> {

        private TextView statusUpdate = (TextView) findViewById(R.id.textview_statusUpdate);
        private TextView weatherStatusName = (TextView) findViewById(R.id.weather_statusName);
        private TextView weatherTemp = (TextView) findViewById(R.id.weather_temperature);
        private TextView weatherHumidity = (TextView) findViewById(R.id.weather_humidity);
        private TextView weatherPressure = (TextView) findViewById(R.id.weather_pressure);
        private TextView weatherDewPoint = (TextView) findViewById(R.id.weather_dewpoint);
        private TextView weatherLight = (TextView) findViewById(R.id.weather_light);

        private String temp = "";
        private String humidity = "";
        private String dewPoint = "";
        private String pressure = "";
        private String light = "";
        private String rain = "";
        private String updated_at = "";
        private String icon;
        private double tempDouble;
        private int rainInt, timeInt;


        @Override
        protected String doInBackground(String... urls) {
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
                showProblemDialog("Not Connected Network");
            }
            return strResult;
        }

        @Override
        protected void onPostExecute(String result) {
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
                try {
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

                } catch (NumberFormatException e) {
                    showProblemDialog("Connect Server fail");
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                showProblemDialog("Not fount Data\n");
                e.printStackTrace();
            } catch (Exception e) {
                showProblemDialog("Program Stop");
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
                    //sb.append(line + "\n");
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