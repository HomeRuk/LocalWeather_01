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

import org.json.JSONObject;

import me.pr3a.localweather.Helper.UrlApi;
import me.pr3a.localweather.Helper.MyAlertDialog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeviceActivity extends AppCompatActivity {

    private final static String url = "http://128.199.210.91/weather/";
    private final UrlApi urlApi = new UrlApi();
    private final MyAlertDialog dialog = new MyAlertDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        //Display Toolbar
        this.showToolbar("Device", "");

        /*SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy:MM:dd");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        System.out.println(dateFormatGmt.format(new Date()) + "");*/

        if (isNetworkConnected()) {
            String PSerialNumber;
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                PSerialNumber = bundle.getString("P_SerialNumber");
                //Set url & LoadJSON
                urlApi.setUri(url, PSerialNumber);
                new LoadJSON2().execute(urlApi.getUrl());
            }
        } else {
            dialog.showProblemDialog(DeviceActivity.this, "Problem", "Not Connected Network");
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

        private final TextView deviceSerialNumber = (TextView) findViewById(R.id.txt_SerialNumber);

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
                String Serial = String.format("%s", json.getString("SerialNumber"));
                deviceSerialNumber.setText(String.format("%s", Serial));
            } catch (Exception e) {
                dialog.showProblemDialog(DeviceActivity.this, "Problem", "Not Connected Internet");
                e.printStackTrace();
            }
        }
    }
}
