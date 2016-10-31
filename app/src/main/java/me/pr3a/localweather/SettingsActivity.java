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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import me.pr3a.localweather.Helper.MyAlertDialog;
import me.pr3a.localweather.Helper.UrlApi;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private final static String FILENAME = "data.txt";
    private final static String url1 = "http://www.doofon.me/device/";
    private final static String url2 = "http://www.doofon.me/device/update/threshold";
    private final UrlApi urlApi1 = new UrlApi();
    private final UrlApi urlApi2 = new UrlApi();
    private final static int READ_BLOCK_SIZE = 100;
    private final MyAlertDialog dialog = new MyAlertDialog();
    private TextView txtSeekBar;
    private SeekBar seekBar;
    private int progressChanged = 0;
    private String sid = "Ruk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //Display Toolbar
        this.showToolbar("Setting", "Predict Threshold");
        //Show DrawerLayout and drawerToggle
        this.initInstances();

        txtSeekBar = (TextView) findViewById(R.id.textView_seekBar);
        if (isNetworkConnected()) {
            this.readData();
            new LoadJSON2().execute(urlApi1.getUri());
        } else {
            dialog.showProblemDialog(SettingsActivity.this, "Problem", "Not Connected Network");
        }

        this.onSeekBar();
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
                Intent intentLocation = new Intent(this, DeviceActivity.class);
                startActivity(intentLocation);
                break;
            case R.id.nav_location:
                finish();
                Intent intentSettings = new Intent(this, LocationActivity.class);
                startActivity(intentSettings);
                break;
            case R.id.nav_setting:
                finish();
                startActivity(getIntent());
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

    // Button Save threshold
    public void onButtonSave(View view) {
        //Check serial is not empty
        if (progressChanged != 0) {
            //Check Connect network
            if (isNetworkConnected()) {
                view.setEnabled(false);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        Log.d("APP", "doInBackground");
                        try {
                            RequestBody formBody = new FormBody.Builder()
                                    .add("SerialNumber", urlApi2.getApikey())
                                    .add("threshold", progressChanged + "")
                                    .add("sid", sid)
                                    .build();
                            Request request = new Request.Builder()
                                    .url(urlApi2.getUrl())
                                    .post(formBody)
                                    .build();
                            OkHttpClient okHttpClient = new OkHttpClient();
                            okHttpClient.newCall(request).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                            dialog.showProblemDialog(SettingsActivity.this, "Problem", "Save Fail");
                        }
                        return null;
                    }
                }.execute();
                dialog.showConnectDialog(SettingsActivity.this, "Save", "Success");
                view.setEnabled(true);
            } else dialog.showProblemDialog(this, "Problem", "Not Connected Network");
        } else Toast.makeText(this, "Please Select threshold", Toast.LENGTH_SHORT).show();
    }

    // SeekBar
    private void onSeekBar() {
        seekBar = (SeekBar) findViewById(R.id.seek_Bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
                txtSeekBar.setText("Threshold : " + progressChanged + "%");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(SettingsActivity.this, "Threshold : " + progressChanged + "%", Toast.LENGTH_SHORT).show();
                System.out.println(progressChanged);
            }
        });
    }

    // Check Connect Network
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
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
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    //Read SerialNumber
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
            if (data.equals("")) dialog.showProblemDialog(this, "Problem", "Data Empty");
            else {
                //Set url
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

    // AsyncTask Load Data Device
    private class LoadJSON2 extends AsyncTask<String, Void, String> {

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
            Log.d("APP", "onPostExecute");
            super.onPostExecute(result);
            try {
                JSONObject json = new JSONObject(result);
                String threshold = String.format("%s", json.getString("threshold"));
                txtSeekBar.setText(String.format("Threshold : %s", threshold));
                seekBar.setProgress(Integer.parseInt(threshold));
            } catch (Exception e) {
                dialog.showProblemDialog(SettingsActivity.this, "Problem", "Not Connected Internet");
                e.printStackTrace();
            }
        }
    }
}
