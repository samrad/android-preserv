package de.rwth.comsys.samrad.preserv.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * This broadcast receiver is triggered by AlarmManager
 * and start the service while keeping the CPU awake.
 *
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

//        // The Intent to be delivered to service.
//        Intent service = new Intent(context, HeartbeatService.class);
//        service.setData(Uri.parse("fuck"));

        // The Intent to be delivered to service.
        Intent service = new Intent(context, PulseService.class);

        // Start the service and stay awake till it's finished
        Log.i("ALARM_RECEIVER", "Starting service @" + SystemClock.elapsedRealtime());
        startWakefulService(context, service);
    }
}
