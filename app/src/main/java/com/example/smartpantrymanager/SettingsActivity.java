package com.example.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

/**
 * Settings screen. Preferences are saved with SharedPreferences,
 * so they are kept after the app is closed and reopened.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "smart_pantry_settings";
    private static final String KEY_SHOW_ALMOST_THERE = "show_almost_there";
    private static final String KEY_CONFIRM_DELETE = "confirm_delete";

    // ---------- Static helpers so other screens can read the settings ----------

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isAlmostThereEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_SHOW_ALMOST_THERE, true);
    }

    public static boolean isConfirmDeleteEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_CONFIRM_DELETE, true);
    }

    // ---------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getPrefs(this);

        // "Almost there" toggle - read by RecipesActivity
        SwitchCompat switchAlmostThere = findViewById(R.id.switchAlmostThere);
        switchAlmostThere.setChecked(isAlmostThereEnabled(this));
        switchAlmostThere.setOnCheckedChangeListener((button, isChecked) ->
                prefs.edit().putBoolean(KEY_SHOW_ALMOST_THERE, isChecked).apply());

        // Delete confirmation toggle - read by PantryActivity
        SwitchCompat switchConfirmDelete = findViewById(R.id.switchConfirmDelete);
        switchConfirmDelete.setChecked(isConfirmDeleteEnabled(this));
        switchConfirmDelete.setOnCheckedChangeListener((button, isChecked) ->
                prefs.edit().putBoolean(KEY_CONFIRM_DELETE, isChecked).apply());

        // Simple profile stats from the database
        PantryDatabase database = new PantryDatabase(this);
        TextView textStats = findViewById(R.id.textStats);
        textStats.setText("Ingredients in pantry: " + database.getIngredientList().size()
                + "\nRecipes in database: " + database.getRecipeCount());
        database.close();
    }
}
