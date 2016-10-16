package me.pr3a.localweather;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import me.pr3a.localweather.Helper.MyAlertDialog;
import me.pr3a.localweather.Helper.UrlApi;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.Manifest;
import android.view.WindowManager;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.OnMapReadyCallback;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class LocationActivity extends AppCompatActivity implements OnLocationUpdatedListener, OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_ID = 1001;
    private GoogleMap mMap;
    private double latitude = 0;
    private double longitude = 0;
    private final static String FILENAME = "location.txt";
    private final static String url1 = "http://128.199.210.91/device/";
    private final static String url2 = "http://128.199.210.91/device/update/location/";
    private final UrlApi urlApi1 = new UrlApi();
    private final UrlApi urlApi2 = new UrlApi();
    private final static int READ_BLOCK_SIZE = 100;
    private final MyAlertDialog dialog = new MyAlertDialog();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("APP", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        //Display Toolbar
        this.showToolbar("Location", "");

        if (isNetworkConnected()) {
            String PSerialNumber;
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                PSerialNumber = bundle.getString("P_SerialNumber");
                //Set url
                urlApi1.setUri(url1, PSerialNumber);
                urlApi2.setUri(url2, PSerialNumber);
            }else finish();

            new LoadJSON1().execute(urlApi1.getUrl());

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

                JSONObject json = new JSONObject(data);
                String latitude2 = String.format("%s", json.getString("latitude"));
                String longitude2 = String.format("%s", json.getString("longitude"));
                // convect to double
                latitude = Double.parseDouble(latitude2);
                longitude = Double.parseDouble(longitude2);
            } catch (Exception e) {
                finish();
                startActivity(getIntent());
                e.printStackTrace();
            }
        } else {
            dialog.showProblemDialog(this, "Problem", "Not Connected Network");
        }


        MapFragment mapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_map_container, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);
        // Keep the screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStart() {
        Log.e("APP", "onStart");
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
            return;
        }
        startLocation();
    }

    @Override
    protected void onStop() {
        Log.e("APP", "onStop");
        super.onStop();
        SmartLocation.with(this)
                .location()
                .stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_ID && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onLocationUpdated(Location location) {
        Log.e("APP", "onLocationUpdated");

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        showLocation(location);
    }

    private void startLocation() {
        if (SmartLocation.with(this).location().state().locationServicesEnabled()) {
            SmartLocation.with(this)
                    .location(new LocationGooglePlayServicesWithFallbackProvider(this))
                    .start(this);
        } else
            Log.e("APP", "onStart fail");
    }

    private void showLocation(final Location location) {
        Log.e("APP", "showLocation");
        if (location != null) {
            final String text = "Current Location \n"+
                                "Latitude : " + location.getLatitude() + "\n" +
                                "Longitude : " + location.getLongitude() + "";
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

            /* We are going to get the address for the current position
            SmartLocation.with(this).geocoding().reverse(location, new OnReverseGeocodingListener() {
                @Override
                public void onAddressResolved(Location original, List<Address> results) {
                    if (results.size() > 0) {
                        Address result = results.get(0);
                        StringBuilder builder = new StringBuilder(text);
                        builder.append("\n[Address] ");
                        List<String> addressElements = new ArrayList<>();
                        for (int i = 0; i <= result.getMaxAddressLineIndex(); i++) {
                            addressElements.add(result.getAddressLine(i));
                        }
                        builder.append(TextUtils.join(", ", addressElements));

                        Toast.makeText(LocationActivity.this, builder.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });*/
        } else {
            Toast.makeText(this, "Null location", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng home = new LatLng(latitude, longitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(home)
                .title("IOT WeatherNow")
                .snippet(latitude+" , "+longitude)
        );

        mMap.addCircle(new CircleOptions()
                .center(home)
                .radius(100)
                .fillColor(0x333F51B5)
                .strokeColor(Color.BLUE)
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 15));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    // Create MenuBar on Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_location_toolbar, menu);
        return true;
    }

    // Click button refresh
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            finish();
            startActivity(getIntent());
        } else if (id == R.id.action_save) {
            startLocation();
            try {
                //Check Connect network
                if (isNetworkConnected()) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            Log.d("APP", "doInBackground");
                            try {
                                RequestBody formBody = new FormBody.Builder()
                                        .add("latitude", latitude + "")
                                        .add("longitude", longitude + "")
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
            } catch (Exception e) {
                dialog.showProblemDialog(this, "Problem", "Save Fail");
            }
        }
        return super.onOptionsItemSelected(item);
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
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
            super.onPostExecute(result);
            try {
                //Writer location
                FileOutputStream fOut = openFileOutput(FILENAME, MODE_PRIVATE);
                OutputStreamWriter writer = new OutputStreamWriter(fOut);
                writer.write(result);
                writer.flush();
                writer.close();
            } catch (IOException ioe) {
                dialog.showConnectDialog(LocationActivity.this, "Connect", "Connect UnSuccess1");
                ioe.printStackTrace();
            }
            //Toast.makeText(LocationActivity.this, "Save successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}

