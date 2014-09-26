package de.rwth.comsys.samrad.preserv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Time;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import de.rwth.comsys.samrad.preserv.service.JSONDownloader;
import de.rwth.comsys.samrad.preserv.model.Poly;
import de.rwth.comsys.samrad.preserv.service.PulseService;
import de.rwth.comsys.samrad.preserv.utilz.Referable;
import de.rwth.comsys.samrad.preserv.utilz.Utilz;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends Activity implements
        Referable,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MAIN_ACTIVITY";
    // To be removed
    private List<Poly> mPolyList = new ArrayList<Poly>();

    // UI widgets
    SharedPreferences mSp;
    private Switch mHrt;
    private TextView mTerminal;
    private ScrollView mScrollView;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);

        // This Activity listens to preference changes
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(this);

        // Get reference to UI widgets
        prepareWidgets();

//        PreferenceManager.getDefaultSharedPreferences(this).
//                registerOnSharedPreferenceChangeListener(this);
//
//        boolean checked = PreferenceManager.getDefaultSharedPreferences(this).
//                getBoolean(getString(R.string.is_schedule_on), false);
//
//        mHrt.setChecked(checked);
//        mHrt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                if(isChecked) {
//                    PulseService.setServiceAlarm(MainActivity.this, true);
//                } else {
//                    Intent service = new Intent(MainActivity.this, PulseService.class);
//                    stopService(service);
//                    PulseService.setServiceAlarm(MainActivity.this, false);
//                }
//            }
//        });


        // Read the JSON file is it already exists
        if(jsonExist()) {

            String json = Utilz.readJSON(getFilesDir() + "/polygons.json");
            onJSONString(json);
            Log.i(TAG, "Reading from internal storage");

        // Download the JSON otherwise
        } else if (hasConnectivity()) {

            final JSONDownloader d = new JSONDownloader(this, this);
            d.execute("http://preserv-samrad.rhcloud.com/monkey");
            Log.i(TAG, "Reading from server");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        mMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        if (mPolyList.size() <= 0) {
            menu.findItem(R.id.action_settings).setEnabled(false);
        }

        boolean checked = mSp.getBoolean(getString(R.string.pref_is_schedule_on), false);
        if (checked) {
            menu.findItem(R.id.action_pulse).setIcon(R.drawable.ic_action_pulse_on);
        } else {
            menu.findItem(R.id.action_pulse).setIcon(R.drawable.ic_action_pulse);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Start settings activity
        if (id == R.id.action_settings) {

            // The names of the POIs are sent to preference activity
            // to populate the MultiSelectListPreference.
            ArrayList<String> prefPolys = new ArrayList<String>(mPolyList.size());
            for (Poly p: mPolyList) {
                prefPolys.add(p.getName());
            }

            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            i.putStringArrayListExtra("polys", prefPolys);
            startActivity(i);
            return true;

        // Set or unset the schedule to run service based on preference
        } else if (id == R.id.action_pulse) {

            boolean checked = mSp.getBoolean(getString(R.string.pref_is_schedule_on), false);
            if (!checked) {
                PulseService.setServiceAlarm(MainActivity.this, true);
                item.setIcon(R.drawable.ic_action_pulse_on);
            } else {
                Intent service = new Intent(MainActivity.this, PulseService.class);
                stopService(service);
                PulseService.setServiceAlarm(MainActivity.this, false);
                item.setIcon(R.drawable.ic_action_pulse);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onJSONString(String s) {
        try {

            JSONObject json = new JSONObject(s);
            mPolyList = Utilz.json2Poly(json);

            // Enable the setting button once
            // the JSON is ready.
            if (mMenu != null) {
                mMenu.findItem(R.id.action_settings).setEnabled(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean hasConnectivity() {

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            Log.i(TAG, "Device has connectivity");
            return true;
        }

        return false;
    }

    private boolean jsonExist() {

        File file = new File(getFilesDir(), "polygons.json");
        Log.i(TAG, "File exist? " + (file.exists() ? "yes" : "no"));
        return file.exists();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Shared preferences changed on key: " + key);

        // Update terminal only on specified keys
        List<String> filter = new ArrayList<String>();
        filter.add(getString(R.string.pref_is_schedule_on));
        filter.add(getString(R.string.pref_last_found_location));
        filter.add(getString(R.string.pref_secret));
        filter.add(getString(R.string.pref_no_of_polygons));
        filter.add(getString(R.string.pref_shares));
        if (filter.contains(key)) {
            updateTerminal();
        }

    }

    private void updateTerminal() {

        // 0xFF6699CC Blue     // 0xFF699aca
        // 0xFF66CCCC Cyan     //
        // 0xFFF99157 Orange   // 0xFFd07b55
        // 0xFFFFCC66 Yellow   // 0xFFfecb6f
        // 0xFFCC99CC Purple   // 0xFFca9bc5
        // 0xFFF2777A Red      // 0xFFf1777c
        // 0xFF99CC99 Green    // 0xFF9aca93
        // 0xFF747469 Grey

        // Timestamp
        Time now = new Time();
        now.setToNow();
        SpannableString time = new SpannableString(now.format("%F @%T") + "\n");
        time.setSpan(new ForegroundColorSpan(0xFF747469), 0, time.length(), 0);

        // Service scheduled?
        SpannableString scheduleLabel = new SpannableString(getString(R.string.str_service_schedule));
        SpannableString scheduleValue = new SpannableString(mSp.getBoolean(getString(R.string.pref_is_schedule_on), false) + "\n");
        scheduleValue.setSpan(new ForegroundColorSpan(0xFF699aca), 0, scheduleValue.length(), 0);

        // # of polygons
        SpannableString polyLabel = new SpannableString(getString(R.string.str_no_of_polygons));
        SpannableString polyValue = new SpannableString(mSp.getString(getString(R.string.pref_no_of_polygons), "0") + "\n");
        polyValue.setSpan(new ForegroundColorSpan(0xFF9aca93), 0, polyValue.length(), 0);

//        sb.append(getString(R.string.str_no_of_polygons) +
//                mSp.getInt(getString(R.string.str_no_of_polygons), 0) + "\n");

        // Last found location
        SpannableString locLabel = new SpannableString(getString(R.string.str_last_found_location));
        SpannableString locValue = new SpannableString(mSp.getString(getString(R.string.pref_last_found_location), "unknown") + "\n");
        locValue.setSpan(new ForegroundColorSpan(0xFFca9bc5), 0, locValue.length(), 0);

//        sb.append(getString(R.string.str_last_found_location) +
//                mSp.getString(getString(R.string.last_found_location), "unknown") + "\n");

        // Secret
        SpannableString secretLabel = new SpannableString(getString(R.string.str_secret));
        SpannableString secretValue = new SpannableString(mSp.getString(getString(R.string.pref_secret), "unknown") + "\n");
        secretValue.setSpan(new ForegroundColorSpan(0xFFf1777c), 0, secretValue.length(), 0);

//        sb.append(getString(R.string.str_secret) +
//                mSp.getString(getString(R.string.secret), "nothing to hide") + "\n");

        // TODO Shares

        // Append the decorated text to terminal TextView
        mTerminal.append(TextUtils.concat(
                "\n", time,
                      scheduleLabel, " ", scheduleValue,
                      polyLabel    , " ", polyValue    ,
                      locLabel     , " ", locValue     ,
                      secretLabel  , " ", secretValue
        ));

        // Scroll down on TextView update
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void prepareWidgets() {

        // Reference to SharedPreferences
        mSp = PreferenceManager.getDefaultSharedPreferences(this);

        // Switch button
        mHrt = (Switch) findViewById(R.id.switch1);
        boolean checked = mSp.getBoolean(getString(R.string.pref_is_schedule_on), false);
        mHrt.setChecked(checked);
        mHrt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    PulseService.setServiceAlarm(MainActivity.this, true);
                } else {
                    Intent service = new Intent(MainActivity.this, PulseService.class);
                    stopService(service);
                    PulseService.setServiceAlarm(MainActivity.this, false);
                }
            }
        });

        // TextView as Terminal
        mTerminal = (TextView) findViewById(R.id.tv_terminal);

        // ScrollView
        mScrollView = (ScrollView) findViewById(R.id.sv_container);
    }
}
