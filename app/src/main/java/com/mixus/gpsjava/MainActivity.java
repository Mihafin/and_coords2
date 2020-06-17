package com.mixus.gpsjava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "MainActivity";
    private static final int    REQUEST_PERMISSION_LOCATION = 1;

    private TextView lblInfo;
    private TextView lblSatelliteCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lblInfo           = findViewById(R.id.lblText);
        lblSatelliteCount = findViewById(R.id.idSatelliteCount);

        lblInfo.setText(R.string.app_name);

        checkPermissionAndStart();
    }

    @SuppressLint("MissingPermission")
    private void startApp() {
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        boolean isGPSEnabled     = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);    // getting GPS satellite status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);// getting cellular network status

        Log.d(TAG, String.format(Locale.ENGLISH,
                "isGPSEnabled: %b, isNetworkEnabled: %b",
                isGPSEnabled, isNetworkEnabled));

        this.lblInfo.setText(R.string.detect_in_process);
        this.lblSatelliteCount.setText(R.string.find_satellites);

        GnssStatus.Callback gnssStatusCallback = new GnssStatus.Callback() {
            public void onSatelliteStatusChanged(GnssStatus status) {
                Log.d(TAG, String.format("onSatelliteStatusChanged: %d", status.getSatelliteCount()));
                lblSatelliteCount.setText(String.format(Locale.ENGLISH, "Satellites count: %d", status.getSatelliteCount()));
            }
        };
        locationManager.registerGnssStatusCallback(gnssStatusCallback);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);

    }

    private void checkPermissionAndStart() {
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        int permissionCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_LOCATION);
            }
            else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_LOCATION);
            }
        }
        else {
            Log.d(TAG, "Permission (already) Granted!");
            startApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission Granted!");
                    startApp();
                } else {
                    Log.d(TAG, "Permission Denied!");
                    lblInfo.setText(R.string.no_permisions);
                }
        }
    }

    private void showExplanation(String title, String message, final String permission, final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permissionName}, permissionRequestCode);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) { //<9>
        Log.d(TAG, "onLocationChanged with location " + location.toString());

        String text = String.format(Locale.ENGLISH,
                "Lat:\t %f\nLong:\t %f\nAlt:\t %f\nBearing:\t %f", location.getLatitude(),
                location.getLongitude(), location.getAltitude(), location.getBearing());
        this.lblInfo.setText(text);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d(TAG, "onStatusChanged (deprecated)");
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(TAG, "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(TAG, "onProviderDisabled");
    }
}
