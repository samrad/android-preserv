package de.rwth.comsys.samrad.preserv.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * Created by Sam on 9/7/2014.
 */
public class PulseService extends Service implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String TAG = "PULSE_SERVICE";
    private static final int BEAT_INTERVAL = 10000; // {10s} // 1000 * 60 * 5 {5m}
    public static final String PREF_IS_ALARM_ON = "isAlarmOn";

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;

    private boolean locationFound =  false;

    private Boolean isServiceAvailable = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(RequestConstants.UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(RequestConstants.FASTEST_INTERVAL);

        isServiceAvailable = servicesConnected();
        
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
    }

    private Boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d(TAG, "Google Play Service is available");
            return true;
           } else {
            Log.d(TAG, "Google Play Service is not available");
            return false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (isServiceAvailable && mLocationClient.isConnected()) {
            while (!locationFound) {
                Log.d(TAG, "Location is not found yet");
            }

            // Do what it must be done with the location
            stopSelf();
        } else if (!mLocationClient.isConnected() || !mLocationClient.isConnecting()) {
            mLocationClient.connect();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {

        // Request location updates using static settings
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
        Log.d(TAG, "Location Service connected");
    }

    @Override
    public void onDisconnected() {

        // Destroy the current location client
        mLocationClient = null;
        Log.d(TAG, "Location Service disconnected");
    }

    @Override
    public void onLocationChanged(Location location) {

        locationFound = true;

        // Report to the UI that the location was updated
        String msg = Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.d(TAG, "Location was found at " + msg);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            Log.d(TAG, "There is a resolution");
        } else {
            Log.d(TAG, "There is no resolution");
        }

    }

    @Override
    public void onDestroy() {

        locationFound = false;

        if(isServiceAvailable && mLocationClient != null) {
            mLocationClient.removeLocationUpdates(this);
            // Destroy the current location client
            mLocationClient = null;
        }
        super.onDestroy();
    }

    /**
     * Executes the network requests on a separate thread.
     *
     * @param runnable The runnable instance containing network mOperations to
     *        be executed.
     */
    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                }
            }
        };
        t.start();
        return t;
    }

    /**
     * Set the schedule of the service in AlarmManager
     * @param context
     * @param isOn
     */
    public static void setServiceAlarm(Context context, boolean isOn) {

        Log.i(TAG, "Set service alarm @" + SystemClock.elapsedRealtime());

        Intent i = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), BEAT_INTERVAL, pi);
            Log.i(TAG, "Schedule was set");
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
            Log.i(TAG, "Schedule was unset");
        }

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PulseService.PREF_IS_ALARM_ON, isOn)
                .commit();

    }
}
