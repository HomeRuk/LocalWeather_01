package me.pr3a.localweather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import me.pr3a.localweather.Helper.MyAlertDialog;
import me.pr3a.localweather.Helper.UrlApi;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity {

    private final static String url1 = "http://www.doofon.me/device/";
    private final static String url2 = "http://www.doofon.me/device/update/threshold/";
    private final UrlApi urlApi1 = new UrlApi();
    private final UrlApi urlApi2 = new UrlApi();
    private final MyAlertDialog dialog = new MyAlertDialog();
    private TextView txtSeekBar;
    private SeekBar seekBar;
    private int progressChanged = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.showToolbar("Setting", "");
        txtSeekBar = (TextView) findViewById(R.id.textView_seekBar);

        if (isNetworkConnected()) {
            String PSerialNumber;
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                PSerialNumber = bundle.getString("P_SerialNumber");
                //Set url
                urlApi1.setUri(url1, PSerialNumber);
                urlApi2.setUri(url2, PSerialNumber);
                new LoadJSON2().execute(urlApi1.getUrl());
            } else dialog.showProblemDialog(this, "Problem", "Extras");
        } else {
            dialog.showProblemDialog(SettingsActivity.this, "Problem", "Not Connected Network");
        }

        this.onSeekBar();
    }

    public void onButtonSave(View view) {
        //Check serial is not empty
        try {
            if (progressChanged != 0) {
                //Check Connect network
                if (isNetworkConnected()) {
                    dialog.showConnectDialog(SettingsActivity.this, "Save", "Success");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            Log.d("APP", "doInBackground");
                            try {
                                RequestBody formBody = new FormBody.Builder()
                                        .add("threshold", progressChanged + "")
                                        .build();
                                Request request = new Request.Builder()
                                        .url(urlApi2.getUrl())
                                        .post(formBody)
                                        .build();
                                OkHttpClient okHttpClient = new OkHttpClient();
                                Response response = okHttpClient.newCall(request).execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();
                } else
                    dialog.showProblemDialog(this, "Problem", "Not Connected Network");
            } else
                Toast.makeText(this, "Please Select threshold", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            dialog.showProblemDialog(this, "Problem", "Save Fail");
        }
    }

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
