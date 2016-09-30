package me.pr3a.localweather;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import me.pr3a.localweather.Helper.MyAlertDialog;
import me.pr3a.localweather.Helper.UrlApi;

public class LogoActivity extends AppCompatActivity {

    private UrlApi urlApi = new UrlApi();
    private MyAlertDialog dialog = new MyAlertDialog();
    private final static String FILENAME = "data.txt";
    private final static String url = "http://128.199.210.91/device/";
    private final static int READ_BLOCK_SIZE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        //set fond
        Typeface weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weather.ttf");
        TextView weatherIcon = (TextView) findViewById(R.id.logo);
        weatherIcon.setTypeface(weatherFont);
        weatherIcon.setText(getString(R.string.weather_rain));

        if (isNetworkConnected()) {
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
                    new LoadJSON0().execute(urlApi.getUrl());
                } else {
                    intentDelay();
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
                intentDelay();
            }
        }else{
            dialog.showProblemDialog(LogoActivity.this, "Problem", "Not Connected Network");
        }
    }

    private void intentDelay() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LogoActivity.this, ConnectDeviceActivity.class));
                finish();
            }
        }, 1500);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private class LoadJSON0 extends AsyncTask<String, Void, String> {

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
                dialog.showProblemDialog(LogoActivity.this, "Problem", "Not Connected Network");
            }
            return strResult;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("APP", "onPostExecute");
            try {
                JSONObject json = new JSONObject(result);
                String Serial = String.format("%s", json.getString("SerialNumber"));
                if (Serial != null) {
                    Intent intent = new Intent(LogoActivity.this, MainActivity.class);
                    intent.putExtra("Data_SerialNumber", Serial);
                    startActivity(intent);
                    finish();
                } else {
                    dialog.showConnectDialog(LogoActivity.this, "Connect", "Connect Unsuccess1");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                try {
                    //Writer Data Serial
                    FileOutputStream fOut = openFileOutput(FILENAME, MODE_PRIVATE);
                    OutputStreamWriter writer = new OutputStreamWriter(fOut);
                    writer.write("");
                    writer.flush();
                    writer.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                dialog.showConnectDialog(LogoActivity.this, "Connect", "Connect Unsuccess2");
                /*startActivity(new Intent(LogoActivity.this, ConnectDeviceActivity.class));
                finish();*/
            } catch (Exception e) {
                e.printStackTrace();
                dialog.showProblemDialog(LogoActivity.this, "Problem", "Program Stop");
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
