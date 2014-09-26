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
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import de.rwth.comsys.samrad.preserv.R;
import de.rwth.comsys.samrad.preserv.model.Poly;
import de.rwth.comsys.samrad.preserv.model.ShamirGPS;
import de.rwth.comsys.samrad.preserv.utilz.Utilz;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A service to be woken up by AlarmManager or
 * user's interaction which listens to location
 * updates from Google Play Service and extract
 * the latitude and longitude.
 *
 */
public class PulseService extends Service implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String TAG = "PULSE_SERVICE";
//    public static final String PREF_IS_ALARM_ON = "isAlarmOn";

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;

    // List of polys
    private List<Poly> mPolyList = new ArrayList<Poly>();

    // Referenc to location
    private Location mLocation;

    // Flag to indicate Google Play Service availability
    private Boolean isServiceAvailable = false;

    private double lat, lng;
    private int counter = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Created");

        // Lat and Lng initialization
        lat = 0;
        lng = 0;

        // Load the JSON into polys
        mPolyList = Utilz.json2Poly(preparePolys());

        // Update Preference
        Utilz.updatePref(this, R.string.pref_no_of_polygons, String.class, String.valueOf(mPolyList.size()));

        // Log
        Log.d(TAG, "Poly size: " + String.valueOf(mPolyList.size()));

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use highest accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 10 seconds
        mLocationRequest.setInterval(ConfigConstants.UPDATE_INTERVAL);
        // Set the fastest update interval to 5 seconds
        mLocationRequest.setFastestInterval(ConfigConstants.FASTEST_INTERVAL);

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
            while (mLocation == null) {
                Log.d(TAG, "No location found yet");
            }

            return START_STICKY;

            // Do what it must be done with the location
//            Log.d(TAG, "stopSelf is supposed to be called here");

//            AlarmReceiver.completeWakefulIntent(intent);
//            stopSelf();
//
//            AlarmReceiver.completeWakefulIntent(intent);
        } else if (!mLocationClient.isConnected() || !mLocationClient.isConnecting()) {
            mLocationClient.connect();
        }

        return START_STICKY;
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

        counter++;
        Log.d(TAG, "Count: " + counter);

        lat = location.getLatitude();
        lng = location.getLongitude();

        /*
         * Get 3 locations and then stop and
         * return the most accurate one
         */
        if (counter < 3) {
            if (mLocation != null && location.getAccuracy() < mLocation.getAccuracy()) {
                lat = location.getLatitude();
                lng = location.getLongitude();
            }
        } else {

            // Create secret array by geo-fencing the location
            long[] secret = geofence(lat, lng);

            // Update Preference
            Utilz.updatePref(this, R.string.pref_last_found_location, String.class, lat + ", " + lng);

            // Generate shares
            ShamirGPS.SharesMessage[] shareMessages = createShareMessages(secret);

            // Log
            Log.d(TAG, "Secret: " + Arrays.toString(shareMessages));

            // Stop the service
            stopSelf();
        }

        mLocation = location;

        // Report to the UI that the location was updated
        String msg = Double.toString(lat) + ", " +  Double.toString(lng);
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

        mLocation = null;

        if(isServiceAvailable && mLocationClient != null) {
            mLocationClient.removeLocationUpdates(this);
            // Destroy the current location client
            mLocationClient = null;
        }

        Log.d(TAG, "Destroyed");
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
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        String freqInSecond = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("pref_pulse_frequency", "60");
        long freqInMilli = Long.parseLong(freqInSecond) * 1000;

        if (isOn) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), freqInMilli, pi);
            Log.d(TAG, "Schedule was set to repeat every " +
                    String.valueOf(freqInMilli) + " seconds");
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
            Log.d(TAG, "Schedule was unset");
        }

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.pref_is_schedule_on), isOn)
                .commit();

    }

    private JSONObject preparePolys() {

        try {
            String jsonString = Utilz.readJSON(getFilesDir() + "/polygons.json");
            JSONObject json = new JSONObject(jsonString);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * Checks whether the location falls into
     * any of the polygons or not.
     *
     * @param lat location's latitude
     * @param lng location's longitude
     *
     * @return long[] containing the secret
     */
    private long[] geofence(double lat, double lng) {

        LatLng point = new LatLng(lat, lng);
        long[] secret = new long[mPolyList.size()];

        for (int i = 0; i < mPolyList.size(); i++) {

            if (PolyUtil.containsLocation(
                    point, mPolyList.get(i).getVertexList(), true)) {
                secret[i] = 1;
            } else {
                secret[i] = 0;
            }
        }

        // Update preferences
        Utilz.updatePref(this, R.string.pref_secret, String.class, Arrays.toString(secret));
        return secret;
    }


    private ShamirGPS.SharesMessage[] createShareMessages(long[] secret) {

        final ShamirGPS shGPS = new ShamirGPS(this);
        long[] scaled = Utilz.tns(secret, 3, 2);
        long[][] shares = shGPS.createShamirShares(1, 3, 9223372036854775783L, scaled);
        final ShamirGPS.SharesMessage[] shareMessages = shGPS.createMsgPackMessage(shares, 9223372036854775783L);

        final Runnable runnable = new Runnable() {
            public void run() {
                String[] peerIp = Utilz.getPrivacyPeersIp(PulseService.this);
                int[] peerPort = Utilz.getPrivacyPeersPort(PulseService.this);
                shGPS.shareOut(shareMessages, peerIp, peerPort);
            }
        };

        performOnBackgroundThread(runnable);

        // TODO Shares Preferences
        return shareMessages;
    }

    private void changePref() {
        getString(R.string.app_name);

        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(getString(R.string.app_name), false)
                .commit();
    }

}
