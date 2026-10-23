package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database for the app. Three tables:
 *  - ingredients         : the user's pantry (full CRUD)
 *  - recipes             : recipes seeded on first run
 *  - recipe_ingredients  : the ingredients each recipe needs
 */
public class PantryDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    // Version 2 adds the recipes and recipe_ingredients tables
            private static final int DB_VERSION = 2;

    public PantryDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                                "CREATE TABLE ingredients (" +
                                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                        "name TEXT NOT NULL, " +
                                        "quantity REAL NOT NULL, " +
                                        "unit TEXT NOT NULL)"
                        );

        db.execSQL(
                                "CREATE TABLE recipes (" +
                                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                        "name TEXT NOT NULL, " +
                                        "steps TEXT NOT NULL)"
                        );

        db.execSQL(
                                "CREATE TABLE recipe_ingredients (" +
                                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                        "recipe_id INTEGER NOT NULL, " +
                                        "name TEXT NOT NULL, " +
                                        "quantity REAL NOT NULL, " +
                                        "unit TEXT NOT NULL, " +
                                        "FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE)"
                        );

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple strategy for a student project: rebuild all tables
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS ingredients");
        onCreate(db);
    }

    // =====================================================
            // INGREDIENTS - CRUD
            // =====================================================

            /** CREATE */
            public boolean addIngredient(String name, double quantity, String unit) {
        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("quantity", quantity);
        values.put("unit", unit.trim());

        return getWritableDatabase().insert("ingredients", null, values) != -1;
    }

    /** READ - all ingredients as a Cursor (kept for any screen that already uses it) */
            public Cursor getIngredients() {
        return getReadableDatabase().rawQuery(
                                "SELECT id, name, quantity, unit FROM ingredients ORDER BY name COLLATE NOCASE",
                                null
        );
    }

    /** READ - all ingredients as a List, used by the RecyclerView adapter */
            public List<Ingredient> getIngredientList() {
        List<Ingredient> list = new ArrayList<>();
        try (Cursor cursor = getIngredients()) {
            while (cursor.moveToNext()) {
                list.add(cursorToIngredient(cursor));
            }
        }
        return list;
    }

    /** READ - one ingredient by id (null if it does not exist) */
            public Ingredient getIngredient(int id) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                                "SELECT id, name, quantity, unit FROM ingredients WHERE id = ?",
                                new String[]{String.valueOf(id)})) {
            if (cursor.moveToFirst()) {
                return cursorToIngredient(cursor);
            }
        }
        return null;
    }

    /** UPDATE - returns true if a row was changed */
            public boolean updateIngredient(int id, String name, double quantity, String unit) {
        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("quantity", quantity);
        values.put("unit", unit.trim());

        int rowsChanged = getWritableDatabase().update(
                                "ingredients", values, "id = ?",
                                new String[]{String.valueOf(id)}
        );
        return rowsChanged > 0;
    }

    /** DELETE */
            public int deleteIngredient(int id) {
        return getWritableDatabase().delete(
                                "ingredients", "id = ?", new String[]{String.valueOf(id)}
                        );
    }

    private Ingredient cursorToIngredient(Cursor cursor) {
        return new Ingredient(
                                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                                cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                                cursor.getString(cursor.getColumnIndexOrThrow("unit"))
                        );
    }

    // =====================================================
            // RECIPES - read only (seeded below)
            // =====================================================

            /** All recipes: columns id, name, steps */
            public Cursor getRecipes() {
        return getReadableDatabase().rawQuery(
                                "SELECT id, name, steps FROM recipes ORDER BY name COLLATE NOCASE",
                                null
        );
    }

    /** One recipe: columns id, name, steps */
            public Cursor getRecipe(int recipeId) {
        return getReadableDatabase().rawQuery(
                                "SELECT id, name, steps FROM recipes WHERE id = ?",
                                new String[]{String.valueOf(recipeId)}
        );
    }

    /** Ingredients needed by one recipe: columns name, quantity, unit */
            public Cursor getRecipeIngredients(int recipeId) {
        return getReadableDatabase().rawQuery(
                                "SELECT name, quantity, unit FROM recipe_ingredients WHERE recipe_id = ?",
                                new String[]{String.valueOf(recipeId)}
        );
    }

    public long getRecipeCount() {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), "recipes");
    }

    // =====================================================
            // SEED DATA - runs once, inside onCreate
            // Units: dry goods in g, liquids in ml, countable items in pcs
            // =====================================================

            private void seedRecipes(SQLiteDatabase db) {
        insertRecipe(db, "Scrambled Eggs",
                                "1. Whisk the eggs and milk.\n2. Melt the butter in a pan on low heat.\n3. Stir the eggs gently until just set.",
                                new String[][]{{"eggs", "3", "pcs"}, {"milk", "50", "ml"}, {"butter", "10", "g"}});

        insertRecipe(db, "Cheese Omelette",
                                "1. Beat the eggs.\n2. Pour into a buttered pan.\n3. Add grated cheese, fold and serve.",
                                new String[][]{{"eggs", "3", "pcs"}, {"cheese", "40", "g"}, {"butter", "10", "g"}});

        insertRecipe(db, "Cheese and Tomato Toastie",
                                "1. Butter the outside of the bread.\n2. Fill with cheese and sliced tomato.\n3. Toast in a pan until golden on both sides.",
                                new String[][]{{"bread", "2", "pcs"}, {"cheese", "50", "g"}, {"tomatoes", "1", "pcs"}, {"butter", "10", "g"}});

        insertRecipe(db, "French Toast",
                                "1. Whisk the eggs and milk.\n2. Soak the bread slices in the mixture.\n3. Fry in butter until golden.",
                                new String[][]{{"bread", "2", "pcs"}, {"eggs", "2", "pcs"}, {"milk", "60", "ml"}, {"butter", "10", "g"}});

        insertRecipe(db, "Pancakes",
                                "1. Mix the flour and sugar.\n2. Whisk in the egg and milk.\n3. Cook spoonfuls in a buttered pan, flipping once.",
                                new String[][]{{"flour", "120", "g"}, {"eggs", "1", "pcs"}, {"milk", "250", "ml"}, {"sugar", "15", "g"}, {"butter", "15", "g"}});

        insertRecipe(db, "Banana Oat Smoothie",
                                "1. Slice the banana.\n2. Blend with the oats and milk until smooth.",
                                new String[][]{{"bananas", "1", "pcs"}, {"oats", "30", "g"}, {"milk", "250", "ml"}});

        insertRecipe(db, "Tomato Pasta",
                                "1. Cook the pasta and drain.\n2. Fry the onion and garlic in oil.\n3. Add chopped tomatoes and simmer 10 minutes.\n4. Mix with the pasta.",
                                new String[][]{{"pasta", "200", "g"}, {"tomatoes", "3", "pcs"}, {"onions", "1", "pcs"}, {"garlic", "2", "pcs"}, {"oil", "15", "ml"}});

        insertRecipe(db, "Macaroni and Cheese",
                                "1. Cook the pasta.\n2. Melt the butter, stir in the flour, then whisk in the milk.\n3. Add the cheese and mix with the pasta.",
                                new String[][]{{"pasta", "200", "g"}, {"cheese", "100", "g"}, {"milk", "250", "ml"}, {"butter", "20", "g"}, {"flour", "20", "g"}});

        insertRecipe(db, "Spaghetti Bolognese",
                                "1. Fry the onion and garlic in oil.\n2. Brown the mince.\n3. Add tomatoes and simmer 20 minutes.\n4. Serve over the pasta.",
                                new String[][]{{"pasta", "200", "g"}, {"beef mince", "400", "g"}, {"tomatoes", "3", "pcs"}, {"onions", "1", "pcs"}, {"garlic", "2", "pcs"}, {"oil", "15", "ml"}});

        insertRecipe(db, "Vegetable Fried Rice",
                                "1. Cook the rice and let it cool.\n2. Fry the onion and carrot in oil.\n3. Scramble in the eggs.\n4. Add the rice and stir-fry 3 minutes.",
                                new String[][]{{"rice", "200", "g"}, {"eggs", "2", "pcs"}, {"carrots", "1", "pcs"}, {"onions", "1", "pcs"}, {"oil", "30", "ml"}});

        insertRecipe(db, "Chicken and Rice",
                                "1. Cook the rice.\n2. Fry the diced chicken and onion in oil until cooked.\n3. Serve over the rice.",
                                new String[][]{{"chicken breast", "400", "g"}, {"rice", "200", "g"}, {"onions", "1", "pcs"}, {"oil", "15", "ml"}});

        insertRecipe(db, "Mashed Potatoes",
                                "1. Boil the peeled potatoes until soft.\n2. Drain and mash.\n3. Beat in the butter and milk.",
                                new String[][]{{"potatoes", "4", "pcs"}, {"butter", "30", "g"}, {"milk", "100", "ml"}});

        insertRecipe(db, "Potato and Egg Hash",
                                "1. Dice the potatoes and onion.\n2. Fry in oil until crispy.\n3. Crack in the eggs and cover until set.",
                                new String[][]{{"potatoes", "3", "pcs"}, {"onions", "1", "pcs"}, {"eggs", "2", "pcs"}, {"oil", "30", "ml"}});

        insertRecipe(db, "Beans on Toast",
                                "1. Heat the baked beans.\n2. Toast and butter the bread.\n3. Pour the beans over the toast.",
                                new String[][]{{"baked beans", "1", "pcs"}, {"bread", "2", "pcs"}, {"butter", "10", "g"}});

        insertRecipe(db, "Pap and Tomato Relish",
                                "1. Boil 750 ml water and stir in the maize meal.\n2. Cover and cook on low for 20 minutes.\n3. Fry the onion in oil, add tomatoes and simmer 10 minutes.\n4. Serve the relish over the pap.",
                                new String[][]{{"maize meal", "250", "g"}, {"tomatoes", "3", "pcs"}, {"onions", "1", "pcs"}, {"oil", "15", "ml"}});

        insertRecipe(db, "Garlic Butter Rice",
                                "1. Cook the rice.\n2. Fry the crushed garlic in butter for 1 minute.\n3. Stir through the rice.",
                                new String[][]{{"rice", "200", "g"}, {"butter", "20", "g"}, {"garlic", "3", "pcs"}});
    }

    /** Inserts one recipe, then each of its ingredients linked by recipe_id. */
            private void insertRecipe(SQLiteDatabase db, String name, String steps,
                              String[][] ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("steps", steps);
        long recipeId = db.insert("recipes", null, recipeValues);

        for (String[] ingredient : ingredients) {
            ContentValues values = new ContentValues();
            values.put("recipe_id", recipeId);
            values.put("name", ingredient[0]);
            values.put("quantity", Double.parseDouble(ingredient[1]));
            values.put("unit", ingredient[2]);
            db.insert("recipe_ingredients", null, values);
        }
    }
}