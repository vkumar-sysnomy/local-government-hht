package com.farthestgate.android.helper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.farthestgate.android.model.database.BreakTable;
import com.farthestgate.android.ui.pcn.VisualPCNListActivity;

import java.util.Date;

/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 * <p>
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 * <p>
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification assocaited with that service is removed.
 */
public class LocationHelperFused extends Service {

    /*private static final String TAG = LocationHelperFused.class.getSimpleName();

    *//**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     *//*
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = CeoApplication.getUpdateGps()*1000;
    private static final int PRIORITY_HIGH_ACCURACY = CeoApplication.getPriority();


    *//**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     *//*
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    *//**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     *//*
    private LocationRequest mLocationRequest;

    *//**
     * Provides access to the Fused Location Provider API.
     *//*
    private FusedLocationProviderClient mFusedLocationClient;
    private Location location;

    *//**
     * Callback for changes in location.
     *//*
    private LocationCallback mLocationCallback;

    private static final String PACKAGE_NAME =
            "com.farthestgate.environmental.enforcement.helper.LocationHelperFused";
    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final int  LOCATION_SETTINGS_REQUEST=1000;*/


    @Override
    public void onCreate() {


       /* mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onNewLocation(locationResult.getLastLocation());
            }
        };
        createLocationRequest();
        getLastLocation();*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

  /*  public void requestLocationUpdates() {

        LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        settingsBuilder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(settingsBuilder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response =
                            task.getResult(ApiException.class);
                } catch (ApiException ex) {
                    switch (ex.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException =
                                        (ResolvableApiException) ex;
                                resolvableApiException
                                        .startResolutionForResult(VisualPCNListActivity.activity,
                                                LOCATION_SETTINGS_REQUEST);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                            break;
                    }
                }
            }
        });

        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());

        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            stopSelf();
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    @Override
    public void onDestroy() {
        //removeLocationUpdates();
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                location = task.getResult();
                                onNewLocation(location);
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        if(location!=null){
            this.location = location;
            Intent intent = new Intent(ACTION_BROADCAST);
            intent.putExtra(EXTRA_LOCATION, this.location);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    *//**
     * Sets the location request parameters.
     *//*
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(PRIORITY_HIGH_ACCURACY);
        requestLocationUpdates();
    }*/

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if(VisualPCNListActivity.currentStreet != null) {
            String streetName = VisualPCNListActivity.currentStreet.streetname;
            ((VisualPCNListActivity) VisualPCNListActivity.activity).publishCeoTracking(streetName);
        }

        BreakTable breakTable = DBHelper.getBreakData(DBHelper.getCeoUserId());
        if(breakTable!=null){
            breakTable.setEndTime(new Date().getTime());
            breakTable.save();
        }
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }
}