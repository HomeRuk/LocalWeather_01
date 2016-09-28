package me.pr3a.localweather;

import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

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
    public static final String MyPREFERENCES = "MyPrefs";
    Button buttonConnect;
    SharedPreferences preferences;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("APP", "onDestroy");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        buttonConnect = (Button) findViewById(R.id.button_connect);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText serialEditText = (EditText) findViewById(R.id.serial);
                String ESerial = serialEditText.getText().toString();

                SharedPreferences.Editor editor = preferences.edit();

                editor.putString("SSerial", ESerial);
                if (editor.commit())
                    Toast.makeText(ConnectDeviceActivity.this, "Thanks", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(ConnectDeviceActivity.this, "No Thanks", Toast.LENGTH_LONG).show();

                String SCSerial = preferences.getString("SSerial", "");

                //set url
                //urlApi.setUri(url, ESerial);
                urlApi.setUri(url, SCSerial);
                new LoadJSON1().execute(urlApi.getUrl());
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("ConnectDevice Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
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
                //String ESerial = serialEditText.getText().toString();
                String Serial = String.format("%s", json.getString("SerialNumber"));
                if (Serial != null) {
                    Intent intent = new Intent(ConnectDeviceActivity.this, MainActivity.class);
                    intent.putExtra("Data_SerialNumber", Serial);
                    startActivity(intent);
                    //startActivity(new Intent(ConnectDeviceActivity.this, MainActivity.class));
                    finish();
                } else {
                    dialog.showConnectDialog(ConnectDeviceActivity.this, "Connect", "Connect Unsuccess");
                }
            } catch (JSONException e) {
                //dialog.showProblemDialog(ConnectDeviceActivity.this, "Problem", "Connect Server fail");
                dialog.showConnectDialog(ConnectDeviceActivity.this, "Connect", "Connect Unsuccess");
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
