
package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PantryDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public PantryDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        // Database upgrades can be handled here.
    }

    public boolean addIngredient(String name, double quantity,
                                 String unit) {
        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("quantity", quantity);
        values.put("unit", unit.trim());

        SQLiteDatabase db = getWritableDatabase();
        return db.insert("ingredients", null, values) != -1;
    }

    public Cursor getIngredients() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT id, name, quantity, unit FROM ingredients ORDER BY name",
                null
        );
    }

    public int deleteIngredient(int id) {
        return getWritableDatabase().delete(
                "ingredients", "id = ?", new String[]{String.valueOf(id)}
        );
    }
}
