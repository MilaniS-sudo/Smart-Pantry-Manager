package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Home screen. Navigation is available two ways:
 *  - the toolbar options menu (top right)
 *  - the three buttons in the middle of the screen
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private PantryDatabase database;
    private TextView textSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate - building the screen");

        database = new PantryDatabase(this);
        textSummary = findViewById(R.id.textPantrySummary);

        // Toolbar menu = the app's navigation element
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener(this::onMenuItemSelected);

        // Buttons use explicit Intents to open each screen
        findViewById(R.id.buttonPantry).setOnClickListener(v -> open(PantryActivity.class));
        findViewById(R.id.buttonRecipes).setOnClickListener(v -> open(RecipesActivity.class));
        findViewById(R.id.buttonSettings).setOnClickListener(v -> open(SettingsActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart - screen becoming visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - refreshing pantry summary");
        // Runs every time the user comes back here, so the numbers are always current
        int ingredientCount = database.getIngredientList().size();
        long recipeCount = database.getRecipeCount();
        textSummary.setText("You have " + ingredientCount + " ingredient(s) in your pantry\n"
                + recipeCount + " recipes available in the database");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop - screen no longer visible");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy - closing database");
        database.close();
        super.onDestroy();
    }

    /** Handles taps on the toolbar menu items. */
    private boolean onMenuItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_pantry) {
            open(PantryActivity.class);
            return true;
        } else if (id == R.id.menu_recipes) {
            open(RecipesActivity.class);
            return true;
        } else if (id == R.id.menu_settings) {
            open(SettingsActivity.class);
            return true;
        }
        return false;
    }

    private void open(Class<?> destination) {
        startActivity(new Intent(this, destination));
    }
}
