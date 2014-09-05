package de.rwth.comsys.samrad.preserv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import de.rwth.comsys.samrad.preserv.service.HeartbeatService;
import de.rwth.comsys.samrad.preserv.service.JSONDownloader;
import de.rwth.comsys.samrad.preserv.model.Poly;
import de.rwth.comsys.samrad.preserv.utilz.Referable;
import de.rwth.comsys.samrad.preserv.utilz.Utilz;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements Referable {

    private static final String TAG = "MainActivity";
    private List<Poly> mPolyList = new ArrayList<Poly>();

    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);

        final Switch hrt = (Switch) findViewById(R.id.switch1);
        hrt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    HeartbeatService.setServiceAlarm(MainActivity.this, true);
                } else {
                    HeartbeatService.setServiceAlarm(MainActivity.this, false);
                }
            }
        });


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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onJSONString(String s) {
        try {
            JSONObject json = new JSONObject(s);
            mPolyList = Utilz.json2Poly(json);
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

}
