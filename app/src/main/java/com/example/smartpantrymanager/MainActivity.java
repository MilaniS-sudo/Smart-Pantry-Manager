package com.example.smartpantrymanager;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(dp(24), dp(24), dp(24), dp(24));
        layout.setBackgroundColor(0xFFF7F5EF);

        TextView title = new TextView(this);
        title.setText("Smart Pantry Manager");
        title.setTextSize(27);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF285943);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Manage your ingredients. Discover what you can cook.");
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(12), 0, dp(24));

        layout.addView(title);
        layout.addView(subtitle);

        addButton(layout, "My Pantry", PantryActivity.class);
        addButton(layout, "Suggested Recipes", RecipesActivity.class);
        addButton(layout, "Settings", SettingsActivity.class);

        setContentView(layout);
    }

    private void addButton(LinearLayout layout, String label,
                           Class<?> destination) {
        Button button = new Button(this);
        button.setText(label);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, dp(52));
        params.setMargins(0, dp(8), 0, dp(8));
        layout.addView(button, params);

        button.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, destination)));
    }
}
