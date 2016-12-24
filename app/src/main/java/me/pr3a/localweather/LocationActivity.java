package me.pr3a.localweather;

import android.content.Intent;
import android.location.Location;
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
import android.widget.Toast;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import me.pr3a.localweather.Helper.MyAlertDialog;
import me.pr3a.localweather.Helper.MyNetwork;
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
//import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class LocationActivity extends AppCompatActivity implements OnLocationUpdatedListener, OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private GoogleMap mMap;
    private double latitude = 0;
    private double longitude = 0;
    private String Serial = "Device ";
    private String sid = "Ruk";
    private static final String FILENAME = "Serialnumber.txt";
    private final static String FILENAME2 = "location.txt";
    private final static String url1 = "http://www.doofon.me/device/";
    private final static String url2 = "http://www.doofon.me/device/update/location";
    private static final int LOCATION_PERMISSION_ID = 1001;
    private final UrlApi urlApi1 = new UrlApi();
    private final UrlApi urlApi2 = new UrlApi();
    private final static int READ_BLOCK_SIZE = 100;
    private final MyAlertDialog dialog = new MyAlertDialog();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        //Display Toolbar
        this.showToolbar("Setting", "Device Location");
        //Show DrawerLayout and drawerToggle
        this.initInstances();

        if (MyNetwork.isNetworkConnected(this)) {
            //Read SerialNumber
            this.readData();
            //Load SerialNumber
            new LoadJSON1().execute(urlApi1.getUri());
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
                Serial += String.format("%s", json.getString("SerialNumber"));
                String latitude2 = String.format("%s", json.getString("latitude"));
                String longitude2 = String.format("%s", json.getString("longitude"));

                // Convect to double
                latitude = Double.parseDouble(latitude2);
                longitude = Double.parseDouble(longitude2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // SET MAP
            this.setMap();
        } else dialog.showProblemDialog(this, "Problem", "Not Connected Network");
        //Log.i("APP", "onCreate");
    }

    // Activity onStart is Rule Start Service Location
    @Override
    protected void onStart() {
        Log.i("APP", "onStart");
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
            return;
        }
        this.startLocation();
    }

    // Activity onStop is Rule Stop Service Location
    @Override
    protected void onStop() {
        Log.i("APP", "onStop");
        super.onStop();
        SmartLocation.with(this)
                .location()
                .stop();
    }

    //Request Permission Location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("APP", "onRequestPermissionsResult");
        if (requestCode == LOCATION_PERMISSION_ID && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
        }
    }

    // Update Current Location
    @Override
    public void onLocationUpdated(Location location) {
        Log.i("APP", "onLocationUpdated");
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    // Marker Map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("APP", "onMapReady");
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        if (latitude == 0 || longitude == 0) {
            return;
        }

        LatLng lo = new LatLng(latitude, longitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(lo)
                .title(Serial)
                .snippet(latitude + " , " + longitude)
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lo, 15));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
            overridePendingTransition(0, 0);
            startActivity(getIntent());
        } else if (id == R.id.action_save) {
            //Check Connect network
            if (MyNetwork.isNetworkConnected(this)) {
                this.saveLocation();
            } else dialog.showProblemDialog(this, "Problem", "Not Connected Network");
        }
        return super.onOptionsItemSelected(item);
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
                overridePendingTransition(0, 0);
                Intent intentMain = new Intent(this, MainActivity.class);
                startActivity(intentMain);
                break;
            case R.id.nav_DeviceProfile:
                finish();
                overridePendingTransition(0, 0);
                Intent intentDevice = new Intent(this, DeviceActivity.class);
                startActivity(intentDevice);
                break;
            case R.id.nav_location:
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                break;
            case R.id.nav_setting:
                finish();
                overridePendingTransition(0, 0);
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivity(intentSettings);
                break;
            case R.id.nav_mode:
                finish();
                overridePendingTransition(0, 0);
                Intent intentMode = new Intent(this, ModeActivity.class);
                startActivity(intentMode);
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

    // Button back
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else {
            finish();
            overridePendingTransition(0, 0);
            Intent intentDevice = new Intent(this, MainActivity.class);
            startActivity(intentDevice);
            super.onBackPressed();
        }
    }

    // Button Disconnect
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
            overridePendingTransition(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Set Google Map
    private void setMap() {
        //Map
        MapFragment mapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_map_container, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);
        // Keep the screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Start LocationService
    private void startLocation() {
        Log.i("APP", "StartLocation fail");
        if (SmartLocation.with(this).location().state().locationServicesEnabled()) {
            SmartLocation.with(this)
                    .location(new LocationGooglePlayServicesWithFallbackProvider(this))
                    .start(this);
        } else Log.e("APP", "StartLocation fail");
    }

    /*
    // Show Current Location (Latitude,Longitude)
    private void showLocation(final Location location) {
        Log.i("APP", "showLocation");
        if (location != null) {
            final String text = "Current Location \n" +
                    "Latitude : " + location.getLatitude() + "\n" +
                    "Longitude : " + location.getLongitude() + "";
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Null location", Toast.LENGTH_SHORT).show();
        }
    }
    */

    // Show Toolbar
    private void showToolbar(String title, String subTitle) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        toolbar.setSubtitle(subTitle);
        setSupportActionBar(toolbar);
    }

    // Show DrawerLayout and drawerToggle
    private void initInstances() {
        // NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    // Delay
    private void intentDelay() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(getIntent());
            }
        }, 20000);
    }

    // UPDATE Location to DB
    private void saveLocation() {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("SerialNumber", urlApi2.getApikey())
                    .add("latitude", latitude + "")
                    .add("longitude", longitude + "")
                    .add("sid", sid)
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

    // Read SerialNumber
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

    // AsyncTask Load Data Device
    private class LoadJSON1 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            Log.i("APP", "doInBackground");
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
            Log.i("APP", "onPostExecute");
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

