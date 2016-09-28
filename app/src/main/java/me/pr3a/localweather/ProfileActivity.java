package me.pr3a.localweather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

public class ProfileActivity extends AppCompatActivity {

    private final static String url = "http://128.199.210.91/weather/";
    private UrlApi urlApi = new UrlApi();
    private MyAlertDialog dialog = new MyAlertDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //Display Toolbar
        this.showToolbar("Profile", "");

        //urlApi.setUri(url,"ZgkL2LfL0Q");
        //new LoadJSON2().execute(urlApi.getUrl());

        String PSerialNumber;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            PSerialNumber = bundle.getString("P_SerialNumber");
            //set url
            urlApi.setUri(url,PSerialNumber);
            new LoadJSON2().execute(urlApi.getUrl());
        }

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void showToolbar(String title, String subTitle) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        toolbar.setSubtitle(subTitle);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private class LoadJSON2 extends AsyncTask<String, Void, String> {

        private TextView deviceSerialNumber = (TextView) findViewById(R.id.txt_SerialNumber);

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
                dialog.showProblemDialog(ProfileActivity.this, "Problem", "Not Connected Network");
            }
            return strResult;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("APP", "onPostExecute");
            try {
                JSONObject json = new JSONObject(result);
                String Serial = String.format("%s", json.getString("SerialNumber"));
                deviceSerialNumber.setText(String.format("%s", Serial));

            } catch (JSONException e) {
                //dialog.showProblemDialog(ConnectDeviceActivity.this, "Problem", "Connect Server fail");
                dialog.showConnectDialog(ProfileActivity.this, "Problem", "Data Not Found");
                e.printStackTrace();
            } catch (Exception e) {
                dialog.showProblemDialog(ProfileActivity.this, "Problem", "Program Stop");
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
