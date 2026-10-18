
package com.example.smartpantrymanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listViewIngredients;
    private DatabaseHelper databaseHelper;

    private ArrayList<String> ingredientList;
    private ArrayList<Long> ingredientIds;
    private IngredientAdapter ingredientAdapter;

    private Button btnAddIngredient;
    private Button btnSuggestedRecipes;
    private Button btnSettings;

    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewIngredients = findViewById(R.id.listViewIngredients);
        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnSuggestedRecipes = findViewById(R.id.btnSuggestedRecipes);
        btnSettings = findViewById(R.id.btnSettings);

        databaseHelper = new DatabaseHelper(this);

        ingredientList = new ArrayList<>();
        ingredientIds = new ArrayList<>();

        // Connect the custom ingredient card adapter
        ingredientAdapter = new IngredientAdapter(
                this,
                ingredientList
        );

        listViewIngredients.setAdapter(ingredientAdapter);

        notificationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (!isGranted) {
                                Toast.makeText(
                                        this,
                                        "Notifications are disabled. You can enable them in Android Settings.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );

        requestNotificationPermission();

        // Schedule automatic expiry checks
        ExpiryReminderScheduler.scheduleExpiryReminders(
                getApplicationContext()
        );

        // Add Ingredient
        btnAddIngredient.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddEditIngredientActivity.class
            );
            startActivity(intent);
        });

        // Suggested Recipes
        btnSuggestedRecipes.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SuggestedRecipesActivity.class
            );
            startActivity(intent);
        });

        // Settings
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SettingsActivity.class
            );
            startActivity(intent);
        });

        // Select an ingredient to edit or delete
        listViewIngredients.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position < ingredientIds.size()) {
                        long ingredientId = ingredientIds.get(position);
                        showIngredientOptions(ingredientId);
                    }
                }
        );
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                );
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadIngredients();
    }

    // Load saved pantry ingredients from SQLite
    private void loadIngredients() {

        ingredientList.clear();
        ingredientIds.clear();

        try (Cursor cursor = databaseHelper.getAllIngredients()) {

            int idIndex = cursor.getColumnIndexOrThrow("id");
            int nameIndex = cursor.getColumnIndexOrThrow("name");
            int quantityIndex = cursor.getColumnIndexOrThrow("quantity");
            int unitIndex = cursor.getColumnIndexOrThrow("unit");
            int expiryIndex = cursor.getColumnIndexOrThrow("expiry_date");

            while (cursor.moveToNext()) {

                long id = cursor.getLong(idIndex);
                String name = cursor.getString(nameIndex);
                double quantity = cursor.getDouble(quantityIndex);
                String unit = cursor.getString(unitIndex);
                String expiryDate = cursor.getString(expiryIndex);

                String ingredientInfo =
                        name + "\nQuantity: " + quantity + " " + unit;

                if (expiryDate != null && !expiryDate.isEmpty()) {
                    ingredientInfo += "\nExpiry: " + expiryDate;
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

    // Show options when an ingredient is selected
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

                        Intent intent = new Intent(
                                MainActivity.this,
                                AddEditIngredientActivity.class
                        );

                        intent.putExtra("ingredient_id", ingredientId);
                        startActivity(intent);

                    } else if (which == 1) {

                        confirmDelete(ingredientId);
                    }
                })
                .show();
    }

    // Confirm before deleting an ingredient
    private void confirmDelete(long ingredientId) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage(
                        "Are you sure you want to delete this ingredient?"
                )
                .setPositiveButton("Delete", (dialog, which) -> {

                    int rowsDeleted =
                            databaseHelper.deleteIngredient(ingredientId);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menuPantry) {
            loadIngredients();
            return true;
        }

        if (itemId == R.id.menuAddIngredient) {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddEditIngredientActivity.class
            );
            startActivity(intent);
            return true;
        }

        if (itemId == R.id.menuSuggestedRecipes) {
            Intent intent = new Intent(
                    MainActivity.this,
                    SuggestedRecipesActivity.class
            );
            startActivity(intent);
            return true;
        }

        if (itemId == R.id.menuSettings) {
            Intent intent = new Intent(
                    MainActivity.this,
                    SettingsActivity.class
            );
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
