package de.rwth.comsys.samrad.preserv.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import de.rwth.comsys.samrad.preserv.R;

/**
 * StartupReceiver triggers when the device completes
 * booting up and it will re-schedule the service.
 *
 */
public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isOn = prefs.getBoolean(context.getString(R.string.pref_is_schedule_on), false);
        PulseService.setServiceAlarm(context, isOn);

    }
}
