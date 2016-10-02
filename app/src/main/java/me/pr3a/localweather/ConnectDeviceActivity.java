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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
//import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import me.pr3a.localweather.Helper.UrlApi;
import me.pr3a.localweather.Helper.MyAlertDialog;

public class ConnectDeviceActivity extends AppCompatActivity {

    private UrlApi urlApi = new UrlApi();
    private MyAlertDialog dialog = new MyAlertDialog();
    private String serial;
    private final static String FILENAME = "data.txt";
    private final static String url = "http://128.199.210.91/device/";
    //private final static int READ_BLOCK_SIZE = 100;
    private Button buttonConnect;
    EditText editSerial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        bindWidgets();
        onButtonConnect();

        /*
        try {
            FileInputStream fIn = openFileInput(FILENAME);
            InputStreamReader reader = new InputStreamReader(fIn);
            char[] buffer = new char[READ_BLOCK_SIZE];
            String data = "";
            int charReadCount;

            while ((charReadCount = reader.read(buffer)) > 0) {
                String readString = String.copyValueOf(buffer, 0,
                        charReadCount);
                data += readString;
                buffer = new char[READ_BLOCK_SIZE];
            }
            reader.close();

            if (!(data.equals(""))) {
                //Set url & LoadJSON
                urlApi.setUri(url, data);
                new LoadJSON1().execute(urlApi.getUrl());
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        */

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void bindWidgets() {
        buttonConnect = (Button) findViewById(R.id.button_connect);
        editSerial = (EditText) findViewById(R.id.serial);
    }

    private void onButtonConnect() {
        // setOnClick
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
        });
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
                    Log.d("APP", "111111");
                    e.printStackTrace();
                    //finish();
                    dialog.showProblemDialog(ConnectDeviceActivity.this, "Problem", "Not Connected Network");
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
                    try {
                        //Writer Data Serial
                        FileOutputStream fOut = openFileOutput(FILENAME, MODE_PRIVATE);
                        OutputStreamWriter writer = new OutputStreamWriter(fOut);
                        writer.write(serial);
                        writer.flush();
                        writer.close();
                    } catch (IOException ioe) {
                        dialog.showConnectDialog(ConnectDeviceActivity.this, "Connect", "Connect Unsuccess1");
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
                    dialog.showConnectDialog(ConnectDeviceActivity.this, "Connect", "Connect Unsuccess1");
                    ioe.printStackTrace();
                }
                dialog.showConnectDialog(ConnectDeviceActivity.this, "Connect", "Connect Unsuccess2");
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
