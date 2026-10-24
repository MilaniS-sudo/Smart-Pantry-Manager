package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Suggested Recipes screen.
 * Runs the strict-matching rule against the current pantry and shows
 * ONLY the recipes the user can cook right now.
 */
public class RecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private PantryDatabase database;
    private RecipeAdapter strictAdapter;
    private RecipeAdapter almostAdapter;
    private TextView textStrictHeader;
    private TextView textEmpty;
    private TextView textAlmostHeader;
    private RecyclerView recyclerAlmost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

        database = new PantryDatabase(this);
        textStrictHeader = findViewById(R.id.textStrictHeader);
        textEmpty = findViewById(R.id.textEmptyRecipes);
        textAlmostHeader = findViewById(R.id.textAlmostHeader);

        RecyclerView recyclerStrict = findViewById(R.id.recyclerStrict);
        recyclerStrict.setLayoutManager(new LinearLayoutManager(this));
        recyclerStrict.setNestedScrollingEnabled(false);
        strictAdapter = new RecipeAdapter(this);
        recyclerStrict.setAdapter(strictAdapter);

        recyclerAlmost = findViewById(R.id.recyclerAlmost);
        recyclerAlmost.setLayoutManager(new LinearLayoutManager(this));
        recyclerAlmost.setNestedScrollingEnabled(false);
        almostAdapter = new RecipeAdapter(this);
        recyclerAlmost.setAdapter(almostAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-run the matching every time this screen is shown,
        // in case the pantry changed
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<Ingredient> pantry = database.getIngredientList();
        List<Recipe> allRecipes = database.getAllRecipes();
        Map<String, Double> pantryIndex = IngredientMatcher.buildPantryIndex(pantry);

        // 1) STRICT suggestions - every ingredient available
        List<Recipe> strict = IngredientMatcher.findStrictMatches(allRecipes, pantry);
        List<String> strictSubtitles = new ArrayList<>();
        for (Recipe recipe : strict) {
            strictSubtitles.add("You have all " + recipe.getIngredients().size() + " ingredients");
        }
        strictAdapter.setData(strict, strictSubtitles);
        textStrictHeader.setText("Ready to cook (" + strict.size() + ")");

        if (strict.isEmpty()) {
            textEmpty.setText(pantry.isEmpty()
                    ? "Your pantry is empty - add some ingredients first."
                    : "No recipes match your pantry yet - add more ingredients.");
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            textEmpty.setVisibility(View.GONE);
        }

        // 2) BONUS - recipes missing exactly ONE ingredient, kept in a SEPARATE list
        List<Recipe> almost = IngredientMatcher.findAlmostThere(allRecipes, pantry);
        List<String> almostSubtitles = new ArrayList<>();
        for (Recipe recipe : almost) {
            List<String> missing = IngredientMatcher.getMissingIngredients(recipe, pantryIndex);
            almostSubtitles.add("Missing: " + missing.get(0));
        }
        almostAdapter.setData(almost, almostSubtitles);
        textAlmostHeader.setText("Almost there - missing 1 ingredient (" + almost.size() + ")");
        int almostVisibility = almost.isEmpty() ? View.GONE : View.VISIBLE;
        textAlmostHeader.setVisibility(almostVisibility);
        recyclerAlmost.setVisibility(almostVisibility);
    }

    /** Open the detail screen, passing the recipe id through the Intent. */
    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
