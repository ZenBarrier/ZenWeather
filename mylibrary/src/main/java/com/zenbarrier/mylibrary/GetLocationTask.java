package com.zenbarrier.mylibrary;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;


public class GetLocationTask extends AsyncTask<Void, Void, Location> {

    private static final String TAG = GetLocationTask.class.getSimpleName();

    private Context mContext;
    private LocationListener mLocationListener;
    private Location mLocation = null;
    private GoogleApiClient mGoogleApiClient;
    private Handler mHandler;

    public GetLocationTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                LocationRequest locationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                        .setInterval(10000)
                        .setFastestInterval(5000);

                mLocationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        mLocation = location;
                    }
                };

                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                LocationServices.FusedLocationApi
                        .requestLocationUpdates(mGoogleApiClient, locationRequest, mLocationListener)
                        .setResultCallback(new ResultCallback<com.google.android.gms.common.api.Status>() {
                            @Override
                            public void onResult(@NonNull com.google.android.gms.common.api.Status status) {
                                if (status.getStatus().isSuccess()) {
                                    Log.d(TAG, "Successfully requested location updates.");
                                } else {
                                    Log.e(TAG,
                                            "Failed in requesting location updates, "
                                                    + "status code: "
                                                    + status.getStatusCode()
                                                    + ", message: "
                                                    + status.getStatusMessage());
                                }
                            }
                        });
            }

            @Override
            public void onConnectionSuspended(int i) {
            }
        };

        GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            }
        };

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build();
        mGoogleApiClient.connect();

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Location location = new Location("");
                location.setLatitude(0);
                location.setLongitude(0);
                mLocation = location;
            }
        }, 500);
    }


    @Override
    protected Location doInBackground(Void... params) {

        while(mLocation == null && !isCancelled());
        mHandler.removeCallbacksAndMessages(null);
        return mLocation;
    }

    @Override
    protected void onPostExecute(Location location) {
        super.onPostExecute(location);

        if(mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, mLocationListener);
        }

        mGoogleApiClient.disconnect();

        if(mContext instanceof OnLocationFound && !isCancelled()){
            ((OnLocationFound) mContext).onLocationFound(location);
        }
    }

    public interface OnLocationFound{
        void onLocationFound(Location location);
    }
}