package de.rwth.comsys.samrad.preserv;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;
import de.rwth.comsys.samrad.preserv.connectivity.JSONDownloader;
import de.rwth.comsys.samrad.preserv.model.Poly;
import de.rwth.comsys.samrad.preserv.utilz.Referable;
import de.rwth.comsys.samrad.preserv.utilz.Utilz;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.msgpack.MessagePack;
import mpc.ShamirSharing;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements Referable {

    private List<Poly> mPolyList = new ArrayList<Poly>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);

        final JSONDownloader d = new JSONDownloader(this, this);
        d.execute("http://preserv-samrad.rhcloud.com/monkey");
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
//            test(json); // TEST
            mPolyList = Utilz.json2Poly(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void test(JSONObject json) {
        try {

            JSONArray polys = json.getJSONArray("polys");

            // For each poly in polys
            for (int i = 0; i < polys.length(); i++) {

                JSONObject poly = polys.getJSONObject(i);
                JSONArray vtx = poly.getJSONArray("vtx");
                Poly p = new Poly(poly.getString("name"));

                // For each object in vtx
                for (int j = 0; j < vtx.length(); j++) {
                    JSONObject v = vtx.getJSONObject(j);
                    p.addVertex(new LatLng(v.getDouble("lat"), v.getDouble("lng")));
                    Log.d("Fuck", String.valueOf(v.getDouble("lat")));
                }

                // Add to ploy collection
                mPolyList.add(p);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
