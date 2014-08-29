package de.rwth.comsys.samrad.preserv;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import de.rwth.comsys.samrad.preserv.connectivity.JSONDownloader;
import de.rwth.comsys.samrad.preserv.model.Poly;
import de.rwth.comsys.samrad.preserv.utilz.Referable;
import de.rwth.comsys.samrad.preserv.utilz.Utilz;
import org.json.JSONException;
import org.json.JSONObject;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements Referable {

    private static final String TAG = "MainActivity";
    private List<Poly> mPolyList = new ArrayList<Poly>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);

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
        Log.i(TAG, "File exit? " + String.valueOf(file.exists()));
        return file.exists();
    }

}
