package me.pr3a.localweather;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;
//import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import me.pr3a.localweather.Helper.UrlApi;
import me.pr3a.localweather.Helper.MyAlertDialog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ConnectDeviceActivity extends AppCompatActivity {

    private UrlApi urlApi = new UrlApi();
    private MyAlertDialog dialog = new MyAlertDialog();
    private String serial;
    private final static String FILENAME = "data.txt";
    private final static String url = "http://128.199.210.91/device/";
    //private final static int READ_BLOCK_SIZE = 100;
    EditText editSerial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        bindWidgets();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void bindWidgets() {
        editSerial = (EditText) findViewById(R.id.serial);
    }

    public void onButtonConnect(View view) {
        serial = editSerial.getText().toString();
        //Check serial is not empty
        try {
            if (!serial.isEmpty()) {
                //Check Connect network
                if (isNetworkConnected()) {
                    //Set url & LoadJSON
                    urlApi.setUri(url, serial);
                    new LoadJSON1().execute(urlApi.getUrl());
                } else
                    dialog.showProblemDialog(ConnectDeviceActivity.this, "Problem", "Not Connected Network");
            } else
                Toast.makeText(ConnectDeviceActivity.this, "Please fill in Serial Number", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            dialog.showProblemDialog(ConnectDeviceActivity.this, "Problem", "Not Connected Network");
        }
    }

    private class LoadJSON1 extends AsyncTask<String, Void, String> {

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
            Log.d("APP", "onPostExecute");
            try {
                JSONObject json = new JSONObject(result);
                String Serial = String.format("%s", json.getString("SerialNumber"));
                if (Serial != null) {
                    try {
                        //Writer Data Serial
                        FileOutputStream fOut = openFileOutput(FILENAME, MODE_PRIVATE);
                        OutputStreamWriter writer = new OutputStreamWriter(fOut);
                        writer.write(serial);
                        writer.flush();
                        writer.close();
                    } catch (IOException ioe) {
                        dialog.showConnectDialog(ConnectDeviceActivity.this, "Connect", "Connect UnSuccess1");
                        ioe.printStackTrace();
                    }
                    Toast.makeText(ConnectDeviceActivity.this, "Save successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ConnectDeviceActivity.this, MainActivity.class);
                    intent.putExtra("Data_SerialNumber", Serial);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception e) {
                try {
                    //Writer Data Serial
                    FileOutputStream fOut = openFileOutput(FILENAME, MODE_PRIVATE);
                    OutputStreamWriter writer = new OutputStreamWriter(fOut);
                    writer.write("");
                    writer.flush();
                    writer.close();
                } catch (IOException ioe) {
                    dialog.showConnectDialog(ConnectDeviceActivity.this, "Connect", "Connect UnSuccess1");
                    ioe.printStackTrace();
                }
                dialog.showConnectDialog(ConnectDeviceActivity.this, "Connect", "Connect UnSuccess2");
                e.printStackTrace();
            }
        }
    }
}
