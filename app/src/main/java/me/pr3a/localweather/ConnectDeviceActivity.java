package me.pr3a.localweather;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import me.pr3a.localweather.Helper.UrlApi;
import me.pr3a.localweather.Helper.MyAlertDialog;

public class ConnectDeviceActivity extends AppCompatActivity {

    private final static String url = "http://128.199.210.91/device/";
    private UrlApi urlApi = new UrlApi();
    private MyAlertDialog dialog = new MyAlertDialog();
    private String ESerial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        Button buttonConnect = (Button) findViewById(R.id.button_connect);
        final EditText etSerial = (EditText) findViewById(R.id.serial);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ESerial = etSerial.getText().toString();
                //set url
                urlApi.setUri(url, ESerial);
                // LoadJSON
                new LoadJSON1().execute(urlApi.getUrl());
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private class LoadJSON1 extends AsyncTask<String, Void, String> {

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
                dialog.showProblemDialog(ConnectDeviceActivity.this, "Problem", "Not Connected Network");
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
                    Intent intent = new Intent(ConnectDeviceActivity.this, MainActivity.class);
                    intent.putExtra("Data_SerialNumber", Serial);
                    startActivity(intent);
                    finish();
                } else {
                    dialog.showConnectDialog(ConnectDeviceActivity.this, "Connect", "Connect Unsuccess1");
                }
            } catch (JSONException e) {
                dialog.showConnectDialog(ConnectDeviceActivity.this, "Connect", "Connect Unsuccess2");
                e.printStackTrace();
            } catch (Exception e) {
                dialog.showProblemDialog(ConnectDeviceActivity.this, "Problem", "Program Stop");
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
