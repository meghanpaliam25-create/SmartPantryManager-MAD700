
package com.example.smartpantrymanager;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView txtRecipeName;
    private TextView txtRecipeIngredients;
    private TextView txtRecipeInstructions;
    private Button btnBackToRecipes;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        txtRecipeName = findViewById(R.id.txtRecipeName);
        txtRecipeIngredients = findViewById(R.id.txtRecipeIngredients);
        txtRecipeInstructions = findViewById(R.id.txtRecipeInstructions);
        btnBackToRecipes = findViewById(R.id.btnBackToRecipes);

        databaseHelper = new DatabaseHelper(this);

        long recipeId = getIntent().getLongExtra("recipe_id", -1);

        if (recipeId == -1) {
            Toast.makeText(
                    this,
                    "Recipe not found",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        loadRecipeDetails(recipeId);

        btnBackToRecipes.setOnClickListener(v -> finish());
    }

    private void loadRecipeDetails(long recipeId) {

        try (Cursor cursor = databaseHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_RECIPES,
                new String[]{"name", "instructions"},
                "id = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                txtRecipeName.setText(cursor.getString(0));
                txtRecipeInstructions.setText(cursor.getString(1));
            } else {
                Toast.makeText(
                        this,
                        "Recipe not found",
                        Toast.LENGTH_SHORT
                ).show();
                finish();
                return;
            }

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Unable to load recipe details",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        StringBuilder ingredients = new StringBuilder();

        try (Cursor cursor =
                     databaseHelper.getRecipeIngredients(recipeId)) {

            int nameIndex =
                    cursor.getColumnIndexOrThrow("ingredient_name");

            int quantityIndex =
                    cursor.getColumnIndexOrThrow("quantity");

            int unitIndex =
                    cursor.getColumnIndexOrThrow("unit");

            while (cursor.moveToNext()) {

                String name = cursor.getString(nameIndex);
                double quantity = cursor.getDouble(quantityIndex);
                String unit = cursor.getString(unitIndex);

                ingredients.append("• ")
                        .append(name)
                        .append(" — ")
                        .append(String.format(
                                Locale.getDefault(),
                                "%s",
                                quantity == Math.floor(quantity)
                                        ? String.valueOf((long) quantity)
                                        : String.valueOf(quantity)
                        ))
                        .append(" ")
                        .append(unit)
                        .append("\n");
            }

            txtRecipeIngredients.setText(ingredients.toString());

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Unable to load recipe ingredients",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
