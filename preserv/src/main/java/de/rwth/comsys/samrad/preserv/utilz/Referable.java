package de.rwth.comsys.samrad.preserv.utilz;

/**
 * A callback interface to be called when
 * the AsyncTask is finished executing.
 */
public interface Referable {

    // Call on onPostExecute() of AsyncTask
    void onJSONString(String s);
}
