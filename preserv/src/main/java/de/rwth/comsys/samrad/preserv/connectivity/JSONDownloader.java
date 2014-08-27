package de.rwth.comsys.samrad.preserv.connectivity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
    private Context ctx;

    public JSONDownloader(Context context) {
        super();
        this.ctx = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... urls) {

        try {
            String shit = getJSON(urls[0]);
            Log.d(TAG, shit);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

            publishProgress(total);
            return sb.toString();


        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
