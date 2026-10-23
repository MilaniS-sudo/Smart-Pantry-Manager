package com.example.smartpantrymanager;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

/** Recipe Detail screen: full ingredient list and method for one recipe. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        PantryDatabase database = new PantryDatabase(this);

        // Read the id that RecipesActivity put in the Intent
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = database.getRecipeById(recipeId);
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found.", Toast.LENGTH_SHORT).show();
            database.close();
            finish();
            return;
        }

        TextView textName = findViewById(R.id.textDetailName);
        TextView textStatus = findViewById(R.id.textDetailStatus);
        TextView textIngredients = findViewById(R.id.textDetailIngredients);
        TextView textSteps = findViewById(R.id.textDetailSteps);

        textName.setText(recipe.getName());
        textSteps.setText(recipe.getSteps());

        // Tick what the user has, cross what is missing
        Map<String, Double> pantryIndex =
                IngredientMatcher.buildPantryIndex(database.getIngredientList());
        StringBuilder list = new StringBuilder();
        int missingCount = 0;
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            boolean have = IngredientMatcher.hasEnough(pantryIndex, ingredient);
            if (!have) {
                missingCount++;
            }
            list.append(have ? "\u2713  " : "\u2717  ")
                    .append(ingredient.getDisplayText());
            if (!have) {
                list.append("  (missing)");
            }
            list.append("\n");
        }
        textIngredients.setText(list.toString().trim());

        if (missingCount == 0) {
            textStatus.setText("You can cook this right now!");
            textStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            textStatus.setText("You are missing " + missingCount + " ingredient(s)");
            textStatus.setTextColor(Color.parseColor("#E65100"));
        }

        database.close();
    }
}
