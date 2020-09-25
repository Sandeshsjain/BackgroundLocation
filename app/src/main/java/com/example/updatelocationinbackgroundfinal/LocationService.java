package com.example.updatelocationinbackgroundfinal;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

public class LocationService extends Service {
    private Location currentLocation = null;
    private final IBinder binder = new LocalBinder();
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLocations() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                currentLocation = locationResult.getLastLocation();
                Log.d("Location_update", latitude + ", " + longitude);
                Geocoder geocoder;
                List<Address> addresses = null;
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    String address = addresses.get(0).getAddressLine(0);
                    showNotification(address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void showNotification(String address) {
        int notificationId = new Random().nextInt(100);
        String channelId = "notification_channel_1";
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),channelId
        );
        builder.setSmallIcon(R.drawable.ic_android_black_24dp);
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentTitle("Your current Location is :");
        builder.setContentText(address);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(notificationManager != null && notificationManager.getNotificationChannel(channelId)==null){
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId,
                        "Notification Channel 1",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("this notification channel is used to notify user.");
                notificationChannel.enableVibration(true);
                notificationChannel.enableLights(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        Notification notification = builder.build();
        if(notificationManager != null){
            notificationManager.notify(notificationId,notification);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public class LocalBinder extends Binder {
        LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }

    private void startLocationService() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "1");
        builder.setContentTitle("GPS");
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(6000);
        locationRequest.setFastestInterval(4000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        startForeground(Constant.LOCATION_SERVICE_ID, builder.build());
    }

    private void stopLocationService(){
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null){
            String action = intent.getAction();
            if (action!=null){
                if (action.equals(Constant.ACTION_START_LOCATION_SERVICE)){
                    startLocationService();
                }else if (action.equals(Constant.ACTION_STOP_LOCATION_SERVICE)){
                    stopLocationService();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
    public Location getCurrentLocation(){
        return currentLocation;
    }

}
