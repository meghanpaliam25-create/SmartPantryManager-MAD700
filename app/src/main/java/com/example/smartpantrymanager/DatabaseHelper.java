
package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.Locale;

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
        seedRecipes(db);
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

    // Seed a starter recipe collection on first database creation.
    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "French Toast", "Beat eggs with milk. Dip bread and cook on both sides in a pan.",
                new String[]{"bread", "eggs", "milk"}, new double[]{2, 2, 50},
                new String[]{"slices", "pieces", "ml"});
        addRecipe(db, "Scrambled Eggs", "Beat eggs with milk. Melt butter and stir eggs over low heat.",
                new String[]{"eggs", "milk", "butter"}, new double[]{2, 30, 10},
                new String[]{"pieces", "ml", "g"});
        addRecipe(db, "Tomato Sandwich", "Slice tomato and assemble with bread and butter.",
                new String[]{"bread", "tomato", "butter"}, new double[]{2, 1, 10},
                new String[]{"slices", "pieces", "g"});
        addRecipe(db, "Cheese Sandwich", "Butter bread, add cheese and serve or toast.",
                new String[]{"bread", "cheese", "butter"}, new double[]{2, 30, 10},
                new String[]{"slices", "g", "g"});
        addRecipe(db, "Pasta with Tomato", "Cook pasta. Heat tomato in oil and combine.",
                new String[]{"pasta", "tomato", "oil"}, new double[]{100, 2, 10},
                new String[]{"g", "pieces", "ml"});
        addRecipe(db, "Banana Smoothie", "Blend banana and milk until smooth.",
                new String[]{"banana", "milk"}, new double[]{1, 200},
                new String[]{"pieces", "ml"});
        addRecipe(db, "Simple Omelette", "Beat eggs, add chopped tomato and cheese, and cook in a pan.",
                new String[]{"eggs", "cheese", "tomato"}, new double[]{2, 30, 1},
                new String[]{"pieces", "g", "pieces"});
        addRecipe(db, "Rice and Vegetables", "Cook rice and vegetables until tender, then combine.",
                new String[]{"rice", "carrot", "peas"}, new double[]{100, 1, 50},
                new String[]{"g", "pieces", "g"});
        addRecipe(db, "Boiled Eggs", "Boil eggs until cooked to your preference.",
                new String[]{"eggs"}, new double[]{2}, new String[]{"pieces"});
        addRecipe(db, "Buttered Toast", "Toast bread and spread with butter.",
                new String[]{"bread", "butter"}, new double[]{2, 10},
                new String[]{"slices", "g"});
        addRecipe(db, "Cheese Toast", "Top bread with cheese and toast until melted.",
                new String[]{"bread", "cheese"}, new double[]{2, 30},
                new String[]{"slices", "g"});
        addRecipe(db, "Banana Milk", "Mash or blend banana into milk.",
                new String[]{"banana", "milk"}, new double[]{1, 150},
                new String[]{"pieces", "ml"});
        addRecipe(db, "Tomato Rice", "Cook rice and stir in chopped tomato.",
                new String[]{"rice", "tomato"}, new double[]{100, 1},
                new String[]{"g", "pieces"});
        addRecipe(db, "Carrot and Pea Rice", "Cook rice with diced carrots and peas.",
                new String[]{"rice", "carrot", "peas"}, new double[]{100, 1, 50},
                new String[]{"g", "pieces", "g"});
        addRecipe(db, "Cheesy Pasta", "Cook pasta, then stir in cheese until melted.",
                new String[]{"pasta", "cheese"}, new double[]{100, 40},
                new String[]{"g", "g"});
        addRecipe(db, "Egg Sandwich", "Cook eggs and place between slices of bread.",
                new String[]{"eggs", "bread"}, new double[]{2, 2},
                new String[]{"pieces", "slices"});
        addRecipe(db, "Tomato Omelette", "Beat eggs, add diced tomato, and cook in a pan.",
                new String[]{"eggs", "tomato"}, new double[]{2, 1},
                new String[]{"pieces", "pieces"});
        addRecipe(db, "Butter Pasta", "Cook pasta♦ and toss with melted butter.",
                new String[]{"pasta", "butter"}, new double[]{100, 15},
                new String[]{"g", "g"});
    }

    private void addRecipe(SQLiteDatabase db, String name, String instructions,
                           String[] ingredients, double[] quantities, String[] units) {
        ContentValues recipe = new ContentValues();
        recipe.put("name", name);
        recipe.put("instructions", instructions);
        long recipeId = db.insertOrThrow(TABLE_RECIPES, null, recipe);
        for (int i = 0; i < ingredients.length; i++) {
            ContentValues requirement = new ContentValues();
            requirement.put("recipe_id", recipeId);
            requirement.put("ingredient_name", ingredients[i]);
            requirement.put("quantity", quantities[i]);
            requirement.put("unit", units[i]);
            db.insertOrThrow(TABLE_RECIPE_INGREDIENTS, null, requirement);
        }
    }

    // Existing databases are kept; recipes are seeded once if the table is empty.
    public void ensureRecipesSeeded() {
        SQLiteDatabase db = getWritableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RECIPES, null)) {
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) return;
        }
        db.beginTransaction();
        try {
            seedRecipes(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getAllRecipes() {
        return getReadableDatabase().query(TABLE_RECIPES, null, null, null,
                null, null, "name COLLATE NOCASE ASC");
    }

    public Cursor getRecipeIngredients(long recipeId) {
        return getReadableDatabase().query(TABLE_RECIPE_INGREDIENTS, null,
                "recipe_id = ?", new String[]{String.valueOf(recipeId)},
                null, null, "id ASC");
    }

}
