package de.rwth.comsys.samrad.preserv.utilz;

import com.google.android.gms.maps.model.LatLng;
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
}
