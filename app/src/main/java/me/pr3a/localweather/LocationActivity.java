package me.pr3a.localweather;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
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

public class LocationActivity extends AppCompatActivity implements OnLocationUpdatedListener, OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_ID = 1001;
    private GoogleMap mMap;
    private double latitude;
    private double longitude;

    @Override
    public void onCreate(Bundle bundle) {
        Log.e("APP", "onCreate");
        super.onCreate(bundle);
        setContentView(R.layout.activity_location);
        //Display Toolbar
        this.showToolbar("Location", "");

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

    private void startLocation() {
        if (SmartLocation.with(this).location().state().locationServicesEnabled()) {
            SmartLocation.with(this)
                    .location(new LocationGooglePlayServicesWithFallbackProvider(this))
                    .start(this);
        } else
            Log.e("APP", "onStart fail");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_ID && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocation();
        }
    }

   private void showLocation(final Location location) {
        Log.e("APP", "showLocation");
        if (location != null) {
            final String text = "Latitude : " + location.getLatitude() + "\n" +
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
    public void onLocationUpdated(Location location) {
        Log.e("APP", "onLocationUpdated");

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        showLocation(location);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng home = new LatLng(13.6544576, 100.4127321);

        /*
        * MarkerOptions options = new MarkerOptions();
        * options.position(home);
        * options.title("IOT WeatherNow");
        * mMap.addMarker(options);
        *
        *  CircleOptions circleOptions = new CircleOptions();
        *  circleOptions.center(home);
        *  circleOptions.radius(100);
        *  circleOptions.fillColor(0x333F51B5);
        *  circleOptions.strokeColor(Color.BLUE);
        *  mMap.addCircle(circleOptions);
        */

        mMap.addMarker(new MarkerOptions()
                .position(home)
                .title("IOT WeatherNow")
                .snippet("SerialNumber")
        );

        mMap.addCircle(new CircleOptions()
                .center(home)
                .radius(100)
                .fillColor(0x333F51B5)
                .strokeColor(Color.BLUE)
        );


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 17));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
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
}