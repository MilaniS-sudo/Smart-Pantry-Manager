
package com.example.smartpantrymanager;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PantryActivity extends Activity {

    private PantryDatabase database;
    private EditText nameInput, quantityInput, unitInput;
    private LinearLayout ingredientList;

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = new PantryDatabase(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView title = new TextView(this);
        title.setText("My Pantry");
        title.setTextSize(26);
        layout.addView(title);

        nameInput = new EditText(this);
        nameInput.setHint("Ingredient name (e.g. Rice)");
        layout.addView(nameInput);

        quantityInput = new EditText(this);
        quantityInput.setHint("Quantity (e.g. 2)");
        quantityInput.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(quantityInput);

        unitInput = new EditText(this);
        unitInput.setHint("Unit (e.g. kg, g, pieces)");
        layout.addView(unitInput);

        Button addButton = new Button(this);
        addButton.setText("Add Ingredient");
        layout.addView(addButton);

        TextView listTitle = new TextView(this);
        listTitle.setText("Saved Ingredients");
        listTitle.setTextSize(20);
        listTitle.setPadding(0, dp(16), 0, dp(8));
        layout.addView(listTitle);

        ScrollView scrollView = new ScrollView(this);
        ingredientList = new LinearLayout(this);
        ingredientList.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(ingredientList);

        layout.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        setContentView(layout);

        addButton.setOnClickListener(v -> addIngredient());

        refreshIngredients();
    }

    private void addIngredient() {
        String name = nameInput.getText().toString().trim();
        String quantityText =
                quantityInput.getText().toString().trim();
        String unit = unitInput.getText().toString().trim();

        if (name.isEmpty() || quantityText.isEmpty()
                || unit.isEmpty()) {
            Toast.makeText(this,
                    "Please complete all fields.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double quantity = Double.parseDouble(quantityText);

            if (!Double.isFinite(quantity) || quantity <= 0) {
                Toast.makeText(this,
                        "Enter a quantity greater than zero.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            boolean saved = database.addIngredient(
                    name, quantity, unit);

            if (saved) {
                nameInput.setText("");
                quantityInput.setText("");
                unitInput.setText("");
                refreshIngredients();

                Toast.makeText(this, "Ingredient saved!",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Could not save ingredient.",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid quantity.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshIngredients() {
        ingredientList.removeAllViews();

        try (Cursor cursor = database.getIngredients()) {
            if (cursor.getCount() == 0) {
                TextView empty = new TextView(this);
                empty.setText("Your pantry is empty. Add an ingredient above.");
                ingredientList.addView(empty);
                return;
            }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                double quantity = cursor.getDouble(2);
                String unit = cursor.getString(3);

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);

                TextView details = new TextView(this);
                details.setText(name + " — " + quantity + " " + unit);
                details.setTextSize(16);

                row.addView(details, new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

                Button deleteButton = new Button(this);
                deleteButton.setText("Delete");
                deleteButton.setOnClickListener(v -> {
                    database.deleteIngredient(id);
                    refreshIngredients();
                    Toast.makeText(this, "Ingredient deleted.",
                            Toast.LENGTH_SHORT).show();
                });

                row.addView(deleteButton);
                ingredientList.addView(row);
            }
        }
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
