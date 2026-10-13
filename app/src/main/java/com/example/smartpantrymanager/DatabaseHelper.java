
package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME =
            "SmartPantry.db";

    private static final int DATABASE_VERSION = 1;

    // Database table names
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS =
            "recipe_ingredients";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Pantry table
        String createPantryTable =
                "CREATE TABLE " + TABLE_PANTRY + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "quantity REAL NOT NULL, " +
                        "unit TEXT NOT NULL, " +
                        "expiry_date TEXT" +
                        ")";

        db.execSQL(createPantryTable);

        // Recipes table
        String createRecipesTable =
                "CREATE TABLE " + TABLE_RECIPES + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "instructions TEXT NOT NULL" +
                        ")";

        db.execSQL(createRecipesTable);

        // Recipe ingredients table
        String createRecipeIngredientsTable =
                "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "recipe_id INTEGER NOT NULL, " +
                        "ingredient_name TEXT NOT NULL, " +
                        "quantity REAL NOT NULL, " +
                        "unit TEXT NOT NULL, " +
                        "FOREIGN KEY(recipe_id) REFERENCES " +
                        TABLE_RECIPES + "(id)" +
                        ")";

        db.execSQL(createRecipeIngredientsTable);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        // Database migrations will be added
        // when the schema changes in a future version.
    }

    // ==================================
    // CREATE - Add a new ingredient
    // ==================================

    public long addIngredient(
            String name,
            double quantity,
            String unit,
            String expiryDate) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("quantity", quantity);
        values.put("unit", unit);

        if (expiryDate == null || expiryDate.isEmpty()) {
            values.putNull("expiry_date");
        } else {
            values.put("expiry_date", expiryDate);
        }

        return db.insert(TABLE_PANTRY, null, values);
    }

    // ==================================
    // READ - Retrieve all pantry items
    // ==================================

    public Cursor getAllIngredients() {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_PANTRY,
                null,
                null,
                null,
                null,
                null,
                "name COLLATE NOCASE ASC"
        );
    }

    // ==================================
    // READ - Retrieve one ingredient
    // ==================================

    public Cursor getIngredientById(long id) {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_PANTRY,
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );
    }

    // ==================================
    // UPDATE - Edit an ingredient
    // ==================================

    public int updateIngredient(
            long id,
            String name,
            double quantity,
            String unit,
            String expiryDate) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("quantity", quantity);
        values.put("unit", unit);

        if (expiryDate == null || expiryDate.isEmpty()) {
            values.putNull("expiry_date");
        } else {
            values.put("expiry_date", expiryDate);
        }

        return db.update(
                TABLE_PANTRY,
                values,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // ==================================
    // DELETE - Remove an ingredient
    // ==================================

    public int deleteIngredient(long id) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_PANTRY,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }
}
