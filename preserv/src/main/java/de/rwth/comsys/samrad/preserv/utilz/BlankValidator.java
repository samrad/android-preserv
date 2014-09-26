package de.rwth.comsys.samrad.preserv.utilz;

import android.app.AlertDialog;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;

/**
 * This TextWatcher matches the value for pulse frequency
 * EditTextPreferences to prevent blank entry or zero values.
 *
 */
public class BlankValidator implements TextWatcher {

    private final EditTextPreference target;
    public BlankValidator(EditTextPreference target) {
        this.target = target;
    }

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

        // Disable the OK button if blank or zero
        if(s.length() <= 0 || s.toString().equals("0")) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }

    }
}
