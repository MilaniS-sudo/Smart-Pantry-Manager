
package com.example.smartpantrymanager;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("Settings & Profile");
        title.setTextSize(24);

        TextView details = new TextView(this);
        details.setText(
                "Smart Pantry Manager\n\n" +
                        "Manage your pantry and reduce food waste.\n\n" +
                        "Version 1.0"
        );
        details.setTextSize(18);

        layout.addView(title);
        layout.addView(details);

        setContentView(layout);
    }
}
