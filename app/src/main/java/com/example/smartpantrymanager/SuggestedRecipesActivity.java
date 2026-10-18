
package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private final ArrayList<String> recipeList = new ArrayList<>();
    private final ArrayList<Long> recipeIds = new ArrayList<>();

    private ArrayAdapter<String> recipeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_suggested_recipes);

        ListView listViewRecipes =
                findViewById(R.id.listViewRecipes);

        Button btnBackToPantry =
                findViewById(R.id.btnBackToPantry);

        databaseHelper = new DatabaseHelper(this);

        recipeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                recipeList
        );

        listViewRecipes.setAdapter(recipeAdapter);

        // Open the selected recipe's detail screen
        listViewRecipes.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position >= recipeIds.size()) {
                        return;
                    }

                    long recipeId = recipeIds.get(position);

                    Intent intent = new Intent(
                            SuggestedRecipesActivity.this,
                            RecipeDetailActivity.class
                    );

                    intent.putExtra("recipe_id", recipeId);
                    startActivity(intent);
                }
        );

        // Return to the main pantry screen
        btnBackToPantry.setOnClickListener(v -> finish());

        loadSuggestedRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && recipeAdapter != null) {
            loadSuggestedRecipes();
        }
    }

    // Load recipes that can be made using pantry ingredients
    private void loadSuggestedRecipes() {

        recipeList.clear();
        recipeIds.clear();

        try {

            databaseHelper.ensureRecipesSeeded();

            Map<String, Double> pantry = new HashMap<>();

            // STEP 1: Read available pantry ingredients
            try (Cursor cursor = databaseHelper.getAllIngredients()) {

                int nameCol =
                        cursor.getColumnIndexOrThrow("name");

                int qtyCol =
                        cursor.getColumnIndexOrThrow("quantity");

                int unitCol =
                        cursor.getColumnIndexOrThrow("unit");

                while (cursor.moveToNext()) {

                    String name = normalizeName(
                            cursor.getString(nameCol)
                    );

                    String unit = normalizeUnit(
                            cursor.getString(unitCol)
                    );

                    double quantity = cursor.getDouble(qtyCol);

                    if (name.isEmpty() || quantity <= 0) {
                        continue;
                    }

                    // Convert units to standard measurements
                    quantity *= unitMultiplier(unit);

                    unit = baseUnit(unit);

                    String key = name + "|" + unit;

                    pantry.put(
                            key,
                            pantry.getOrDefault(key, 0.0) + quantity
                    );
                }
            }

            // STEP 2: Read available recipes
            try (Cursor recipes = databaseHelper.getAllRecipes()) {

                int idCol =
                        recipes.getColumnIndexOrThrow("id");

                int nameCol =
                        recipes.getColumnIndexOrThrow("name");

                while (recipes.moveToNext()) {

                    long recipeId = recipes.getLong(idCol);

                    String recipeName =
                            recipes.getString(nameCol);

                    boolean matches = true;
                    boolean hasRequirements = false;

                    Map<String, Double> required = new HashMap<>();

                    // STEP 3: Read ingredients required by recipe
                    try (Cursor ingredients =
                                 databaseHelper.getRecipeIngredients(recipeId)) {

                        int ingCol =
                                ingredients.getColumnIndexOrThrow(
                                        "ingredient_name"
                                );

                        int qtyCol =
                                ingredients.getColumnIndexOrThrow(
                                        "quantity"
                                );

                        int unitCol =
                                ingredients.getColumnIndexOrThrow(
                                        "unit"
                                );

                        while (ingredients.moveToNext()) {

                            hasRequirements = true;

                            String ingredientName = normalizeName(
                                    ingredients.getString(ingCol)
                            );

                            String unit = normalizeUnit(
                                    ingredients.getString(unitCol)
                            );

                            double quantity =
                                    ingredients.getDouble(qtyCol);

                            quantity *= unitMultiplier(unit);

                            unit = baseUnit(unit);

                            String key = ingredientName + "|" + unit;

                            required.put(
                                    key,
                                    required.getOrDefault(key, 0.0)
                                            + quantity
                            );
                        }
                    }

                    // STEP 4: Compare required and available quantities
                    for (Map.Entry<String, Double> item :
                            required.entrySet()) {

                        double available =
                                pantry.getOrDefault(
                                        item.getKey(),
                                        0.0
                                );

                        double needed = item.getValue();

                        if (available + 0.000001 < needed) {
                            matches = false;
                            break;
                        }
                    }

                    // STEP 5: Display matching recipes
                    if (hasRequirements && matches) {

                        recipeList.add(
                                recipeName +
                                        "\nAll ingredients available"
                        );

                        // Keep the database ID for each displayed recipe
                        recipeIds.add(recipeId);
                    }
                }
            }

            // No matching recipes
            if (recipeList.isEmpty()) {

                recipeList.add(
                        "No recipes match your pantry yet.\n" +
                                "Add more ingredients or increase quantities."
                );
            }

        } catch (Exception e) {

            Log.e(
                    "SuggestedRecipes",
                    "Unable to load suggested recipes",
                    e
            );

            recipeList.clear();
            recipeIds.clear();

            recipeList.add(
                    "Unable to load recipes. Please try again."
            );

            Toast.makeText(
                    this,
                    "Recipe loading failed",
                    Toast.LENGTH_LONG
            ).show();
        }

        recipeAdapter.notifyDataSetChanged();
    }

    // Standardize ingredient names
    private String normalizeName(String raw) {

        if (raw == null) {
            return "";
        }

        String name = raw.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        switch (name) {

            case "tomatoes":
                return "tomato";

            case "potatoes":
                return "potato";

            case "egg":
                return "eggs";

            case "carrots":
                return "carrot";

            case "bananas":
                return "banana";

            default:
                return name;
        }
    }

    // Standardize ingredient measurement units
    private String normalizeUnit(String raw) {

        if (raw == null) {
            return "unknown";
        }

        String unit = raw.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        // Example: "Millilitres (ml)" becomes "ml"
        int openBracket = unit.lastIndexOf('(');
        int closeBracket = unit.lastIndexOf(')');

        if (openBracket >= 0 &&
                closeBracket > openBracket) {

            String abbreviation = unit.substring(
                    openBracket + 1,
                    closeBracket
            ).trim();

            if (!abbreviation.isEmpty()) {
                unit = abbreviation;
            }
        }

        switch (unit) {

            case "grams":
            case "gram":
            case "g":
                return "g";

            case "kilograms":
            case "kilogram":
            case "kg":
                return "kg";

            case "millilitres":
            case "milliliters":
            case "millilitre":
            case "milliliter":
            case "ml":
                return "ml";

            case "litres":
            case "liters":
            case "litre":
            case "liter":
            case "l":
                return "l";

            case "piece":
            case "pieces":
            case "pcs":
            case "pc":
            case "unit":
            case "units":
                return "pieces";

            case "slice":
            case "slices":
                return "slices";

            default:
                return unit;
        }
    }

    // Convert kilograms to grams and litres to millilitres
    private double unitMultiplier(String unit) {

        if (unit.equals("kg") || unit.equals("l")) {
            return 1000.0;
        }

        return 1.0;
    }

    // Return the standard base unit
    private String baseUnit(String unit) {

        if (unit.equals("kg")) {
            return "g";
        }

        if (unit.equals("l")) {
            return "ml";
        }

        return unit;
    }

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
