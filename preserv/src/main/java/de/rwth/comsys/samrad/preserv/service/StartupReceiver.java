package de.rwth.comsys.samrad.preserv.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * StartupReceiver triggers when the device completes
 * booting up and it will re-schedule the HeartBeat
 * service.
 */
public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isOn = prefs.getBoolean(HeartbeatService.PREF_IS_ALARM_ON, false);
        HeartbeatService.setServiceAlarm(context, isOn);

    }
}
