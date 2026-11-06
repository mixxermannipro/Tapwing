// SPDX-License-Identifier: GPL-3.0-only
package io.github.mixxermannipro.satzfink.smoke;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

public final class FixtureActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout fields = new LinearLayout(this);
        fields.setOrientation(LinearLayout.VERTICAL);
        fields.setPadding(24, 72, 24, 0);
        addField(fields, "Normal text", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT, 0);
        addField(fields, "Private text", InputType.TYPE_CLASS_TEXT, EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING);
        addField(fields, "Password", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, 0);
        addField(fields, "PIN", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD, 0);
        setContentView(fields);
    }

    private void addField(LinearLayout fields, String label, int type, int imeOptions) {
        EditText field = new EditText(this);
        field.setHint(label);
        field.setInputType(type);
        field.setImeOptions(imeOptions);
        fields.addView(field);
    }
}
