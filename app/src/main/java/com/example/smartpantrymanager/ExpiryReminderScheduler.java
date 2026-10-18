
package com.example.smartpantrymanager;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class ExpiryReminderScheduler {

    private static final String WORK_NAME =
            "SmartPantryExpiryReminder";

    public static void scheduleExpiryReminders(Context context) {

        // Schedule automatic expiry checks approximately every 24 hours
        PeriodicWorkRequest expiryWorkRequest =
                new PeriodicWorkRequest.Builder(
                        ExpiryNotificationWorker.class,
                        24,
                        TimeUnit.HOURS
                ).build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        expiryWorkRequest
                );
    }

    // Temporary method for testing notifications
    public static void testExpiryNotification(Context context) {

        OneTimeWorkRequest testRequest =
                new OneTimeWorkRequest.Builder(
                        ExpiryNotificationWorker.class
                ).build();

        WorkManager.getInstance(context)
                .enqueue(testRequest);
    }
}
