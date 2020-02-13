package com.example.bus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.provider.AlarmClock.VALUE_RINGTONE_SILENT;

public class MainActivity extends AppCompatActivity
        implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {

    private GoogleMap mMap;
    public static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static boolean PERMISSION;
    public static boolean gps_enabled;
    EditText mSearchText;
    private static final String TAG = "MyActivity";

    /*static final CameraPosition NEWYORK = CameraPosition.builder()
            .target(new LatLng(40.784, -73.9857))
            .zoom(21)
            .bearing(0)
            .tilt(45)
            .build();*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSearchText = findViewById(R.id.search_text);
    }

    private void init() {
        Log.d(TAG, "init: initializing");
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    geoLocate();
                }
                return false;
            }
        });
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        }
        catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException " + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + address.getLatitude() + "," + address.getLongitude() + "&dirflg=r");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        }


    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        updateLocationUI();
        init();
    }
    public static int h;
    public static int m;

    public void createAlarm(View view) {
        EditText eh = findViewById(R.id.hour_number);
        EditText em = findViewById(R.id.min_number);

        if (eh.length() != 0 && em.length() != 0) {
            h = Integer.parseInt(eh.getText().toString());
            m = Integer.parseInt(em.getText().toString());
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                    .putExtra(AlarmClock.EXTRA_HOUR, h)
                    .putExtra(AlarmClock.EXTRA_MINUTES, m)
                    .putExtra(AlarmClock.EXTRA_VIBRATE, true)
                    .putExtra(AlarmClock.EXTRA_RINGTONE, VALUE_RINGTONE_SILENT);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
        else Toast.makeText(this, "Please input time", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onMyLocationClick(Location location) {

        Toast.makeText(this, "Current location:", Toast.LENGTH_SHORT).show();
        
    }

    @Override
    public boolean onMyLocationButtonClick() {
        LocationManager lm = (LocationManager) getSystemService(Context. LOCATION_SERVICE );
        gps_enabled = lm.isProviderEnabled(LocationManager. GPS_PROVIDER );

        if (gps_enabled) {
            Toast.makeText(this, "My Location button clicked", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "Please enable location", Toast.LENGTH_SHORT).show();

        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    public void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (PERMISSION) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);
                mMap.setOnMyLocationClickListener(this);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.*/

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            PERMISSION = true;
            updateLocationUI();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PERMISSION = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PERMISSION = true;
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(this, "Please enable location", Toast.LENGTH_SHORT).show();
            }
        }
        updateLocationUI();
    }

}
