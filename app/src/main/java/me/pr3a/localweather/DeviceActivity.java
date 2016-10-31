package me.pr3a.localweather;

import android.content.Context;
import android.content.Intent;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import me.pr3a.localweather.Helper.UrlApi;
import me.pr3a.localweather.Helper.MyAlertDialog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeviceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private final static String url = "http://www.doofon.me/device/";
    private static final String FILENAME = "data.txt";
    private final static int READ_BLOCK_SIZE = 100;
    private final UrlApi urlApi = new UrlApi();
    private final MyAlertDialog dialog = new MyAlertDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        //Display Toolbar
        this.showToolbar("My Device", "");
        //Show DrawerLayout and drawerToggle
        this.initInstances();

        if (isNetworkConnected()) {
            this.readData();
            //LoadJSON
            new LoadJSON2().execute(urlApi.getUri());
        } else {
            dialog.showProblemDialog(DeviceActivity.this, "Problem", "Not Connected Network");
        }
        /*SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy:MM:dd");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        System.out.println(dateFormatGmt.format(new Date()) + "");*/
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
                Intent intentDevice = new Intent(this, MainActivity.class);
                startActivity(intentDevice);
                break;
            case R.id.nav_DeviceProfile:
                finish();
                startActivity(getIntent());
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

    //Button back
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

    //Show Toolbar
    private void showToolbar(String title, String subTitle) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        toolbar.setSubtitle(subTitle);
        setSupportActionBar(toolbar);
    }

    //Show DrawerLayout and drawerToggle
    private void initInstances() {
        // NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    // Check Connect Network
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
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
                urlApi.setUri(url, data);
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

    // AsyncTask Load Data Device
    private class LoadJSON2 extends AsyncTask<String, Void, String> {

        private final TextView deviceSerialNumber = (TextView) findViewById(R.id.txt_SerialNumber);
        private final TextView deviceLatitude = (TextView) findViewById(R.id.txt_Latitude);
        private final TextView deviceLongitude = (TextView) findViewById(R.id.txt_Longitude);
        private final TextView deviceThreshold = (TextView) findViewById(R.id.txt_Threshold);
        private final TextView deviceCreate = (TextView) findViewById(R.id.txt_Create);
        private final TextView deviceUpdated = (TextView) findViewById(R.id.txt_Updated);

        @Override
        protected String doInBackground(String... urls) {
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
            super.onPostExecute(result);

            try {
                JSONObject json = new JSONObject(result);
                //System.out.println(result);
                String Serial = String.format("%s", json.getString("SerialNumber"));
                String latitude = String.format("%s", json.getString("latitude"));
                String longitude = String.format("%s", json.getString("longitude"));
                String threshold = String.format("%s", json.getString("threshold"));
                String created_at = String.format("%s", json.getString("created_at"));
                String updated_at = String.format("%s", json.getString("updated_at"));

                deviceSerialNumber.setText(String.format("%s", Serial));
                deviceLatitude.setText(String.format("%s", latitude));
                deviceLongitude.setText(String.format("%s", longitude));
                deviceThreshold.setText(String.format("%s", threshold));
                deviceCreate.setText(String.format("%s", created_at));
                deviceUpdated.setText(String.format("%s", updated_at));
            } catch (Exception e) {
                dialog.showProblemDialog(DeviceActivity.this, "Problem", "Read Data fail");
                e.printStackTrace();
            }
        }
    }

}
