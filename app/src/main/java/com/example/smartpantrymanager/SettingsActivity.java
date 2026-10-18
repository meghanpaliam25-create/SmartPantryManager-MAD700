
package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchNotifications;
    private Spinner spinnerExpiryDays;
    private SharedPreferences preferences;

    private final int[] alertDays = {1, 2, 3, 5, 7, 14};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        switchNotifications = findViewById(R.id.switchNotifications);
        spinnerExpiryDays = findViewById(R.id.spinnerExpiryDays);
        TextView textAbout = findViewById(R.id.textAbout);
        Button btnBackToPantry = findViewById(R.id.btnBackToPantry);

        preferences = getSharedPreferences(
                "SmartPantrySettings",
                MODE_PRIVATE
        );

        boolean notificationsEnabled =
                preferences.getBoolean("notifications_enabled", true);

        int savedDays =
                preferences.getInt("expiry_alert_days", 3);

        switchNotifications.setChecked(notificationsEnabled);

        String[] dayOptions = {
                "1 day before expiry",
                "2 days before expiry",
                "3 days before expiry",
                "5 days before expiry",
                "7 days before expiry",
                "14 days before expiry"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                dayOptions
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerExpiryDays.setAdapter(adapter);

        int selectedPosition = 2;

        for (int i = 0; i < alertDays.length; i++) {
            if (alertDays[i] == savedDays) {
                selectedPosition = i;
                break;
            }
        }

        spinnerExpiryDays.setSelection(selectedPosition);

        switchNotifications.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    preferences.edit()
                            .putBoolean("notifications_enabled", isChecked)
                            .apply();
                }
        );

        spinnerExpiryDays.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id
                    ) {

                        preferences.edit()
                                .putInt(
                                        "expiry_alert_days",
                                        alertDays[position]
                                )
                                .apply();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {
                    }
                }
        );

        textAbout.setText(
                "Smart Pantry Manager\nVersion 1.0\n\n" +
                        "Manage pantry ingredients, track expiry dates, " +
                        "and discover recipes using available food."
        );

        btnBackToPantry.setOnClickListener(v -> finish());
    }
}
