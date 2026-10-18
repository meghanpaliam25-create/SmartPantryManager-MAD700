
package com.example.smartpantrymanager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ExpiryNotificationWorker extends Worker {

    private static final String CHANNEL_ID =
            "pantry_expiry_reminders";

    public ExpiryNotificationWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams
    ) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Context context = getApplicationContext();

        SharedPreferences preferences =
                context.getSharedPreferences(
                        "SmartPantrySettings",
                        Context.MODE_PRIVATE
                );

        boolean notificationsEnabled =
                preferences.getBoolean(
                        "notifications_enabled",
                        true
                );

        if (!notificationsEnabled) {
            return Result.success();
        }

        int alertDays =
                preferences.getInt("expiry_alert_days", 3);

        DatabaseHelper databaseHelper =
                new DatabaseHelper(context);

        try (Cursor cursor =
                     databaseHelper.getAllIngredients()) {

            int nameIndex =
                    cursor.getColumnIndexOrThrow("name");

            int expiryIndex =
                    cursor.getColumnIndexOrThrow("expiry_date");

            SimpleDateFormat dateFormat =
                    new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.US
                    );

            dateFormat.setLenient(false);

            StringBuilder expiringIngredients =
                    new StringBuilder();

            int count = 0;

            while (cursor.moveToNext()) {

                String name =
                        cursor.getString(nameIndex);

                String expiryDate =
                        cursor.getString(expiryIndex);

                if (expiryDate == null ||
                        expiryDate.trim().isEmpty()) {
                    continue;
                }

                Date expiry;

                try {
                    expiry = dateFormat.parse(expiryDate);
                } catch (Exception e) {
                    continue;
                }

                if (expiry == null) {
                    continue;
                }

                Date today;

                try {
                    today = dateFormat.parse(
                            dateFormat.format(new Date())
                    );
                } catch (Exception e) {
                    continue;
                }

                long difference =
                        expiry.getTime() - today.getTime();

                long daysUntilExpiry =
                        TimeUnit.MILLISECONDS.toDays(difference);

                if (daysUntilExpiry >= 0 &&
                        daysUntilExpiry <= alertDays) {

                    count++;

                    if (count <= 5) {
                        if (expiringIngredients.length() > 0) {
                            expiringIngredients.append(", ");
                        }

                        expiringIngredients.append(name);
                    }
                }
            }

            if (count > 0) {

                String message =
                        expiringIngredients.toString();

                if (count > 5) {
                    message += " and " +
                            (count - 5) + " more";
                }

                sendNotification(context, count, message);
            }

            return Result.success();

        } catch (Exception e) {

            return Result.retry();

        } finally {

            databaseHelper.close();
        }
    }

    private void sendNotification(
            Context context,
            int count,
            String message
    ) {

        NotificationManager notificationManager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (notificationManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Pantry Expiry Reminders",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );

            channel.setDescription(
                    "Notifications for ingredients approaching expiry"
            );

            notificationManager.createNotificationChannel(
                    channel
            );
        }

        if (Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        )
                        .setContentTitle(
                                count + " pantry ingredient(s) expiring soon"
                        )
                        .setContentText(message)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(message)
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_DEFAULT
                        )
                        .setAutoCancel(true);

        try {
            notificationManager.notify(1001, builder.build());
        } catch (SecurityException e) {
            android.util.Log.e(
                    "ExpiryNotificationWorker",
                    "Notification permission not granted",
                    e
            );
        }
    }
}

