
package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listViewIngredients;
    private DatabaseHelper databaseHelper;

    private ArrayList<String> ingredientList;
    private ArrayList<Long> ingredientIds;
    private ArrayAdapter<String> ingredientAdapter;

    private Button btnAddIngredient;
    private Button btnSuggestedRecipes;
    private Button btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect XML components
        listViewIngredients =
                findViewById(R.id.listViewIngredients);

        btnAddIngredient =
                findViewById(R.id.btnAddIngredient);

        btnSuggestedRecipes =
                findViewById(R.id.btnSuggestedRecipes);

        btnSettings =
                findViewById(R.id.btnSettings);

        // Initialize SQLite database
        databaseHelper = new DatabaseHelper(this);

        // Initialize ingredient lists
        ingredientList = new ArrayList<>();
        ingredientIds = new ArrayList<>();

        ingredientAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                ingredientList
        );

        listViewIngredients.setAdapter(ingredientAdapter);

        // Add Ingredient button
        btnAddIngredient.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    AddEditIngredientActivity.class
            );

            startActivity(intent);
        });

        // Select an ingredient to edit or delete
        listViewIngredients.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position < ingredientIds.size()) {

                        long ingredientId =
                                ingredientIds.get(position);

                        showIngredientOptions(ingredientId);
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadIngredients();
    }

    // Load all saved ingredients from SQLite
    private void loadIngredients() {

        ingredientList.clear();
        ingredientIds.clear();

        try (Cursor cursor =
                     databaseHelper.getAllIngredients()) {

            int idIndex =
                    cursor.getColumnIndexOrThrow("id");

            int nameIndex =
                    cursor.getColumnIndexOrThrow("name");

            int quantityIndex =
                    cursor.getColumnIndexOrThrow("quantity");

            int unitIndex =
                    cursor.getColumnIndexOrThrow("unit");

            int expiryIndex =
                    cursor.getColumnIndexOrThrow("expiry_date");

            while (cursor.moveToNext()) {

                long id = cursor.getLong(idIndex);

                String name =
                        cursor.getString(nameIndex);

                double quantity =
                        cursor.getDouble(quantityIndex);

                String unit =
                        cursor.getString(unitIndex);

                String expiryDate =
                        cursor.getString(expiryIndex);

                String ingredientInfo =
                        name + "\nQuantity: " +
                                quantity + " " + unit;

                if (expiryDate != null &&
                        !expiryDate.isEmpty()) {

                    ingredientInfo +=
                            "\nExpiry: " + expiryDate;
                }

                ingredientIds.add(id);
                ingredientList.add(ingredientInfo);
            }

            if (ingredientList.isEmpty()) {

                ingredientList.add(
                        "Your pantry is empty. Add an ingredient!"
                );
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to load pantry ingredients",
                    Toast.LENGTH_LONG
            ).show();
        }

        ingredientAdapter.notifyDataSetChanged();
    }

    // Display options when an ingredient is selected
    private void showIngredientOptions(long ingredientId) {

        String[] options = {
                "Edit Ingredient",
                "Delete Ingredient",
                "Cancel"
        };

        new AlertDialog.Builder(this)
                .setTitle("Ingredient Options")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {

                        // Open existing ingredient for editing
                        Intent intent = new Intent(
                                MainActivity.this,
                                AddEditIngredientActivity.class
                        );

                        intent.putExtra(
                                "ingredient_id",
                                ingredientId
                        );

                        startActivity(intent);

                    } else if (which == 1) {

                        // Ask for confirmation before deleting
                        confirmDelete(ingredientId);
                    }

                    // Cancel closes the dialog automatically

                })
                .show();
    }

    // Confirm and delete selected ingredient
    private void confirmDelete(long ingredientId) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage(
                        "Are you sure you want to delete this ingredient?"
                )
                .setPositiveButton("Delete", (dialog, which) -> {

                    int rowsDeleted =
                            databaseHelper.deleteIngredient(
                                    ingredientId
                            );

                    if (rowsDeleted > 0) {

                        Toast.makeText(
                                this,
                                "Ingredient deleted successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadIngredients();

                    } else {

                        Toast.makeText(
                                this,
                                "Unable to delete ingredient",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
