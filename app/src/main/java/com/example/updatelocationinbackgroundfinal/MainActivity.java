package com.example.updatelocationinbackgroundfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1;
    private LocationManager locationManager;
    private TextView latitude,longitude, add;
    private LocationService locationService;
    private boolean mBound = false;
    private String la,lo;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private Handler handler = new Handler();
    private Button start , stop;
    private Runnable runnable;
    private boolean detect = true;
    private int delay = 5000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getAddress();
        startLocationService();



        start = findViewById(R.id.updateLocation);
        stop = findViewById(R.id.removeUpdate);


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAddress();
                startLocationService();
                detect = true;
                stop.setVisibility(View.VISIBLE);
                start.setVisibility(View.INVISIBLE);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationService();
                detect = false;
                start.setVisibility(View.VISIBLE);
                stop.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void getAddress(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

                startLocationService();
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location!=null){
                            try {
                                showLocation(location);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION );
            }
        }
    }

    private void showLocation(Location location) throws IOException {

        Double lat = location.getLatitude();
        Double longi = location.getLongitude();
        la = String.valueOf(lat);
        lo = String.valueOf(longi);
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(lat, longi, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()


    }

    private boolean isLocationServiceRunning(){
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager!=null){
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)){
                if (LocationService.class.getName().equals(service.service.getClassName())){
                    if (service.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private boolean isLocationServiceRunningGetLocation(){
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager!=null){
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)){
                if (LocationService.class.getName().equals(service.service.getClassName())){
                    if (service.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }




    private void startLocationService(){
        if (!isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constant.ACTION_START_LOCATION_SERVICE);
            startService(intent);

        }
    }

    private void stopLocationService(){
        if (isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constant.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}