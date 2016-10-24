package me.pr3a.localweather;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import me.pr3a.localweather.Helper.MyAlertDialog;
import me.pr3a.localweather.Helper.UrlApi;
import okhttp3.Call;
import okhttp3.Callback;
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

public class LocationActivity extends AppCompatActivity implements OnLocationUpdatedListener, OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener  {

    private Toolbar toolbar;
    private GoogleMap mMap;
    private double latitude = 0;
    private double longitude = 0;
    private static final String FILENAME = "data.txt";
    private final static String FILENAME2 = "location.txt";
    private final static String url1 = "http://www.doofon.me/device/";
    private final static String url2 = "http://www.doofon.me/device/update/location/";
    private static final int LOCATION_PERMISSION_ID = 1001;
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
        //Show DrawerLayout and drawerToggle
        this.initInstances();

        if (isNetworkConnected()) {
            //Read SerialNumber
            this.readData();
            new LoadJSON1().execute(urlApi1.getUrl());
            //read location
            try {
                FileInputStream fIn = openFileInput(FILENAME2);
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
                String Serial = String.format("%s", json.getString("SerialNumber"));
                String latitude2 = String.format("%s", json.getString("latitude"));
                String longitude2 = String.format("%s", json.getString("longitude"));

                // convect to double
                latitude = Double.parseDouble(latitude2);
                longitude = Double.parseDouble(longitude2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Map
            MapFragment mapFragment = MapFragment.newInstance();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragment_map_container, mapFragment);
            fragmentTransaction.commit();
            mapFragment.getMapAsync(this);
            // Keep the screen always on
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else dialog.showProblemDialog(this, "Problem", "Not Connected Network");
    }

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
        } else Log.e("APP", "StartLocation fail");
    }


    private void showLocation(final Location location) {
        Log.e("APP", "showLocation");
        if (location != null) {
            final String text = "Current Location \n" +
                    "Latitude : " + location.getLatitude() + "\n" +
                    "Longitude : " + location.getLongitude() + "";
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Null location", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (latitude == 0 || longitude == 0) {
            finish();
            startActivity(getIntent());
        }
        LatLng home = new LatLng(latitude, longitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(home)
                .title("IOT DooFon")
                .snippet(latitude + " , " + longitude)
        );

       /* mMap.addCircle(new CircleOptions()
                .center(home)
                .radius(100)
                .fillColor(0x333F51B5)
                .strokeColor(Color.BLUE)
        );*/
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
            Toast.makeText(this, "Refresh Location", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(getIntent());
        } else if (id == R.id.action_save) {
            //Check Connect network
            if (isNetworkConnected()) {
                this.saveLocation();
            } else dialog.showProblemDialog(this, "Problem", "Not Connected Network");
        }
        return super.onOptionsItemSelected(item);
    }


    //Show Toolbar
    private void showToolbar(String title, String subTitle) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        toolbar.setSubtitle(subTitle);
        setSupportActionBar(toolbar);
    }

    //Show DrawerLayout and drawerToggle
    private void initInstances() {
        // NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
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
                startActivity(getIntent());
                break;
            case R.id.nav_setting:
                finish();
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivity(intentSettings);
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    //Button back
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else {
            android.os.Process.killProcess(android.os.Process.myPid());
            finish();
            super.onBackPressed();
        }
    }

    private void intentDelay() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(getIntent());
            }
        }, 15000);
    }


    private void saveLocation(){
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
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);
                }
            });
            Toast.makeText(LocationActivity.this, "Save Location", Toast.LENGTH_SHORT).show();
            intentDelay();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            if (!(data.equals(""))) {
                //Set url & LoadJSON
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
                FileOutputStream fOut = openFileOutput(FILENAME2, MODE_PRIVATE);
                OutputStreamWriter writer = new OutputStreamWriter(fOut);
                writer.write(result);
                writer.flush();
                writer.close();
            } catch (Exception e) {
                dialog.showConnectDialog(LocationActivity.this, "Connect", "Connect UnSuccess");
                e.printStackTrace();
            }
            //Toast.makeText(LocationActivity.this, "Save successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}

