
package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddEditIngredientActivity extends AppCompatActivity {

    private EditText editIngredientName;
    private EditText editQuantity;
    private EditText editExpiryDate;
    private Spinner spinnerUnit;
    private Button btnSaveIngredient;

    private DatabaseHelper databaseHelper;

    // -1 means we are adding a new ingredient
    private long ingredientId = -1;

    private final String[] units = {
            "Pieces",
            "Grams (g)",
            "Kilograms (kg)",
            "Millilitres (ml)",
            "Litres (L)",
            "Cups",
            "Tablespoons",
            "Teaspoons"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        // Connect XML components
        editIngredientName =
                findViewById(R.id.editIngredientName);

        editQuantity =
                findViewById(R.id.editQuantity);

        editExpiryDate =
                findViewById(R.id.editExpiryDate);

        spinnerUnit =
                findViewById(R.id.spinnerUnit);

        btnSaveIngredient =
                findViewById(R.id.btnSaveIngredient);

        Button btnCancel =
                findViewById(R.id.btnCancel);

        databaseHelper = new DatabaseHelper(this);

        // Set up unit dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerUnit.setAdapter(adapter);

        // Check whether an ingredient ID was passed
        ingredientId = getIntent().getLongExtra(
                "ingredient_id", -1
        );

        if (ingredientId != -1) {

            // Editing an existing ingredient
            btnSaveIngredient.setText("Update Ingredient");


            loadIngredientDetails();

        } else {

            // Adding a new ingredient
            btnSaveIngredient.setText("Save Ingredient");
        }

        // Open calendar
        editExpiryDate.setOnClickListener(
                v -> showDatePicker()
        );

        // Save or update ingredient
        btnSaveIngredient.setOnClickListener(
                v -> saveIngredient()
        );

        // Cancel
        btnCancel.setOnClickListener(
                v -> finish()
        );
    }

    private void loadIngredientDetails() {

        try (Cursor cursor =
                     databaseHelper.getIngredientById(ingredientId)) {

            if (cursor.moveToFirst()) {

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

                double quantity = cursor.getDouble(
                        cursor.getColumnIndexOrThrow("quantity")
                );

                String unit = cursor.getString(
                        cursor.getColumnIndexOrThrow("unit")
                );

                String expiryDate = cursor.getString(
                        cursor.getColumnIndexOrThrow("expiry_date")
                );

                editIngredientName.setText(name);

                editQuantity.setText(
                        String.valueOf(quantity)
                );

                editExpiryDate.setText(
                        expiryDate == null ? "" : expiryDate
                );

                // Select previously saved unit
                for (int i = 0; i < units.length; i++) {

                    if (units[i].equals(unit)) {
                        spinnerUnit.setSelection(i);
                        break;
                    }
                }

            } else {
                Toast.makeText(
                        this,
                        "Ingredient not found",
                        Toast.LENGTH_SHORT
                ).show();
                finish();
            }

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Unable to load ingredient",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
        }
    }

    private void saveIngredient() {

        String name = editIngredientName.getText()
                .toString().trim();

        String quantityText = editQuantity.getText()
                .toString().trim();

        String unit = spinnerUnit.getSelectedItem()
                .toString();

        String expiryDate = editExpiryDate.getText()
                .toString().trim();

        // Validate name
        if (name.isEmpty()) {
            editIngredientName.setError(
                    "Please enter an ingredient name"
            );
            editIngredientName.requestFocus();
            return;
        }

        // Validate quantity
        if (quantityText.isEmpty()) {
            editQuantity.setError(
                    "Please enter a quantity"
            );
            editQuantity.requestFocus();
            return;
        }

        double quantity;

        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            editQuantity.setError("Enter a valid number");
            return;
        }

        if (!Double.isFinite(quantity) || quantity <= 0) {
            editQuantity.setError(
                    "Quantity must be greater than zero"
            );
            return;
        }

        if (ingredientId == -1) {

            // CREATE - Save new ingredient
            long result = databaseHelper.addIngredient(
                    name,
                    quantity,
                    unit,
                    expiryDate
            );

            if (result != -1) {

                Toast.makeText(
                        this,
                        "Ingredient saved successfully!",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

            } else {

                Toast.makeText(
                        this,
                        "Failed to save ingredient",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } else {

            // UPDATE - Edit existing ingredient
            int rowsUpdated = databaseHelper.updateIngredient(
                    ingredientId,
                    name,
                    quantity,
                    unit,
                    expiryDate
            );

            if (rowsUpdated > 0) {

                Toast.makeText(
                        this,
                        "Ingredient updated successfully!",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

            } else {

                Toast.makeText(
                        this,
                        "Failed to update ingredient",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (view, selectedYear,
                         selectedMonth, selectedDay) -> {

                            String selectedDate = String.format(
                                    Locale.US,
                                    "%04d-%02d-%02d",
                                    selectedYear,
                                    selectedMonth + 1,
                                    selectedDay
                            );

                            editExpiryDate.setText(selectedDate);
                        },
                        year,
                        month,
                        day
                );

        datePickerDialog.show();
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
