package de.rwth.comsys.samrad.preserv.utilz;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.android.gms.maps.model.LatLng;
import de.rwth.comsys.samrad.preserv.R;
import de.rwth.comsys.samrad.preserv.model.Poly;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Utilz {

    /**
     * Truncates a floating point *x_f* to *d* decimal places,
     * then scales it up to factor 10**(nd).
     * i.e. the result can be safely used in a product with *d*
     * decimal places of accuracy and at maximum *n* factors.
     *
     * Note: the input array is edited in-place
     */
    public static long[] tns(long[] x, int d, int n) {
        int index = 0;
        for (long x_i : x) {
            x[index++] = ((int) (x_i * Math.pow(10, d))) * (long) (Math.pow(10, (n * d - d)));
        }
        return x;
    }

    /**
     * Parse a JSONObject and return a list of polys
     *
     * @param json JSONObject
     * @return List<Poly>
     */
    public static List<Poly> json2Poly(JSONObject json) {

        List<Poly> result = new ArrayList<Poly>();

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
                }

                // Add to ploy collection
                result.add(p);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Read the JSON file and return string.
     *
     * @param path
     * @return JSON string
     */
    public static String readJSON(String path) {

        File file = new File(path);
        StringBuilder sb = new StringBuilder(64);

        try {

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static void updatePref(Context context, int key,
                                  Class<?> cls, String value) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        if (cls == String.class) {
            sp.edit().putString(context.getString(key), value).commit();
        } else if (cls == boolean.class) {
            sp.edit().putBoolean(context.getString(key), Boolean.parseBoolean(value)).commit();
        } else if (cls == int.class) {
            sp.edit().putInt(context.getString(key), Integer.valueOf(value)).commit();
        }
    }

    public static String[] getPrivacyPeersIp(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String ip1 = sp.getString("pref_privacypeer1",
                context.getString(R.string.pref_privacypeer_default1)).split(":")[0];
        String ip2 = sp.getString("pref_privacypeer2",
                context.getString(R.string.pref_privacypeer_default2)).split(":")[0];
        String ip3 = sp.getString("pref_privacypeer3",
                context.getString(R.string.pref_privacypeer_default3)).split(":")[0];

        String[] result = {ip1, ip2, ip3};
        return result;
    }

    public static int[] getPrivacyPeersPort(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String port1 = sp.getString("pref_privacypeer1",
                context.getString(R.string.pref_privacypeer_default1)).split(":")[1];
        String port2 = sp.getString("pref_privacypeer2",
                context.getString(R.string.pref_privacypeer_default2)).split(":")[1];
        String port3 = sp.getString("pref_privacypeer3",
                context.getString(R.string.pref_privacypeer_default3)).split(":")[1];

        int[] result = {Integer.parseInt(port1),
                Integer.parseInt(port2),
                Integer.parseInt(port3)};
        return result;
    }

    public static long[] getPoiMask(Context context) {

        // TODO to be completed
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return null;
    }
}
