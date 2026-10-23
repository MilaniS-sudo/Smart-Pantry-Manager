package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Pantry List screen: shows every saved ingredient in a RecyclerView.
 * Tap a row to edit it, tap Delete to remove it, tap Add to create a new one.
 */
public class PantryActivity extends AppCompatActivity
        implements IngredientAdapter.OnIngredientClickListener {

    private PantryDatabase database;
    private IngredientAdapter adapter;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        database = new PantryDatabase(this);
        emptyText = findViewById(R.id.textEmptyPantry);

        // RecyclerView + custom adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerIngredients);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IngredientAdapter(this);
        recyclerView.setAdapter(adapter);

        // Explicit Intent with no extra = "add new ingredient" mode
        Button addButton = findViewById(R.id.buttonAddIngredient);
        addButton.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Runs every time we come back from the Add/Edit screen,
        // so the list always shows the latest database data
        refreshIngredients();
    }

    private void refreshIngredients() {
        List<Ingredient> ingredients = database.getIngredientList();
        adapter.setIngredients(ingredients);
        emptyText.setVisibility(ingredients.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Row tapped: open the edit screen and pass the ingredient id in the Intent. */
    @Override
    public void onIngredientClick(Ingredient ingredient) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_INGREDIENT_ID, ingredient.getId());
        startActivity(intent);
    }

    /** Delete tapped: confirm first, then remove from the database. */
    @Override
    public void onDeleteClick(Ingredient ingredient) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + ingredient.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    database.deleteIngredient(ingredient.getId());
                    refreshIngredients();
                    Toast.makeText(this, "Ingredient deleted.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
