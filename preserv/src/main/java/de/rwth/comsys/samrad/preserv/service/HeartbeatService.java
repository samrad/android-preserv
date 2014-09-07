package de.rwth.comsys.samrad.preserv.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Sam on 9/3/2014.
 */
public class HeartbeatService extends IntentService {

    private static final String TAG = "HeartbeatService";
//    private static final int BEAT_INTERVAL = 1000 * 60 * 5; // 5 minutes
    private static final int BEAT_INTERVAL = 10000; // 10 seconds
    public static final String PREF_IS_ALARM_ON = "isAlarmOn";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public HeartbeatService(String name) {
        super(name);
    }

    // Default constructor for AndroidManifest.xml lint error
    public HeartbeatService() {
        super("HeartbeatService");
    }

    @Override
    protected void onHandleIntent(Intent jobIntent) {

        // Gets data from the incoming Intent
        String dataString = jobIntent.getDataString();
        // Do work here, based on the contents of dataString
        Log.i(TAG, dataString);


        Log.i("AlarmReceiver", "Completed service @ " + SystemClock.elapsedRealtime());
        AlarmReceiver.completeWakefulIntent(jobIntent);

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
            Log.i(TAG, "Service schedule set");
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
            Log.i(TAG, "Service schedule unset");
        }

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(HeartbeatService.PREF_IS_ALARM_ON, isOn)
                .commit();

    }
}
