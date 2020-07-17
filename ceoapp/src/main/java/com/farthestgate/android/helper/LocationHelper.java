package com.farthestgate.android.helper;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.farthestgate.android.CeoApplication;

import java.io.IOException;
import java.util.List;

/**
 * Created by Jitendra on 04/03/2015.
 */
public class LocationHelper extends Service implements LocationListener {
    private static final String TAG = LocationHelper.class.getSimpleName();
    private final Context mContext;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    boolean userInGeofence = true;
    Location location;
    double latitude;
    double longitude;
    float speed;
    private static long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static long MIN_TIME_BW_UPDATES = 1000 * 30 * 1; // 30 seconds
    protected LocationManager locationManager;
    private OnLocationChangedListener onLocationChangedListener;
    public interface OnLocationChangedListener {
        public void onCurrentLocationChanged(Location location);
    }
    public LocationHelper(Context context) {
        this.mContext = context;
        MIN_TIME_BW_UPDATES = CeoApplication.getUpdateGps() * 1000 * 1;
        onLocationChangedListener = (OnLocationChangedListener)context;
        getLocation();
    }
    public void getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("GPS/Network", "disable");
            } else {
                this.canGetLocation = true;
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("GPS Enabled", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                if (isNetworkEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(LocationHelper.this);
        }
    }
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }
    public float getSpeed(){
        if(location != null){
            speed = location.getSpeed();
        }
        return speed;
    }
    public boolean OutsideGeoFence(){
        return userInGeofence;
    }
    public boolean canGetLocation() {
        return canGetLocation;
    }
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("GPS settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
    @Override
    public void onLocationChanged(Location location) {
        if (onLocationChangedListener != null) {
            speed = location.getSpeed();
            this.location = location;
            if(location !=null){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
            onLocationChangedListener.onCurrentLocationChanged(location);
        }
    }
    @Override
    public void onProviderDisabled(String provider) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
