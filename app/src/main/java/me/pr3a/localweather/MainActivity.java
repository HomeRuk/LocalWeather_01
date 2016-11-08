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

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

import me.pr3a.localweather.Helper.MyAlertDialog;
import me.pr3a.localweather.Helper.UrlApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private TextView weatherIcon;
    private static final String url1 = "http://www.doofon.me/weather/";
    private static final String url2 = "http://www.doofon.me/device/update/FCMtoken";
    private static final String FILENAME = "data.txt";
    private final static int READ_BLOCK_SIZE = 100;
    private final UrlApi urlApi1 = new UrlApi();
    private final UrlApi urlApi2 = new UrlApi();
    private final MyAlertDialog dialog = new MyAlertDialog();
    private String sid = "Ruk";

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

        //Show Toolbar
        this.showToolbar("DooFon", "");
        //Show DrawerLayout and drawerToggle
        this.initInstances();

        this.readData();

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("TOKEN", "=" + token);
        this.updateToken(urlApi2.getApikey(), token);
        // Connect loadJson choice 1 setTime
        this.conLoadJSON(1);
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
            Toast.makeText(this, "Refresh Weather", Toast.LENGTH_SHORT).show();
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
                finish();
                startActivity(getIntent());
                break;
            case R.id.nav_DeviceProfile:
                finish();
                Intent intentDevice = new Intent(this, DeviceActivity.class);
                startActivity(intentDevice);
                break;
            case R.id.nav_location:
                finish();
                Intent intentLocation = new Intent(this, LocationActivity.class);
                startActivity(intentLocation);
                break;
            case R.id.nav_setting:
                finish();
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivity(intentSettings);
                break;
            case R.id.nav_disconnect:
                this.updateToken(urlApi2.getApikey(), "0");
                try {
                    FileOutputStream fOut = openFileOutput(FILENAME, MODE_PRIVATE);
                    OutputStreamWriter writer = new OutputStreamWriter(fOut);
                    writer.write("");
                    writer.flush();
                    writer.close();

                    Toast.makeText(this, "Disconnect Device", Toast.LENGTH_SHORT).show();
                    Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Button back
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else {
            android.os.Process.killProcess(android.os.Process.myPid());
            finish();
            super.onBackPressed();
        }
    }

    // Button Disconnect
    public void onClickDisconnect(View view) {
        this.updateToken(urlApi2.getApikey(), "0");
        try {
            FileOutputStream fOut = openFileOutput(FILENAME, MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fOut);
            writer.write("");
            writer.flush();
            writer.close();

            Toast.makeText(this, "Disconnect Device", Toast.LENGTH_SHORT).show();
            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Show Toolbar
    private void showToolbar(String title, String subTitle) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        toolbar.setSubtitle(subTitle);
        setSupportActionBar(toolbar);
    }

    // Show DrawerLayout and drawerToggle
    private void initInstances() {
        // NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // DrawerLayout
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
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
                        new LoadJSON().execute(urlApi1.getUri());
                        // Parallel
                        // new LoadJSON().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,urlApi.getUrl());
                    }
                };
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(taskNew, 5 * 100, 300 * 1000);
            } else {
                new LoadJSON().execute(urlApi1.getUri());
            }
        } else {
            dialog.showProblemDialog(this, "Problem", "Not Connected Network");
        }
    }

    // Check Connect Network
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    // Update Token FCM
    private void updateToken(String Apikey, String token) {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("SerialNumber", Apikey + "")
                    .add("FCMtoken", token + "")
                    .add("sid", sid)
                    .build();
            Request request = new Request.Builder()
                    .url(urlApi2.getUrl())
                    .post(formBody)
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);
                }
            });
            Toast.makeText(this, "UPDATE Token", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read SerialNumber
    private void readData() {
        try {
            FileInputStream fIn = openFileInput(FILENAME);
            InputStreamReader reader = new InputStreamReader(fIn);

            char[] buffer = new char[READ_BLOCK_SIZE];
            String data = "";
            int charReadCount;
            while ((charReadCount = reader.read(buffer)) > 0) {
                String readString = String.copyValueOf(buffer, 0, charReadCount);
                data += readString;
                buffer = new char[READ_BLOCK_SIZE];
            }
            reader.close();
            if (!(data.equals(""))) {
                //Set url & LoadJSON
                urlApi1.setUri(url1, data);
                urlApi2.setUri(url2, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                FileOutputStream fOut = openFileOutput(FILENAME, MODE_PRIVATE);
                OutputStreamWriter writer = new OutputStreamWriter(fOut);
                writer.write("");
                writer.flush();
                writer.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    // AsyncTask Load Data Weather
    private class LoadJSON extends AsyncTask<String, Void, String> {

        private final TextView statusUpdate = (TextView) findViewById(R.id.textview_statusUpdate);
        private final TextView weatherStatusName = (TextView) findViewById(R.id.weather_statusName);
        private final TextView weatherTemp = (TextView) findViewById(R.id.weather_temperature);
        private final TextView weatherHumidity = (TextView) findViewById(R.id.weather_humidity);
        private final TextView weatherPressure = (TextView) findViewById(R.id.weather_pressure);
        private final TextView weatherDewPoint = (TextView) findViewById(R.id.weather_dewpoint);
        private final TextView weatherLight = (TextView) findViewById(R.id.weather_light);
        private final TextView weatherProbabilityRain = (TextView) findViewById(R.id.weather_probabilityRain);
        //private final TextView deviceSerialNumber = (TextView) findViewById(R.id.main_serialNumber);

        private String temp = "";
        private String humidity = "";
        private String dewPoint = "";
        private String pressure = "";
        private String light = "";
        private String rain = "";
        private String updated_at = "";
        private String icon;
        //private String SerialNumber = "";
        private String probabilityRain = "";
        private double tempDouble;
        private int rainInt, timeInt;

        @Override
        protected String doInBackground(String... urls) {
            Log.d("APP", "doInBackground");
            OkHttpClient okHttpClient = new OkHttpClient();
            Request.Builder builder = new Request.Builder();
            Request request = builder.url(urls[0]).build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    return "Not Success - code : " + response.code();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Error - " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
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
                probabilityRain += String.format("%s", json.getString("PredictPercent"));
                //SerialNumber += String.format("%s", json.getString("SerialNumber"));

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
                if ((probabilityRain != null) && (probabilityRain.equals("")) && (probabilityRain.equals("null"))) {
                    weatherProbabilityRain.setText(String.format("Probability Rain: %s %%", probabilityRain));
                }
                //deviceSerialNumber.setText(String.format("%s", SerialNumber));

            } catch (JSONException e) {
                dialog.showProblemDialog(MainActivity.this, "Problem", "Data Not Found\nPlease Check Device");
                e.printStackTrace();
            } catch (Exception e) {
                dialog.showProblemDialog(MainActivity.this, "Problem", "Program Stop");
                e.printStackTrace();
            }
        }
    }
}