package de.rwth.comsys.samrad.preserv.utilz;

import android.app.AlertDialog;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;

import java.util.regex.Pattern;

/**
 * This TextWatcher matches the value for PrivacyPeers
 * EditTextPreferences with IP:PORT pattern to prevent
 * inserting invalid values.
 *
 * The pattern matches the following range: 0.0.0.0:0 - 255.255.255.255:65535
 */
public class IpValidator implements TextWatcher {

    private final EditTextPreference target;
    public IpValidator(EditTextPreference target) {
        this.target = target;
    }

    // Pattern to match IP address and port number. e.g. 127.0.0.1:9121
    Pattern p = Pattern.compile("\\b(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}" +
            "(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9]):" +
            "([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])\\b");

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        // The AlertDialog is created, on the fly, each time the
        // preference is opened. Before that, it's always null.
        AlertDialog dialog = (AlertDialog) target.getDialog();

        // If the dialog is not created yet, return.
        if (dialog == null) { return; }

        // If the inserted value doesn't match the pattern,
        // disable the OK button, or enable otherwise.
        if(!p.matcher(s).matches()) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
    }
}
