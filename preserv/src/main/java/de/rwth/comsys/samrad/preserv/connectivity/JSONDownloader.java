package de.rwth.comsys.samrad.preserv.connectivity;

import android.content.Context;
import android.os.AsyncTask;
import de.rwth.comsys.samrad.preserv.utilz.Referable;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An AsyncTask to request the JSON file from server
 *
 * @param: url of the REST
 * @return: JSON string
 */
public class JSONDownloader extends AsyncTask<String, Integer, String> {

    private static final String TAG = "JSONDownloader";
    private Referable listener;
    private Context ctx;
    private String result;

    public JSONDownloader(Context ctx, Referable l) {
        super();
        this.ctx      = ctx;
        this.listener = l;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... urls) {

        try {

            // Get the JSON and write to a file
            result = getJSON(urls[0]);
            File file = new File(ctx.getFilesDir(), "polygons.json");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(result.getBytes());
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        listener.onJSONString(result);

    }

    /**
     * Given a URL, establishes an HttpUrlConnection and retrieves
     * the web page content as an InputStream, which then is returned
     * as a string of JSON.
     *
     * @param: url - path of the file
     * @return: JSON string
     */
    private String getJSON(String path) throws IOException {

        InputStream in = null;
        Reader reader = null;
        StringBuilder sb = null;
        JSONObject polys = null;

        try {

            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            // Check for HTTP response code and return if it's not 200
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP "
                        + conn.getResponseCode() + " "
                        + conn.getResponseMessage();
            }

            in = conn.getInputStream();
            reader = new InputStreamReader(in, "UTF-8");
            sb = new StringBuilder(64);
            char[] buffer = new char[in.available()];

            int total = 0; // total read chars
            int count;     // # of chars read

            while ((count = reader.read(buffer)) != -1) {

                sb.append(buffer);
                total += count;

            }

        } finally {
            if (in != null) {
                in.close();
            }
        }

        return sb.toString();
    }
}
