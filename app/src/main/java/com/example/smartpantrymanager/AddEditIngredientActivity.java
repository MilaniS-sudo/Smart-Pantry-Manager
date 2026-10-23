package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Add/Edit Ingredient screen.
 * No id in the Intent = ADD mode. An id in the Intent = EDIT mode.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_INGREDIENT_ID = "ingredient_id";
    private static final String[] UNITS = {"pcs", "g", "kg", "ml", "l"};

    private PantryDatabase database;
    private EditText nameInput;
    private EditText quantityInput;
    private Spinner unitSpinner;
    private int ingredientId = -1; // -1 means "adding a new ingredient"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        database = new PantryDatabase(this);
        nameInput = findViewById(R.id.editIngredientName);
        quantityInput = findViewById(R.id.editIngredientQuantity);
        unitSpinner = findViewById(R.id.spinnerUnit);
        TextView title = findViewById(R.id.textFormTitle);
        Button saveButton = findViewById(R.id.buttonSaveIngredient);

        List<String> units = new ArrayList<>(Arrays.asList(UNITS));

        // Read the id that PantryActivity put in the Intent (if any)
        ingredientId = getIntent().getIntExtra(EXTRA_INGREDIENT_ID, -1);
        Ingredient existing = null;
        if (ingredientId != -1) {
            existing = database.getIngredient(ingredientId);
            if (existing == null) {
                Toast.makeText(this, "Ingredient not found.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            // Older items may have a typed unit that is not in the list
            if (!units.contains(existing.getUnit())) {
                units.add(existing.getUnit());
            }
        }

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);

        if (existing != null) {
            title.setText("Edit Ingredient");
            saveButton.setText("Save Changes");
            // Only pre-fill on first creation; after rotation the EditTexts restore themselves
            if (savedInstanceState == null) {
                nameInput.setText(existing.getName());
                quantityInput.setText(existing.getQuantityText());
                unitSpinner.setSelection(units.indexOf(existing.getUnit()));
            }
        } else {
            title.setText("Add Ingredient");
            saveButton.setText("Add Ingredient");
        }

        saveButton.setOnClickListener(v -> saveIngredient());
    }

    /** Validates the form, then either inserts or updates. */
            private void saveIngredient() {
        String name = nameInput.getText().toString().trim();
        String quantityText = quantityInput.getText().toString().trim().replace(',', '.');
        String unit = unitSpinner.getSelectedItem().toString();

        // ---- Validation: errors appear under each field ----
        boolean valid = true;

        if (name.isEmpty()) {
            nameInput.setError("Please enter an ingredient name");
            valid = false;
        } else if (!name.matches(".*[a-zA-Z].*")) {
            nameInput.setError("Name must contain letters");
            valid = false;
        } else if (name.length() > 40) {
            nameInput.setError("Name is too long (max 40 characters)");
            valid = false;
        }

        double quantity = 0;
        if (quantityText.isEmpty()) {
            quantityInput.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(quantityText);
                if (!Double.isFinite(quantity) || quantity <= 0) {
                    quantityInput.setError("Quantity must be greater than zero");
                    valid = false;
                } else if (quantity > 100000) {
                    quantityInput.setError("Quantity is too large");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                quantityInput.setError("Enter a valid number");
                valid = false;
            }
        }

        if (!valid) {
            return;
        }

        // ---- Save: CREATE or UPDATE ----
        boolean success;
        if (ingredientId == -1) {
            success = database.addIngredient(name, quantity, unit);
        } else {
            success = database.updateIngredient(ingredientId, name, quantity, unit);
        }

        if (success) {
            Toast.makeText(this,
                                        ingredientId == -1 ? "Ingredient saved!" : "Ingredient updated!",
                                        Toast.LENGTH_SHORT).show();
            finish(); // back to PantryActivity, whose onResume refreshes the list
        } else {
            Toast.makeText(this, "Could not save ingredient.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}